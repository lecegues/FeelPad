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
    private String content; // for text
    private Uri imageUri; // for images
    private int orderIndex; // keep track of order in a list

    /**
     * Constructor for creating a new NoteItem
     * @param type
     * @param itemId
     * @param content
     * @param imageUri
     * @param orderIndex
     */
    public NoteItem(ItemType type, String itemId, String content, Uri imageUri, int orderIndex){
        this.type = type;
        if (itemId == null) {
            this.itemId = UUID.randomUUID().toString(); // Generate a new ID only if none is provided
        } else {
            this.itemId = itemId;
        }
        this.content = content;
        this.imageUri = imageUri;
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
     * Getter for imageUri (Images only)
     * @return
     */
    public Uri getImageUri() {
        return imageUri;
    }

    /**
     * Setter for imageUri (images only)
     * @param imageUri
     */
    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
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
