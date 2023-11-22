package com.example.journalapp;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.journalapp.note.Note;
import com.example.journalapp.note.NoteRepository;

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
    private TextView dateTextView;
    private EditText titleEditText;
    private EditText descriptionEditText;

    private NoteRepository noteRepository;
    private Note note;

    public static final String THEME_PREFERENCES = "theme_preferences";
    public static final String SELECTED_THEME = "selected_theme";


    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    /* ExecutorService-- must be used because database operations can
       take a non-trivial amount of time and block the main UI thread,
       causing an error*/
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    /**
     * Called when activity is first created
     *
     * @param saveInstanceState Bundle containing the saved state of the activity
     */
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        Intent intent = getIntent();

        if (intent.hasExtra("themeBackground")) {
            int themeBackgroundId = intent.getIntExtra("themeBackground", 0);
            setContentView(themeBackgroundId);
            saveThemeToPreferences(String.valueOf(themeBackgroundId));
        } else {
            // If no theme is provided, load the default theme from SharedPreferences
            String savedTheme = getThemeFromPreferences();
            if (savedTheme != null && !savedTheme.isEmpty()) {
                setContentView(Integer.parseInt(savedTheme));
            } else {
                setContentView(R.layout.activity_note);
            }
        }

        initWidgets();
        initOptionsMenu();
        // Check if the received intent is for a new note or existing note
        if (intent.hasExtra("note_id")) { // existing note

            // retrieve note_id
            String note_id = intent.getStringExtra("note_id");

            // retrieve existing note from database using noteId and populate the UI
            setExistingNote(note_id);
        } else { // new note
            setNewNote();
        }
    }
    private void saveThemeToPreferences(String themeId) {
        SharedPreferences preferences = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_THEME, themeId);
        editor.apply();
    }

    /**
     * Retrieve the selected theme from SharedPreferences
     *
     * @return The resource ID of the selected theme, or null if not found
     */
    private String getThemeFromPreferences() {
        SharedPreferences preferences = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE);
        return preferences.getString(SELECTED_THEME, null);
    }



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
                    //openThemeActivity();

                    return true;
                }
                else if (menuItem.getItemId() == R.id.item6) {
                    Toast.makeText(getApplicationContext(), "Remove Theme", Toast.LENGTH_SHORT).show();
                    removeThemeFromPreferences();
                    recreate();
                    return true;
                }
                return true;
            });
        });
    }

    private void openThemeActivity(){
        Intent intent = new Intent(NewNoteActivity.this, ThemeSelectionActivity.class);
        startActivity(intent);
    }

    private void removeThemeFromPreferences() {
        SharedPreferences preferences = getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(SELECTED_THEME);
        editor.apply();
    }


    /**
     * Initialize a new note with a date and store it
     * in the database
     */
    private void setNewNote() {
        Date currentDate = new Date();
        //String defaultThemeId = String.valueOf(R.layout.activity_note);
        note = new Note("", "", currentDate.toString());
        dateTextView.setText(note.getCreatedDate());
        //saveThemeToPreferences(defaultThemeId);
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
                descriptionEditText.setText(note.getDescription());
                //applyThemeToNote();



            });
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
     * @param description The journals description
     */
    public void saveNoteDescription(String description) {
        Log.d("TextWatcher", "Updating the description: " + description);
        note.setDescription(description);
        noteRepository.updateNoteDescription(note);
    }




    /**
     * Remove the note from the database if there is, no
     * title or description to be saved
     *
     * @param view The button view that triggers the save operation
     */
    public void exitNote(View view) {
        String description = note.getDescription();
        String title = note.getTitle();
        if (description.isEmpty() && title.isEmpty()) {
            noteRepository.deleteNote(note);
        }
        finish();
    }
}