package com.example.journalapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.journalapp.note.Note;
import com.example.journalapp.note.NoteViewModel;
import com.example.journalapp.utils.DateUtils;

import java.util.Date;

public class NewNoteActivity extends AppCompatActivity {
    private EditText titleEditText, descriptionEditText;
    private NoteViewModel noteViewModel;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_note);
        initWidgets();
    }

    private void initWidgets() {
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
    }

    public void saveNote(View view) {
        String title = String.valueOf(titleEditText.getText());
        String desc = String.valueOf(descriptionEditText.getText());
        if (title.isEmpty() || desc.isEmpty()) {
            return;
        }
        Note newNote = new Note(title, desc,
                DateUtils.DateToString(new Date()));
        noteViewModel.createNote(newNote);
        finish();
    }
}
