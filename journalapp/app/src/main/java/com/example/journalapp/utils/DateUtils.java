package com.example.journalapp.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for working with date and time conversion
 */
public class DateUtils {

    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dateFormat =
            new SimpleDateFormat("MM-dd-yyyy HH:mmLss");

    private DateUtils() {
        // private constructor to prevent instantiation
    }

    /**
     * Converts a Date object to a String
     *
     * @param date Date object to be formatted
     * @return String representation representing the date
     */
    public static String DateToString(Date date) {
        return dateFormat.format(date);
    }

    /**
     * Converts a String to a Date object
     *
     * @param string String representation of the date
     * @return Date object parsed from input string
     * Null if parsing fails
     */
    public static Date StringToDate(String string) {
        try {
            return dateFormat.parse(string);
        } catch (ParseException e) {
            return null;
        }
    }
}
