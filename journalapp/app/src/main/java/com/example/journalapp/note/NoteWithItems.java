package com.example.journalapp.note;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * Links two Entity databases together: Note & NoteItemEntity
 */
public class NoteWithItems {
    @Embedded
    public Note note;

    @Relation(
            parentColumn = "id",
            entityColumn = "note_id"
    )
    public List<NoteItemEntity> items;

}
