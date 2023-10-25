package com.example.journalapp.note;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.journalapp.database.NoteDao;
import com.example.journalapp.database.NoteDatabase;

import java.util.List;

public class NoteRepository {

    private static NoteRepository instance;
    private final NoteDao noteDao;

    public NoteRepository(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getNoteDatabase(application);
        noteDao = noteDatabase.noteDao();
    }

    public static synchronized NoteRepository getInstance(Application application) {
        if (instance == null) {
            instance = new NoteRepository(application);
        }
        return instance;
    }

    public void insertNote(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.insertNote(note));
    }

    public void updateNoteTitle(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.updateNoteTitle(note.getTitle(), note.getId()));
    }

    public void updateNoteDescription(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.updateNoteDescription(note.getDescription(), note.getId()));
    }

    public LiveData<List<Note>> getAllNotesOrderedByCreatedDateDesc() {
        return noteDao.getAllNotesOrderByCreatedDateDesc();
    }

    public void deleteNote(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.deleteNote(note));
    }
}
