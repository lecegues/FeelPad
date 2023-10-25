package com.example.journalapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.journalapp.note.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM note_table")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM note_table ORDER BY create_date DESC")
    LiveData<List<Note>> getAllNotesOrderByCreatedDateDesc();

    @Query("SELECT * FROM note_table WHERE title = :providedTitle")
    LiveData<List<Note>> getNotesWithTitle(String providedTitle);

    @Query("SELECT * FROM note_table WHERE id = :providedId")
    Note getNoteById(int providedId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertNote(Note note);

    @Query("UPDATE note_table SET title = :providedTitle WHERE id = :noteId")
    void updateNoteTitle(String providedTitle, String noteId);

    @Query("UPDATE note_table SET description = :providedDescription WHERE id = :noteId")
    void updateNoteDescription(String providedDescription, String noteId);

    @Delete
    void deleteNote(Note note);
}
