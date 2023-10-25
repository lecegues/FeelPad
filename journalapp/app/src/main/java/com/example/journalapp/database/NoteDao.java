package com.example.journalapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.journalapp.note.Note;

import java.util.List;

/**
 * Data Access Object (DAO) for Room database operations
 */
@Dao
public interface NoteDao {

    /**
     * Retrieves all notes from the database
     *
     * @return LiveData list of all notes
     */
    @Query("SELECT * FROM note_table")
    LiveData<List<Note>> getAllNotes();

    /**
     * Retrieves all notes from the database ordered by created date
     * descending
     *
     * @return LiveData list of all notes descending order by date
     */
    @Query("SELECT * FROM note_table ORDER BY create_date DESC")
    LiveData<List<Note>> getAllNotesOrderByCreatedDateDesc();

    /**
     * Retrieves notes with a specific title from the database
     *
     * @param providedTitle String Title to search for
     * @return a LiveData list of notes with the specified title
     */
    @Query("SELECT * FROM note_table WHERE title = :providedTitle")
    LiveData<List<Note>> getNotesWithTitle(String providedTitle);

    /**
     * Retrieves a single note given the id
     * @param note_id String id to search for a note with
     * @return a LiveData Note object
     */
    @Query("SELECT * FROM note_table WHERE id = :note_id")
    Note getNoteById(String note_id);


    /**
     * Inserts a new note into the database
     *
     * @param note Note to insert
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNote(Note note);

    /**
     * Update the title of note with id
     *
     * @param noteId        The notes id
     * @param providedTitle the new title
     */
    @Query("UPDATE note_table SET title = :providedTitle WHERE id = :noteId")
    void updateNoteTitle(String providedTitle, String noteId);

    /**
     * Update the description of note with id
     *
     * @param noteId              the notes id
     * @param providedDescription The new description
     */
    @Query("UPDATE note_table SET description = :providedDescription WHERE id = :noteId")
    void updateNoteDescription(String providedDescription, String noteId);

    /**
     * Deletes a note from the database
     *
     * @param note Note to delete
     */
    @Delete
    void deleteNote(Note note);
}
