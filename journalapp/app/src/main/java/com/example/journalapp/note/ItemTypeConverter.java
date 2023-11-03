package com.example.journalapp.note;

import androidx.room.TypeConverter;

import com.example.journalapp.newnote.NoteItem;

/**
 * Type Converters
 */
public class ItemTypeConverter {

    @TypeConverter
    public static NoteItem.ItemType toItemType(int itemType){
        return NoteItem.ItemType.values()[itemType];
    }

    @TypeConverter
    public static int toInteger(NoteItem.ItemType itemType){
        return itemType.ordinal();
    }
}
