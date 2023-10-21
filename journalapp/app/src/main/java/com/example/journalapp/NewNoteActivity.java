package com.example.journalapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.journalapp.note.Note;
import com.example.journalapp.note.NoteViewModel;
import com.example.journalapp.utils.DateUtils;

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
            popupMenu.show();
        });
        findViewById(R.id.infoMenu).setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.getMenuInflater().inflate(R.menu.journal_information_menu, popupMenu.getMenu());
            popupMenu.show();
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
