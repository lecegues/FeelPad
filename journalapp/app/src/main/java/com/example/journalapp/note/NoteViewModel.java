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

    /**
     * Constructor for creating a new NoteViewModel
     *
     * @param application the application context
     */
    public NoteViewModel(Application application) {
        super(application);
        noteRepository = NoteRepository.getInstance(application);
    }

    /**
     * Retrieve LiveData containing a list of all notes in descending order by date
     *
     * @return LiveData containing all notes
     */
    public LiveData<List<Note>> getAllNotesOrderedByCreateDateDesc() {
        return noteRepository.getAllNotesOrderedByCreatedDateDesc();
    }
}
