package com.example.journalapp.note;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private final NoteRepository noteRepository;

    public NoteViewModel(Application application) {
        super(application);
        noteRepository = NoteRepository.getInstance(application);
    }

    public LiveData<List<Note>> getAllNotesOrderedByCreateDateDesc() {
        return noteRepository.getAllNotesOrderedByCreatedDateDesc();
    }
}
