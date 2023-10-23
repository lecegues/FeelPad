package com.example.journalapp;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.ImageButton;

        import androidx.appcompat.app.AppCompatActivity;
        import androidx.lifecycle.ViewModelProvider;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.example.journalapp.note.NoteViewModel;

public class MainActivity extends AppCompatActivity {

    private NoteListAdapter noteListAdapter;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setNoteRecyclerView();
        createNoteObserver();


         ImageButton arrowButton = findViewById(R.id.arrowdown);
         ImageButton combinePdfButton = findViewById(R.id.combinePDF);
         ImageButton addNoteButton = findViewById(R.id.addNote);
         ImageButton searchButton = findViewById(R.id.search);
         ImageButton templateButton = findViewById(R.id.template);

         arrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click for the arrow button here
            }
       });
         combinePdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click for the combine PDF button here
            }
         });
         addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click for the add note button here
                Intent intent = new Intent(MainActivity.this, NewNoteActivity.class);
                startActivity(intent);
            }
         });
         searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click for the search button here
            }
         });

         templateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click for the template button here
            }
         });
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