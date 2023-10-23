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

public class NewNoteActivity extends AppCompatActivity {
    private EditText titleEditText, descriptionEditText;
    private TextView dateTextView;
    private NoteViewModel noteViewModel;
    private Date currentDate;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_note);
        initWidgets();
        initOptionsMenu();
        setCurrentDate();
    }

    private void initOptionsMenu() {
        findViewById(R.id.optionsMenu).setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.getMenuInflater().inflate(R.menu.journal_options_menu, popupMenu.getMenu());
            try {
                popupMenu.getClass().getDeclaredMethod("setForceShowIcon", boolean.class)
                        .invoke(popupMenu, true);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            } finally {
                popupMenu.show();
            }

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

    private void initWidgets() {
        dateTextView = findViewById(R.id.dateTextView);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
    }

    private void setCurrentDate() {
        // TODO: Possibly allow the user to update the date
        currentDate = new Date();
        dateTextView.setText(currentDate.toString());
    }

    public void saveNote(View view) {
        String title = String.valueOf(titleEditText.getText());
        String desc = String.valueOf(descriptionEditText.getText());
        if (title.isEmpty() || desc.isEmpty()) {
            return;
        }
        Note newNote = new Note(title, desc, DateUtils.DateToString(currentDate));
        noteViewModel.createNote(newNote);
        finish();
    }
}
