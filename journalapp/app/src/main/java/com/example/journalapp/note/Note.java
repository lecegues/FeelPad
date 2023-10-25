package com.example.journalapp.note;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * An entity class used to create a Room database table
 * Represents a 'note' entity in the table
 */
@Entity(tableName = "note_table")
public class Note {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id; // unique identifier for the node
    @ColumnInfo(name = "title")
    private String title; // title of the note
    @ColumnInfo(name = "description")
    private String description; // content description of the note
    @ColumnInfo(name = "create_date")
    private String createDate; // date when note was created

    /**
     * Constructor to create a new Note instance
     * @param title String representing title of the note
     * @param description String representing the description of the note
     * @param createDate String representating when the note was created
     */
    public Note(String title, String description, String createDate) {
        this.title = title;
        this.description = description;
        this.createDate = createDate;
    }

    /**
     * Getter for unique ID of a note
     * @return integer ID of a note
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for unique ID of a note
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter for title of a note
     * @return String title of a note
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter for title of a note
     * @param title Title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter for description of the note
     * @return String description of a note
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for description of a note
     * @param description String description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for the date the note was created
     * @return String creation date of the note
     */
    public String getCreateDate() {
        return createDate;
    }

    /**
     * Setter to change the date when note was created
     * @param createDate
     */
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
