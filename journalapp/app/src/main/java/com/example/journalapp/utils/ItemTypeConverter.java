package com.example.journalapp.utils;

import androidx.room.TypeConverter;

import com.example.journalapp.ui.note.NoteItem;

/**
 * Room Type Converters
 * (Room does not support complex data types, so we can convert them to types that SQLite understands)
 */
public class ItemTypeConverter {

    /**
     * Converts an integer back to the corresponding NoteItem.ItemType enum
     * Used when reading data from the database
     * @param itemType
     * @return
     */
    @TypeConverter // tells room that they should be used for converting between the database column type and entity field type
    public static NoteItem.ItemType toItemType(int itemType){
        return NoteItem.ItemType.values()[itemType];
    }

    /**
     * Converts a NoteItem.ItemType to its ordinal integer value.
     * Used when writing data to database
     * @param itemType
     * @return
     */
    @TypeConverter
    public static int toInteger(NoteItem.ItemType itemType){
        return itemType.ordinal();
    }
}
