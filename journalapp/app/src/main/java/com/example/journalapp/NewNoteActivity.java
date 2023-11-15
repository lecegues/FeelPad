package com.example.journalapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.journalapp.Utils.NoteMediaHandler;
import com.example.journalapp.note.Note;
import com.example.journalapp.note.NoteRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Activity representing a single note page
 * Contains creation and saving of a note
 */
public class NewNoteActivity extends AppCompatActivity {

    // Note Component Variables
    private TextView dateTextView;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Button BtnBold;

    private Uri mCurrentPhotoUri;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    // Note Database Variables
    private NoteRepository noteRepository;
    private Note note;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // thread manager


    // Permissions Variables
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 1337;

    // Special member variable used to launch activities that expect a result
    private final ActivityResultLauncher<String> mGetContent =
            registerForActivityResult( new ActivityResultContracts.GetContent(), uri -> {
                // Handle returned Uri's
                try{
                    Drawable drawable = getDrawableFromUri(uri);
                    insertImageIntoText(drawable, uri);
                } catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(NewNoteActivity.this, "Failed to insert image", Toast.LENGTH_SHORT).show();
                }

                if (uri != null) {
                    // Do nothing

                }
            });
    private final ActivityResultLauncher<Void> mTakePicture =
            registerForActivityResult( new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                // Handle returned Uri's
                Uri uri = null;
                try {
                    uri = saveImageFromBitmapToStorage(bitmap);
                    Drawable drawable = getDrawableFromUri(uri);
                    insertImageIntoText(drawable, uri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(NewNoteActivity.this, "Failed to insert image", Toast.LENGTH_SHORT).show();
                }
                if (uri != null) {
                    // Do nothing
                }

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
        initToolBar();

        // Check if the received intent is for a new note or existing note
        Intent intent = getIntent();

        if (intent.hasExtra("note_id")) { // existing note

            // retrieve note_id
            String note_id = intent.getStringExtra("note_id");

            // retrieve existing note from database using noteId and populate the UI
            setExistingNote(note_id);
        } else { // new note
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
        dateTextView = findViewById(R.id.dateTextView);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

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
        /* 5 seconds */
        int SAVE_DELAY = 1000;
        Observable<String> titleObservable = titleChangedObservable
                .debounce(SAVE_DELAY, TimeUnit.MILLISECONDS);

        /*
         * Creates an Observable to monitor changes in the description using debouncing
         */
        Observable<String> descriptionChangedObservable = Observable.create(emitter -> descriptionEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // actions before text is changed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emitter.onNext(charSequence.toString()); // emitter notified of update
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // actions after text is changed
            }
        }));

        // Sets debounce time (ms) for description
        /* 5 seconds */
        Observable<String> descriptionObservable = descriptionChangedObservable
                .debounce(SAVE_DELAY, TimeUnit.MILLISECONDS);

        // Subscribe to observables to trigger a save to database
        compositeDisposable.addAll(
                descriptionObservable.subscribe(this::saveNoteDescription),
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
                    checkPermissionAndOpenCamera();
                    return true;

                // if user wants to go into gallery to add photos to notes page
                } else if (menuItem.getItemId() == R.id.item1b) {
                    System.out.println("Gallery button is pressed. Now asking for permission");
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
     * Initialize the bottom toolbar
     */
    private void initToolBar(){
        BtnBold = findViewById(R.id.bold_button);
        BtnBold.setOnClickListener(v -> toggleBold());

    }

    // ==============================
    // REGION: Text Formatting
    // ==============================

    /**
     * Make text bold
     */
    private void toggleBold(){
        int start = descriptionEditText.getSelectionStart();
        int end = descriptionEditText.getSelectionEnd();
        if (start < end) {
            // if user has selected and pressed bold, then:
            SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(descriptionEditText.getText());
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            spannableBuilder.setSpan(boldSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            descriptionEditText.setText(spannableBuilder);
            descriptionEditText.setSelection(start,end);
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

        if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            // if no permissions, request
            ActivityCompat.requestPermissions(NewNoteActivity.this, new String[] {Manifest.permission.READ_MEDIA_IMAGES},REQUEST_STORAGE_PERMISSION);
        }else{
            // if permission granted, go to gallery
            selectImage();
        }
    }

    private void checkPermissionAndOpenCamera(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            // Permission has not been granted for using Camera, request it
            ActivityCompat.requestPermissions(NewNoteActivity.this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }else{
            // Permission has been granted, go to the camera
            takePicture();
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
     * Creates an intent to open the Camera
     * Starts that intent
     */
    private void takePicture() {
        mTakePicture.launch(null);
    }

    private Uri saveImageFromBitmapToStorage(Bitmap image) {
        Uri uri = null;
        try{
            String imageName = "image_" + System.currentTimeMillis() + ".png";
            FileOutputStream stream = openFileOutput(imageName, MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            return FileProvider.getUriForFile(this,"com.example.journalapp.fileprovider", new File(getFilesDir(), imageName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Take the given Uri and turn into a Drawable
     * @param uri The Uri to create the Drawable from
     * @return Drawable object created from input stream of the Uri
     * @throws IOException if there's an error opening the input stream
     */
    public Drawable getDrawableFromUri(Uri uri) throws IOException {

        // open input stream from the Uri and create the drawable
        InputStream inputStream = getContentResolver().openInputStream(uri);
        Drawable drawable = Drawable.createFromStream(inputStream, uri.toString());

        inputStream.close();
        return drawable;
    }


    /**
     * Inserts an image as a drawable into the EditText at the current cursor position
     * Image is scaled to fit the width of the screen, while maintaining aspect ratio
     * @param drawable Drawable object representing the image
     * @param uri Uri of image used as a content description
     */
    public void insertImageIntoText(Drawable drawable, Uri uri){

        // first, save image to storage
        Uri internalUri = saveImageToInternalStorage(uri);

        if (internalUri == null){
            Toast.makeText(this, "Error saving image internally", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ImageInsertion", "Inserting image with URI: " + internalUri.toString());

        // Calculate the dimensions of image
        int screenWidth = getResources().getDisplayMetrics().widthPixels; // width of screen
        int originalWidth = drawable.getIntrinsicWidth();
        int originalHeight = drawable.getIntrinsicHeight();
        int scaledHeight = (int) ((float) originalHeight * ((float) screenWidth / originalWidth));

        // Set drawable size and create an imageSpan with it
        drawable.setBounds(0, 0, screenWidth, scaledHeight);
        ImageSpan imageSpan = new ImageSpan(drawable, internalUri.toString());

        // get current positon of the cursor in the editText
        int selectionStart = descriptionEditText.getSelectionStart();
        int selectionEnd = descriptionEditText.getSelectionEnd();


        // get current text
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(descriptionEditText.getText());

        String placeholder = " \n"; // change where the image is placed in reference to the cursor
        spannableStringBuilder.replace(selectionStart, selectionEnd,placeholder);
        spannableStringBuilder.setSpan(imageSpan, selectionStart, selectionStart + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // set updated text to edittext
        descriptionEditText.setText(spannableStringBuilder);

        // move cursor to the line after the inserted image
        descriptionEditText.setSelection(selectionStart + 2);
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
            return null;
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
        note = new Note("", "", currentDate.toString());
        dateTextView.setText(note.getCreatedDate());
        noteRepository.insertNote(note);
    }

    /**
     * Initialize an existing note with date, title, and description
     * from database
     */
    private void setExistingNote(String note_id) {

        // use executorService for separate background thread instead of using UI thread
        // Note: not seen but execute has a 'Runnable' parameter that tells executorService
        //       to execute code inside the run() method; however we are using a lambda function
        executorService.execute(() -> {

            // retrieve note using id database operations
            try {
                note = noteRepository.getNoteById(note_id);
            }

            // if invalid note_id, then just close the note
            // @TODO improper handle of error
            catch (Exception e) {
                finish();
            }


            // Use UI Thread to update UI
            runOnUiThread(() -> {
                // Populate UI with existing note
                dateTextView.setText(note.getCreatedDate());
                titleEditText.setText(note.getTitle());

                Spanned spannedDescription;

                // convert HTML description to spanned object
                try{
                    spannedDescription = NoteMediaHandler.htmlToSpannable(this, note.getDescriptionHtml());
                    descriptionEditText.setText(spannedDescription);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(NewNoteActivity.this, "Error loading images from the note", Toast.LENGTH_SHORT).show();
                }

                Log.d("ValueCheck", "Raw Description: " + note.getDescriptionRaw());
                Log.d("ValueCheck", "HTML Description: " + note.getDescriptionHtml());


            });
        });
    }

    // ==============================
    // REGION: Database Operations
    // ==============================

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
     * @param description The journals description
     */
    public void saveNoteDescription(String description) throws FileNotFoundException {
        Log.d("TextWatcher", "Updating the description: " + description);
        Spannable descriptionContent = descriptionEditText.getText();
        String descriptionHTML = NoteMediaHandler.spannableToHtml(descriptionContent);

        // update description
        note.setDescriptionHtml(descriptionHTML, this);

        noteRepository.updateNoteDescription(note);
    }

    // ==============================
    // REGION: Other
    // ==============================

    /**
     * Remove the note from the database if there is, no
     * title or description to be saved
     *
     * @param view The button view that triggers the save operation
     */
    public void exitNote(View view) {
        String descriptionHTML = note.getDescriptionHtml();
        String title = note.getTitle();
        if (descriptionHTML.isEmpty() && title.isEmpty()) {
            noteRepository.deleteNote(note);
        }
        finish();
    }
}
