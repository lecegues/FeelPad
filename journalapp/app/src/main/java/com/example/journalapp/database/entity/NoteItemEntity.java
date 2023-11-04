package com.example.journalapp.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.journalapp.utils.ItemTypeConverter;

import java.util.UUID;

/**
 * An entity class used to create a Room database table called note_items
 * Represents an individual Notes` contents.
 * A one-to-many relationship between Notes and NoteItems.
 */
@Entity(tableName="note_items",
        foreignKeys = @ForeignKey(entity= Note.class,
                parentColumns = "id",
                childColumns = "note_id",
                onDelete = ForeignKey.CASCADE)) // Cascade will delete all related note items if its deleted
public class NoteItemEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name= "item_id")
    private String itemId;

    @ColumnInfo(name="note_id")
    private String noteId; // will link NoteItemEntity to a Note

    @ColumnInfo(name= "item_type")
    @TypeConverters(ItemTypeConverter.class) // tells room to use converter for this field
    private int type; // the ordinal of the enum

    @ColumnInfo(name="content")
    private String content; // Store text from editText or URI if image

    @ColumnInfo(name="order_index")
    private int orderIndex; // to maintain order of items within a note

    /**
     * Constructor to create a new NoteItemEntity
     * @param itemId String NoteItemEntity's ID. Can be passed from local NoteItem variable or generated automatically.
     * @param noteId String noteId that links every NoteItemEntity to a NoteEntity
     * @param type item type converter to an integer from an enum
     * @param content String content of the item -- depends on NoteItem type
     * @param orderIndex the order in which the noteItem belongs in the Recyclerview. Loaded in said order.
     */
    public NoteItemEntity(String itemId, String noteId, int type, String content, int orderIndex) {
        if (itemId == null) {
            this.itemId = UUID.randomUUID().toString(); // Generate a new ID only if none is provided
        } else {
            this.itemId = itemId;
        }
        this.noteId = noteId;
        this.type = type;
        this.content = content;
        this.orderIndex = orderIndex;
    }

    @NonNull
    public String getItemId() {
        return itemId;
    }

    public void setItemId(@NonNull String itemId) {
        this.itemId = itemId;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
