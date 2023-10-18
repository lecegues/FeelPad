package com.example.journalapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.note.NoteViewModel;

public class MainActivity extends AppCompatActivity {

    private NoteListAdapter noteListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setNoteRecyclerView();
        createNoteObserver();
    }

    private void createNoteObserver() {
        NoteViewModel noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, notes -> {
            noteListAdapter.submitList(notes);
        });
    }

    private void setNoteRecyclerView() {
        RecyclerView noteRecycleView = findViewById(R.id.noteListView);
       noteListAdapter = new NoteListAdapter(new NoteListAdapter.NoteDiff());
       noteRecycleView.setAdapter(noteListAdapter);
       noteRecycleView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void newNote(View view) {
        Intent intent = new Intent(this, NewNoteActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNoteRecyclerView();
        setNoteRecyclerView();
    }

}