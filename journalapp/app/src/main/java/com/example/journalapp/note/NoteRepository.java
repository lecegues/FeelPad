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

    private static NoteRepository instance;
    private final NoteDao noteDao;

    /**
     * Constructor for creating a new NoteRepository
     *
     * @param application Application context
     */
    public NoteRepository(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getNoteDatabase(application);

        // Initialize the variables
        noteDao = noteDatabase.noteDao();
    }

    /**
     * Get singleton instance of NoteRepository
     *
     * @param application The application.
     * @return A synchronized instance of the NoteRepository
     */
    public static synchronized NoteRepository getInstance(Application application) {
        if (instance == null) {
            instance = new NoteRepository(application);
        }
        return instance;
    }

    /**
     * Inserts a note into the database
     *
     * @param note Note object to be inserted
     */
    public void insertNote(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.insertNote(note));
    }

    /**
     * Update note title
     *
     * @param note The Note to be updated
     */
    public void updateNoteTitle(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.updateNoteTitle(note.getTitle(), note.getId()));
    }

    /**
     * Update note description
     *
     * @param note The Note to be updated
     */
    public void updateNoteDescription(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.updateNoteDescription(note.getDescription(), note.getId()));
    }

    /**
     * Retrieve all notes using LiveData list of notes. Live
     * data list of notes descending order by date.
     *
     * @return LiveData object containing a list of notes
     */
    public LiveData<List<Note>> getAllNotesOrderedByCreatedDateDesc() {
        return noteDao.getAllNotesOrderByCreatedDateDesc();
    }

    /**
     * Retrieve all note containing the provided string in their title, description, or date,
     * using LiveData list of notes.
     *
     * @param string The string to search for
     * @return LiveData object containing the list of notes
     */
    public LiveData<List<Note>> getAllNoteWhereTitleDateDescContains(String string) {
        return noteDao.getAllNotesWhereTitleDateDescContains(string);
    }

    /**
     * Get a note by id
     *
     * @param note_id The note id
     * @return The note with note id
     */
    public Note getNoteById(String note_id) {
        return noteDao.getNoteById(note_id);
    }

    /**
     * Delete a note from the database
     *
     * @param note The note to be removed
     */
    public void deleteNote(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> noteDao.deleteNote(note));
    }
}
