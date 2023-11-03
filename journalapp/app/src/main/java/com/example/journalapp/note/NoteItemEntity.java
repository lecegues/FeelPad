package com.example.journalapp.note;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.util.UUID;

@Entity(tableName="note_items",
        foreignKeys = @ForeignKey(entity=Note.class,
                parentColumns = "id",
                childColumns = "note_id",
                onDelete = ForeignKey.CASCADE))
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
