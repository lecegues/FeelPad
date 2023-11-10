package com.example.journalapp;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.ImageButton;


public class ThemeSelectionActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_theme);
            selection();
        }
    private void selection() {
        ImageButton theme1 = findViewById(R.id.sky);
        ImageButton theme2 = findViewById(R.id.beach);
        ImageButton theme3 = findViewById(R.id.map);
        ImageButton theme4 = findViewById(R.id.idk);


        theme1.setOnClickListener(v -> {
            //Log.d("ThemeSelectionActivity", "Theme1 button clicked");
            noteTheme(R.drawable.background1);
        });
        theme2.setOnClickListener(v -> {
            //Log.d("ThemeSelectionActivity", "Theme2 button clicked");
            noteTheme(R.drawable.background2);
        });
        theme3.setOnClickListener(v -> {
            //Log.d("ThemeSelectionActivity", "Theme3 button clicked");
            noteTheme(R.drawable.background3);
        });
        theme4.setOnClickListener(v -> {
            //Log.d("ThemeSelectionActivity", "Theme4 button clicked");
            noteTheme(R.drawable.background4);
        });
    }

    private void noteTheme(int themeBackground){
        Intent intent = new Intent(ThemeSelectionActivity.this, NewNoteActivity.class);
        intent.putExtra("themeBackground", themeBackground);
        startActivity(intent);

    }
}


