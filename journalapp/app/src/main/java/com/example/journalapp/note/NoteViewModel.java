package com.example.journalapp.note;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private final NoteRepository noteRepository;
    private final LiveData<List<Note>> allNotes;

    public NoteViewModel(Application application) {
        super(application);
        noteRepository = new NoteRepository(application);
        allNotes = noteRepository.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void createNote(Note note) {
        noteRepository.insertNote(note);
    }
}
