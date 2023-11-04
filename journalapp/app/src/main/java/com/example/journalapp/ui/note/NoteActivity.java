package com.example.journalapp.ui.note;

import static com.example.journalapp.utils.ConversionUtil.convertNoteItemEntitiesToNoteItems;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.database.entity.Note;
import com.example.journalapp.database.entity.NoteItemEntity;
import com.example.journalapp.database.NoteRepository;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Activity representing a single note page
 * Contains creation and saving of a note
 */
public class NoteActivity extends AppCompatActivity implements NoteAdapter.OnNoteItemChangeListener {

    // Note Component Variables
    private EditText titleEditText;

    // Note Contents Variables
    private RecyclerView noteContentRecyclerView;
    private NoteAdapter noteAdapter;
    private List<NoteItem> noteItems;

    // Note Database Variables
    private NoteRepository noteRepository;
    private Note note;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // thread manager


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
        /* 1 second */
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
        RecyclerView noteContentRecyclerView = findViewById(R.id.recycler_view_notes); // Make sure this ID matches your layout
        noteAdapter = new NoteAdapter(noteItems);

        // Set up the RecyclerView
        noteContentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteContentRecyclerView.setAdapter(noteAdapter);
        noteAdapter.setOnNoteItemChangeListener(this); // notified to save if changes are made to noteItems
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
        noteItems.add(new NoteItem(NoteItem.ItemType.TEXT,null,"", null, 0)); // Empty text for the user to start typing

    }

    /**
     * Initialize an existing note with date, title, and description
     * from database
     */
    private void setExistingNote(String note_id) {
        // Observe the LiveData returned by the repository for note items
        noteRepository.getNoteItemsForNote(note_id).observe(this, noteItemEntities -> {
            // This code will run when the note items are loaded or when they change.
            // Convert NoteItemEntity to NoteItem
            List<NoteItem> newNoteItems = convertNoteItemEntitiesToNoteItems(noteItemEntities);
            // Update the shared noteItems list
            noteItems.clear();
            noteItems.addAll(newNoteItems);
            // Notify the adapter of the change
            noteAdapter.notifyDataSetChanged();
        });

        // Retrieve the note using the id on a background thread
        executorService.execute(() -> {
            Note fetchedNote = noteRepository.getNoteById(note_id);
            if (fetchedNote != null) {
                // Use UI Thread to update UI with the fetched note
                runOnUiThread(() -> {
                    note = fetchedNote;
                    titleEditText.setText(note.getTitle());
                    // You may also want to update the description or other fields if necessary.
                });
            } else {
                // Handle the case where the note is null (e.g., not found in the database)
                runOnUiThread(this::finish);
            }
        });
    }

    /**
     * Save note title locally and to database
     *
     * @param title The note's title
     */
    public void saveNoteTitle(String title) {
        Log.d("TextWatcher", "Updating the title: " + title);
        note.setTitle(title);
        noteRepository.updateNoteTitle(note);
    }

    /**
     * Save note description locally and to the database.
     *
     */
    public void saveNoteContent() {
        executorService.execute(() -> {
            // Get the current list of note items from the database synchronously
            List<NoteItemEntity> currentNoteItems = noteRepository.getNoteItemsForNoteSync(note.getId());

            // Create a list to hold new or updated entities
            List<NoteItemEntity> noteItemEntitiesToSave = new ArrayList<>();

            // Iterate over the local note items
            for (NoteItem noteItem : noteItems) {
                // Check if the NoteItem matches an existing NoteItemEntity
                NoteItemEntity matchingEntity = null;
                for (NoteItemEntity entity : currentNoteItems) {
                    if (entity.getItemId().equals(noteItem.getItemId())) {
                        matchingEntity = entity;
                        break;
                    }
                }

                if (matchingEntity != null) {
                    Log.e("Saving", "Found a matching Entity. Updating said Entity");
                    // Update the existing entity with new content
                    matchingEntity.setContent(noteItem.getContent());
                    matchingEntity.setOrderIndex(noteItem.getOrderIndex());
                    noteRepository.updateNoteItem(matchingEntity); // Update immediately
                } else {
                    Log.e("Saving", "Did not find matching Entity. Saving a new Entity");
                    // Create a new entity with the ID from NoteItem
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

            // Inform the user that the note has been saved
            // This must be done on the main thread since it interacts with the UI
            runOnUiThread(() -> Toast.makeText(NoteActivity.this, "Note saved", Toast.LENGTH_SHORT).show());
        });
    }




    /**
     * This is called when the NoteAdapter notices changes made to noteItems
     */
    @Override
    public void onNoteItemContentChanged(){
        saveNoteContent();
    }

    /**
     * Remove the note from the database if there is, no
     * title or description to be saved
     *
     * @param view The button view that triggers the save operation
     */
    public void exitNote(View view) {
        String title = note.getTitle();
        if ( (noteItems.size() == 1 && noteItems.get(0).getContent() == "") && title.isEmpty()) {
            Log.e("Exiting note", "Deleting note");
            noteRepository.deleteNote(note);
        }
        else{
            saveNoteContent();
        }
        finish();
    }
}
