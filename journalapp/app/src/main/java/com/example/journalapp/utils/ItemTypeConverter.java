package com.example.journalapp.utils;

import androidx.room.TypeConverter;

import com.example.journalapp.ui.note.NoteItem;

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
