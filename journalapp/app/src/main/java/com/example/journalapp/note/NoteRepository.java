package com.example.journalapp.note;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.journalapp.database.NoteDao;
import com.example.journalapp.database.NoteDatabase;

import java.util.List;

public class NoteRepository {

    private final NoteDao noteDao;
    private final LiveData<List<Note>> allNotes;

    public NoteRepository(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getNoteDatabase(application);
        noteDao = noteDatabase.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public void insertNote(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.insertNote(note);
        });
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }
}
