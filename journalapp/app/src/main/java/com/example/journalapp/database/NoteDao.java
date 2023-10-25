package com.example.journalapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.journalapp.note.Note;

import java.util.List;

/**
 * Data Access Object (DAO) for Room database operations
 */
@Dao
public interface NoteDao {

    /**
     * Retrieves all notes from the database
     * @return LiveData list of all notes
     */
    @Query("SELECT * FROM note_table")
    LiveData<List<Note>> getAllNotes();

    /**
     * Retrieves notes with a specific title from the database
     * @param providedTitle String Title to search for
     * @return a LiveData list of notes with the specified title
     */
    @Query("SELECT * FROM note_table WHERE title = :providedTitle")
    LiveData<List<Note>> getNotesWithTitle(String providedTitle);

    /**
     * Inserts a new note into the database
     * @param note Note to insert
     */
    @Insert
    void insertNote(Note note);

    /**
     * Updates an existing note in the database
     * @param note Note to update
     */
    @Update
    void updateNote(Note note);

    /**
     * Deletes a note from the database
     * @param note Note to delete
     */
    @Delete
    void deleteNote(Note note);
}
