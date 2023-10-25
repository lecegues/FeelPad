package com.example.journalapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.journalapp.note.Note;
import com.example.journalapp.note.NoteViewModel;
import com.example.journalapp.utils.DateUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * Activity representing a single note page
 * Contains creation and saving of a note
 */
public class NewNoteActivity extends AppCompatActivity {
    private EditText titleEditText, descriptionEditText;
    private TextView dateTextView;
    private NoteViewModel noteViewModel;
    private Date currentDate;

    /**
     * Called when activity is first created
     * @param saveInstanceState Bundle containing the saved state of the activity
     */
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_note);

        // Initialize UI Widgets & set current date
        initWidgets();
        initOptionsMenu();
        setCurrentDate();
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
     * Initializes UI widgets and the ViewModel
     */
    private void initWidgets() {
        dateTextView = findViewById(R.id.dateTextView);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        // Creates a new instance of ViewModelProvider and associates it and a NoteViewModel with the current class
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
    }

    /**
     * Sets the current date for the new note
     */
    private void setCurrentDate() {
        // TODO: Possibly allow the user to update the date
        currentDate = new Date();
        dateTextView.setText(currentDate.toString());
    }

    /**
     * @TODO should be automatic saving.
     * Save the new note when the "Save" button is clicked
     * @param view The button view that triggers the save operation
     */
    public void saveNote(View view) {
        String title = String.valueOf(titleEditText.getText());
        String desc = String.valueOf(descriptionEditText.getText());

        // to return nothing if fields are empty
        if (title.isEmpty() || desc.isEmpty()) {
            return;
        }

        // create a new note object and save using the ViewModel
        Note newNote = new Note(title, desc, DateUtils.DateToString(currentDate));
        noteViewModel.createNote(newNote);

        // Finish the activity (close it) and return to the previous screen
        finish();
    }
}
