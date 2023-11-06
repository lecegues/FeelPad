package com.example.journalapp.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;
import java.util.UUID;

/**
 * An entity class used to create a Room database table called note_table
 * Represents an individual notes' metadata
 */
@Entity(tableName = "note_table")
public class Note {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "create_date")
    private String createdDate;

    /**
     * Constructor to create a new Note instance
     *
     * @param title       String representing title of the note
     * @param createdDate String representing when the note was created
     */
    public Note(String title, String createdDate) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.createdDate = createdDate;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createDate) {
        this.createdDate = createDate;
    }

    /**
     * Indicates if two notes are equal
     *
     * @param o The object to compare with this instance
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return id.equals(note.id) && Objects.equals(title, note.title) && Objects.equals(createdDate, note.createdDate);
    }
}