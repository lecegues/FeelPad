package com.example.journalapp.note;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.journalapp.database.NoteDao;
import com.example.journalapp.database.NoteDatabase;

import java.util.List;

/**
 * Repository class to manage interactions with database
 * Acts as a channel between data source and the ViewModel
 */
public class NoteRepository {

    private final NoteDao noteDao; // Database Access API to make it easier for database-repository interaction
    private final LiveData<List<Note>> allNotes;

    /**
     * Constructor for creating a new NoteRepository
     * @param application Application context
     */
    public NoteRepository(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getNoteDatabase(application);

        // Initialize the variables
        noteDao = noteDatabase.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    /**
     * Inserts a note into the database
     * @param note Note object to be inserted
     */
    public void insertNote(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.insertNote(note);
        });
    }

    /**
     * Retrieve all notes using LiveData list of notes
     * @return LiveData object containing a list of notes
     */
    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }
}
