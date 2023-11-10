package com.example.journalapp;

public class ThemeManager {
    private static int currentTheme;

    public static int getCurrentTheme() {
        return currentTheme;
    }

    public static void setCurrentTheme(int theme) {
        currentTheme = theme;
    }
}
