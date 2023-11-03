package com.example.journalapp.newnote;

import android.net.Uri;

/**
 * Data model to represent items inside a Note
 * `NoteContent`
 */
public class NoteItem {

    // can represent multiple items
    public enum ItemType{
        TEXT,
        IMAGE
    }

    private ItemType type;
    private String content; // for text
    private Uri imageUri; // for images
    private int orderIndex; // keep track of order in a list

    public NoteItem(ItemType type, String content, Uri imageUri, int orderIndex){
        this.type = type;
        this.content = content;
        this.imageUri = imageUri;
        this.orderIndex = orderIndex;
    }
    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
