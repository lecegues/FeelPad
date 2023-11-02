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

    public NoteItem(ItemType type, String content, Uri imageUri){
        this.type = type;
        this.content = content;
        this.imageUri = imageUri;
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


}
