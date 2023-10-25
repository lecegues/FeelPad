package com.example.journalapp.note;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * ViewModel class - manages and provides access to note data
 * Basically responsible for sending data to data layer and retrieving data
 */
public class NoteViewModel extends AndroidViewModel {

    private final NoteRepository noteRepository;

    // Note: LiveData is designed to hold and observe data changes (inserts, deletes, modifications)
    private final LiveData<List<Note>> allNotes;

    /**
     * Constructor for creating a new NoteViewModel
     * @param application the application context
     */
    public NoteViewModel(Application application) {
        super(application);

        // Creates a new repository each time to manage connection to data layer
        noteRepository = new NoteRepository(application);

        // Creates LiveData to hold a list of notes
        allNotes = noteRepository.getAllNotes();
    }

    /**
     * Retrieve LiveData containing a list of all notes
     * @return LiveData object containing all notes
     */
    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    /**
     * Create and insert a new note into the data layer
     * @param note Note object to be inserted
     */
    public void createNote(Note note) {
        noteRepository.insertNote(note);
    }
}
