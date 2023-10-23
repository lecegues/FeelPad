package com.example.journalapp.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dateFormat =
            new SimpleDateFormat("MM-dd-yyyy HH:mmLss");

    private DateUtils() {
    }

    public static String DateToString(Date date) {
        return dateFormat.format(date);
    }

    public static Date StringToDate(String string) {
        try {
            return dateFormat.parse(string);
        } catch (ParseException e) {
            return null;
        }
    }
}
