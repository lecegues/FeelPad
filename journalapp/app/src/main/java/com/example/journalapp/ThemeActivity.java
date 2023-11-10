package com.example.journalapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.journalapp.MainActivity;
import com.example.journalapp.R;

public class ThemeActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_theme);

            // Initialize ImageButtons for theme selection
            ImageButton theme1Button = findViewById(R.id.sky);
            ImageButton theme2Button = findViewById(R.id.beach);
            ImageButton theme3Button = findViewById(R.id.map);
            ImageButton theme4Button = findViewById(R.id.idk);

            // Set click listeners for each theme button
            theme1Button.setOnClickListener(v -> selectTheme("theme1"));
            theme2Button.setOnClickListener(v -> selectTheme("theme2"));
            theme3Button.setOnClickListener(v -> selectTheme("theme3"));
            theme4Button.setOnClickListener(v -> selectTheme("theme4"));
        }

    private void selectTheme(String selectedTheme) {
        int themeLayoutId = 0;

        // Determine the layout resource ID based on the selected theme
        if ("theme1".equals(selectedTheme)) {
            themeLayoutId = R.layout.activity_theme1;
        } else if ("theme2".equals(selectedTheme)) {
            themeLayoutId = R.layout.activity_theme2;}
//        } else if ("theme3".equals(selectedTheme)) {
//            themeLayoutId = R.layout.activity_theme3;
//        } else if ("theme4".equals(selectedTheme)) {
//            themeLayoutId = R.layout.activity_theme4;
//        }

        // Send the selected theme resource ID back to NewNoteActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_theme", themeLayoutId);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

}

