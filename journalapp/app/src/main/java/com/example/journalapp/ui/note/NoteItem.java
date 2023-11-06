package com.example.journalapp.ui.note;

import android.net.Uri;
import android.util.Log;

import java.util.UUID;

/**
 * Data model to represent items inside a Note
 * Think of this as the content inside a Note
 */
public class NoteItem {

    // Enum to define types of items that can exist within a note
    public enum ItemType{
        TEXT,
        IMAGE
    }
    private String itemId; // unique identifier
    private ItemType type; // type of the note item (Enum)
    private String content; // can store either text or string representation of the URI

    private int orderIndex; // keep track of order in a list

    /**
     * Constructor for creating a new NoteItem
     * @param type
     * @param itemId
     * @param content
     * @param orderIndex
     */
    public NoteItem(ItemType type, String itemId, String content, int orderIndex){
        this.type = type;
        if (itemId == null) {
            this.itemId = UUID.randomUUID().toString(); // Generate a new ID only if none is provided
        } else {
            this.itemId = itemId;
        }
        this.content = content;
        this.orderIndex = orderIndex;
        Log.e("Note created", "itemId = " + itemId);
    }

    /**
     * Getter for the note type
     * @return
     */
    public ItemType getType() {
        return type;
    }

    /**
     * Setter for the note type
     * @param type
     */
    public void setType(ItemType type) {
        this.type = type;
    }

    /**
     * Getter for the item id
     * @return
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Setter for the item id
     * @param itemId
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /**
     * Getter for item content (EditTexts only)
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * Setter for item content (EditTexts only)
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Getter for item content as an ImageURI
     * @return
     */
    public Uri getContentImageUri(){
        if (this.type == ItemType.IMAGE && this.content != null){
            return Uri.parse(this.content);
        }
        return null;
    }

    /**
     * Setter for item content from URI to string
     * @param imageUri
     */
    public void setContentImageUri(Uri imageUri){
        if (imageUri != null){
            this.content = imageUri.toString();
        }
        else{
            this.content = null;
        }
    }

    /**
     * Getter for order index
     * @return
     */
    public int getOrderIndex() {
        return orderIndex;
    }

    /**
     * Setter for order index
     * @param orderIndex
     */
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}