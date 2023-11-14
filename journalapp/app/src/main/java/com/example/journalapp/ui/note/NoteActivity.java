package com.example.journalapp.ui.note;

import static com.example.journalapp.utils.ConversionUtil.convertNoteItemEntitiesToNoteItems;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteItemEntity;
import com.example.journalapp.database.NoteRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Activity representing a single note page
 * Contains creation and saving of a note
 */
public class NoteActivity extends AppCompatActivity implements NoteAdapter.OnNoteItemChangeListener, NoteAdapter.OnItemFocusChangeListener {

    // Note Component Variables
    private EditText titleEditText;

    // Note Contents Variables
    private RecyclerView noteContentRecyclerView;
    private NoteAdapter noteAdapter;
    private List<NoteItem> noteItems;
    private int focusedItem = -1; // starts at invalid
    private int highlightedItem = -1; // starts at invalid

    // Note Database Variables
    private NoteRepository noteRepository;
    private Note note;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // thread manager

    // Permission Variables
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    // Image Handling Variables
    // Special member variable used to launch activities that expect a result
    private final ActivityResultLauncher<String> mGetContent =
            registerForActivityResult( new ActivityResultContracts.GetContent(), uri -> {
                try{
                    // Handle returned Uri's

                    // save to local storage and insert to image
                    Uri localUri = saveImageToInternalStorage(uri);
                    insertImage(localUri);

                    // noteAdapter.notifyItemInserted(noteItems.size() - 1);
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(NoteActivity.this, "Failed to insert image", Toast.LENGTH_SHORT).show();
                }

                // If null, handle it:
            });



    /**
     * Called when activity is first created
     *
     * @param saveInstanceState Bundle containing the saved state of the activity
     */
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_note);

        // Initialize UI Widgets & set current date
        initWidgets();
        initOptionsMenu();
        initRecyclerView();

        // Check if the received intent is for a new note or existing note
        Intent intent = getIntent();

        if (intent.hasExtra("note_id")) {
            // Existing Note: retrieve note_id and set up existing note
            String note_id = intent.getStringExtra("note_id");
            setExistingNote(note_id);

        } else {
            // New Note: create note_id and create new note
            setNewNote();
        }
    }

    // ==============================
    // REGION: UI Initialization
    // ==============================

    /**
     * Initializes UI widgets, ViewModel, and set the edit text watcher with debouncing.
     */
    private void initWidgets() {
        titleEditText = findViewById(R.id.titleEditText);

        noteRepository = NoteRepository.getInstance(getApplication()); // initialize the note repo

        /*
         * create an Observable to monitor changes in the title using debouncing
         * Process: Action is taken --> emitter is notified --> emitter notifies observable
         *          observable notifies subscribers --> subscribers take action
         */
        Observable<String> titleChangedObservable = Observable.create(emitter -> titleEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // actions before text is changed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emitter.onNext(charSequence.toString()); // emitter is notified of an update
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // actions after text is changed
            }
        }));

        // Sets debounce time (ms) for title changes
        /* 0.5 second */
        int SAVE_DELAY = 500;
        Observable<String> titleObservable = titleChangedObservable
                .debounce(SAVE_DELAY, TimeUnit.MILLISECONDS);


        // Subscribe to observables to trigger a save to database
        compositeDisposable.addAll(
                titleObservable.subscribe(this::saveNoteTitle));
    }

    /**
     * Initialize the options menu in the notes page for additional actions
     */
    private void initOptionsMenu() {
        findViewById(R.id.optionsMenu).setOnClickListener(view -> {

            // popup menu (built-in)
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.getMenuInflater().inflate(R.menu.journal_options_menu, popupMenu.getMenu());

            // attempt to show icons in the menu
            try {
                popupMenu.getClass().getDeclaredMethod("setForceShowIcon", boolean.class)
                        .invoke(popupMenu, true);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            } finally {
                popupMenu.show();
            }

            // handle menu item choices & clicks
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                /* Don't ask why it's not a switch statement, it's just not. */
                if (menuItem.getItemId() == R.id.item1a) {
                    Toast.makeText(getApplicationContext(), "Take Photo/Video", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.item1b) {
                    Toast.makeText(getApplicationContext(), "Add Photo/Video From Library", Toast.LENGTH_SHORT).show();
                    checkPermissionAndOpenGallery();
                    return true;
                } else if (menuItem.getItemId() == R.id.item2) {
                    Toast.makeText(getApplicationContext(), "Add Voice Note", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.item3) {
                    Toast.makeText(getApplicationContext(), "Insert", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.item4) {
                    Toast.makeText(getApplicationContext(), "Save Note", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.item5) {
                    Toast.makeText(getApplicationContext(), "Add Template", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return true;
            });
        });
    }

    /**
     * Initialize the RecyclerView that represents a Note's contents
     * Contents can include: EditTexts, ImageViews, etc.
     */
    private void initRecyclerView(){

        // First initialize the noteItems variable
        noteItems = new ArrayList<>();

        // Initialize the RecyclerView and Adapter
        noteContentRecyclerView = findViewById(R.id.recycler_view_notes); // Make sure this ID matches your layout
        noteAdapter = new NoteAdapter(noteItems);

        // Set up the RecyclerView
        noteContentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteContentRecyclerView.setAdapter(noteAdapter);

        // Set all listeners
        noteAdapter.setOnNoteItemChangeListener(this); // notified to save if changes are made to noteItems
        noteAdapter.setOnItemFocusChangeListener(this); // notified if focus is shifted

        // @TODO set up to remove from highlights if outside of a recyclerView is pressed
    }

    // ==============================
    // REGION: Listeners
    // ==============================

    /**
     *
     */
    @Override
    public void onNoteItemContentChanged(){
        saveNoteContent();
    }

    /**
     * Called when focus changes (either user types on EditText or clicks in image)
     * @param position
     * @param hasFocus
     */
    @Override
    public void onItemFocusChange(int position, boolean hasFocus) {
        if (hasFocus){
            focusedItem = position;
            Log.e("Focus", "Focus has changed to position " + focusedItem);
        }
        else if (focusedItem == position){
            focusedItem = -1;
        }
    }

    /**
     * Called when user long clicks a view
     * @param position
     */
    @Override
    public void onItemLongClick(int position) {
        Log.e("ItemLongClick", "Position #" + position + " has been long clicked.");

        // highlight item in adapter
        noteAdapter.highlightItem(position);
        // update highlighteditem
        highlightedItem = position;

        // Find the view by position
        View view = Objects.requireNonNull(noteContentRecyclerView.findViewHolderForAdapterPosition(position)).itemView;

        // Create a PopupMenu
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.delete_menu); // Inflate your menu resource
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                // Perform deletion of the item
                deleteItem(position);
                return true;
            }
            // ... handle other menu item clicks if necessary
            return false;
        });
        popupMenu.show();
    }


    public void clearHighlight(){
        if (highlightedItem != 1){
            Log.e("Highlights", "clearing highlights");
            noteAdapter.clearHighlights();
            highlightedItem = -1;
        }
    }

    private void deleteItem(int position){
        Log.e("Deletion", "Deleting an item. Position: " + position);

        // remove items from the list
        noteItems.remove(position);

        // notify adapter
        noteAdapter.notifyItemRemoved(position);

        logNoteItems();

        // update ordering for subsequent items in the list
        for (int i = position; i < noteItems.size(); i++){
            noteItems.get(i).setOrderIndex(i);
        }

        logNoteItems();

        // notify the adapter of the item range changed for updating the view
        noteAdapter.notifyItemRangeChanged(position, noteItems.size() - position);

        saveNoteContent(); // @TODO must be changed to save to database
    }

    public void logNoteItems() {
        for (int i = 0; i < noteItems.size(); i++) {
            NoteItem item = noteItems.get(i);
            Log.d("NoteItemLog", "Index: " + i + ", Type: " + item.getType() + ", Content: " + item.getContent());
        }
    }
    // ==============================
    // REGION: Image Handling
    // ==============================

    /**
     * Called by a button
     * Checks for permissions to read images from storage
     * Permission Granted: Opens gallery for image selection
     * Permission not Granted: Request for permissions
     */
    private void checkPermissionAndOpenGallery() {
        // API Level 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            // API Level 33+
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // if no permissions, request
                ActivityCompat.requestPermissions(NoteActivity.this, new String[] {Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
            }
            else {
                // if permission granted, go to gallery
                selectImage();
            }
        }

        // API Level below 33
        else{

        }
            // API Below 33
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                // if no permissions, request
                ActivityCompat.requestPermissions(NoteActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSION);

            }

            else{
                // if permission granted, go to gallery
                selectImage();
            }
    }

    /**
     * Callback for the result from requesting permissions
     * @param requestCode The request code in
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // check permission requests for READ_MEDIA_IMAGES
        if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // if permission granted, then allow access
                selectImage();
            }
            else{

                // if permission denied, inform user
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Launches an intent to open the gallery
     * Allow the user to select an image
     */
    private void selectImage(){
        mGetContent.launch("image/*");
    }

    /**
     * Save the image to internal storage so it still exists even if user deletes from gallery
     * -- should be called right after the user picks their image
     * @param imageUri
     * @return
     */
    private Uri saveImageToInternalStorage(Uri imageUri){
        try{
            // Open image using received Uri
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Save image to app's internal storage
            String imageName = "image_" + System.currentTimeMillis() + ".png";
            FileOutputStream fos = openFileOutput(imageName, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            // return URI to saved image
            return FileProvider.getUriForFile(this,"com.example.journalapp.fileprovider", new File(getFilesDir(), imageName));
        } catch (Exception e) {
            e.printStackTrace();
            return null; // remember to handle using: if (uri != null)
        }
    }

    private boolean deleteImageFromInternalStorage(Uri imageUri) {
        try {
            // Get the file's name from the URI
            String fileName = new File(imageUri.getPath()).getName();

            // Build the file's path within the app's internal storage directory
            File fileToDelete = new File(getFilesDir(), fileName);

            // Delete the file
            return fileToDelete.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserts an image into the note based on the position of the current focused item
     * Case 1: Focused Item: EditText && isEmpty -> Replace with image and push EditText down
     * Case 2: Focused Item: EditText && !(isEmpty) -> Put image underneath EditText and create new EditText under image
     * Case 3: Focused Item: Image -> Put image underneath Image
     * @param imageUri
     */
    public void insertImage(Uri imageUri) {
        int focusedIndex = focusedItem;

        // Check if focus is within bounds of list
        if (focusedIndex >= 0 && focusedIndex < noteItems.size()) {
            // Get currently focused note item
            NoteItem focusedNoteItem = noteItems.get(focusedIndex);

            Log.e("Focus", "Focused item when inserting image: " + focusedItem);

            // Determine type of focused note item
            switch (focusedNoteItem.getType()) {
                case TEXT:
                    String textContent = focusedNoteItem.getContent();
                    if (textContent.isEmpty()) {
                        // Case 1: Empty EditText
                        noteItems.set(focusedIndex, new NoteItem(NoteItem.ItemType.IMAGE, null, imageUri.toString(), focusedIndex));
                        noteItems.add(focusedIndex + 1, new NoteItem(NoteItem.ItemType.TEXT, null, "", focusedIndex + 1));
                        noteAdapter.notifyItemChanged(focusedIndex);
                        noteAdapter.notifyItemInserted(focusedIndex + 1);
                    } else {
                        // Case 2: Non-Empty EditText
                        noteItems.add(focusedIndex + 1, new NoteItem(NoteItem.ItemType.IMAGE, null, imageUri.toString(), focusedIndex + 1));
                        noteItems.add(focusedIndex + 2, new NoteItem(NoteItem.ItemType.TEXT, null, "", focusedIndex + 2));
                        noteAdapter.notifyItemRangeInserted(focusedIndex + 1, 2);
                    }
                    break;
                case IMAGE:
                    // Case 3: Image Focused
                    noteItems.add(focusedIndex + 1, new NoteItem(NoteItem.ItemType.IMAGE, null, imageUri.toString(), focusedIndex + 1));
                    noteAdapter.notifyItemInserted(focusedIndex + 1);
                    break;
            }
        } else {
            // If no item is focused, add the image at the end @TODO insert a text after?? (new case)
            noteItems.add(new NoteItem(NoteItem.ItemType.IMAGE, null, imageUri.toString(), noteItems.size()));
            noteAdapter.notifyItemInserted(noteItems.size() - 1);
        }

        // Update order indexes for all items following the insertion point
        for (int i = focusedIndex + 1; i < noteItems.size(); i++) {
            noteItems.get(i).setOrderIndex(i);
        }
    }

    // ==============================
    // REGION: Setting up Note Data
    // ==============================

    /**
     * Initialize a new note with a date and store it
     * in the database
     */
    private void setNewNote() {

        Date currentDate = new Date();
        note = new Note("", currentDate.toString());
        noteRepository.insertNote(note);

        // Initialize the contents of noteItems as a single EditText
        noteItems.add(new NoteItem(NoteItem.ItemType.TEXT,null,"",  0)); // Empty text for the user to start typing

        // set focusedItem to first item
        focusedItem = 0;

    }

    /**
     * Initializes the UI with data from an existing note based on the provided note ID.
     * It retrieves the note details and note items from the database and updates the UI.
     *
     * @param note_id String ID of the note to be loaded into the UI
     */
    private void setExistingNote(String note_id) {

        // Observe the LiveData returned by the repository for note items
        noteRepository.getNoteItemsForNote(note_id).observe(this, noteItemEntities -> {
            // This code will run when the note items are loaded or when they change.

            // Convert NoteItemEntity to NoteItem
            List<NoteItem> newNoteItems = convertNoteItemEntitiesToNoteItems(noteItemEntities);

            // Make sure the noteItems list is clear to add all items from the database to it
            noteItems.clear();
            noteItems.addAll(newNoteItems);

            // Notify the adapter of the change to refresh RecyclerView.
            noteAdapter.notifyDataSetChanged(); // resource intensive, but okay because its only done when setting the existing note
        });

        // Retrieve the note using the id on a background thread
        executorService.execute(() -> {
            Note fetchedNote = noteRepository.getNoteById(note_id);
            if (fetchedNote != null) {

                // Use UI Thread to update UI with the fetched note
                runOnUiThread(() -> {
                    note = fetchedNote;
                    titleEditText.setText(note.getTitle());

                });
            } else {
                // Handle the case where the note is null (e.`g., not found in the database)
                runOnUiThread(this::finish);
            }
        });

        focusedItem = 0;
    }

    // ==============================
    // REGION: Database Operations
    // ==============================

    /**
     * Saves title of the note both locally and in the current activity instance
     * Called in response to changes in the title via auto save
     *
     * @param title The note's title
     */
    public void saveNoteTitle(String title) {
        Log.d("TextWatcher", "Updating the title: " + title);
        note.setTitle(title);
        noteRepository.updateNoteTitle(note);
    }

    /**
     * Saves the contents of the note to the database
     * First checks if it should update or add content to the database
     * Usually called by program when changes are detected by auto save
     *
     */
    public void saveNoteContent() {
        // must be done on a background thread
        executorService.execute(() -> {
            // Get the current list of note items from the database
            List<NoteItemEntity> currentNoteItems = noteRepository.getNoteItemsForNoteSync(note.getId());

            // Create a list to hold the IDs of LOCAL note items for comparison
            List<String> localNoteItemIds = new ArrayList<>();
            for (NoteItem noteItem : noteItems) {
                localNoteItemIds.add(noteItem.getItemId());
            }

            // Determine which items have been deleted
            List<NoteItemEntity> itemsToDelete = new ArrayList<>();
            for (NoteItemEntity entity : currentNoteItems) {
                if (!localNoteItemIds.contains(entity.getItemId())) {
                    itemsToDelete.add(entity);
                }
            }

            // Delete the removed items from the database
            for (NoteItemEntity entity : itemsToDelete) {
                noteRepository.deleteNoteItem(entity);

                // If the entity is of type IMAGE, delete from internal storage as well
                if (entity.getType() == NoteItem.ItemType.IMAGE.ordinal()) {
                    Uri imageUri = Uri.parse(entity.getContent()); // assuming the content is the URI in string format
                    boolean deleted = deleteImageFromInternalStorage(imageUri);
                    if (!deleted) {
                        // Handle the case where the image couldn't be deleted
                        Log.e("Delete Image", "Failed to delete image from internal storage.");
                    }
                }
            }

            // Iterate over the LOCAL note items
            for (NoteItem noteItem : noteItems) {
                // Check if there are any matches between LOCAL noteItems and DATABASE noteItems
                NoteItemEntity matchingEntity = null;
                for (NoteItemEntity entity : currentNoteItems) {
                    if (entity.getItemId().equals(noteItem.getItemId())) {
                        matchingEntity = entity;
                        break;
                    }
                }

                // If there is a match, then we want to update it
                if (matchingEntity != null) {
                    // Update the existing entity with new content and order
                    matchingEntity.setContent(noteItem.getContent());
                    matchingEntity.setOrderIndex(noteItem.getOrderIndex());
                    noteRepository.updateNoteItem(matchingEntity); // Update immediately
                } else {
                    // It's a new NoteItem, so create it in the database
                    NoteItemEntity newEntity = new NoteItemEntity(
                            noteItem.getItemId(), // Use the ID from NoteItem
                            note.getId(), // The ID of the note
                            noteItem.getType().ordinal(), // Convert enum to int
                            noteItem.getContent(),
                            noteItem.getOrderIndex()
                    );
                    noteRepository.insertNoteItem(newEntity); // Insert immediately
                }
            }

            // Inform user of the save on the UI thread
            runOnUiThread(() -> Toast.makeText(NoteActivity.this, "Note saved", Toast.LENGTH_SHORT).show());
        });
    }



    // ==============================
    // REGION: Other
    // ==============================

    /**
     * This is called when the NoteAdapter notices changes made to noteItems
     */

    /**
     * If back button is pressed
     */
    @Override
    public void onBackPressed() {
        exitNote(null);
    }

    /**
     * If user pauses the application
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()){
            exitNote(null);
        }
    }

    /**
     * Remove the note from the database if there is, no
     * title or if the contents of the note is empty.
     *
     * @param view The button view that triggers the save operation
     */
    public void exitNote(View view) {
        String title = note.getTitle();
        if ( (noteItems.size() == 1 && noteItems.get(0).getContent().equals("") ) && title.isEmpty()) {
            Log.e("Exiting note", "Deleting note");
            noteRepository.deleteNote(note);
        }
        else{
            saveNoteContent(); // add to onExit?? method instead?
        }
        finish();
    }
}

// @TODO change initRecyclerView listeners to implements & functions.
