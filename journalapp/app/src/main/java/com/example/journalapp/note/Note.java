package com.example.journalapp.note;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;
import java.util.UUID;

/**
 * An entity class used to create a Room database table
 * Represents a 'note' entity in the table
 */
@Entity(tableName = "note_table")
public class Note {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "create_date")
    private String createdDate;

    /**
     * Constructor to create a new Note instance
     *
     * @param title       String representing title of the note
     * @param description String representing the description of the note
     * @param createdDate String representing when the note was created
     */
    public Note(String title, String description, String createdDate) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.createdDate = createdDate;
    }

    /**
     * Getter for unique ID of a note
     *
     * @return integer ID of a note
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * Setter for unique ID of a note
     *
     * @param id the ID to set
     */
    public void setId(@NonNull String id) {
        this.id = id;
    }

    /**
     * Getter for title of a note
     *
     * @return String title of a note
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter for title of a note
     *
     * @param title Title to set
     */
    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    /**
     * Getter for description of the note
     *
     * @return String description of a note
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for description of a note
     *
     * @param description String description to set
     */
    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    /**
     * Getter for the date the note was created
     *
     * @return String creation date of the note
     */
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Setter to change the date when note was created
     *
     * @param createDate The date that the note was created
     */
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
        return id.equals(note.id) && Objects.equals(title, note.title) && Objects.equals(description, note.description) && Objects.equals(createdDate, note.createdDate);
    }
}
