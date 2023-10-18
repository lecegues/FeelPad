package com.example.journalapp.note;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;

public class NoteViewHolder extends RecyclerView.ViewHolder {

    private final TextView noteViewTitle;
    private final TextView noteViewDescription;

    public NoteViewHolder(@NonNull View itemView) {
        super(itemView);
        noteViewTitle = itemView.findViewById(R.id.noteTitle);
        noteViewDescription = itemView.findViewById(R.id.noteDesc);
    }

    public void bind(String title, String description) {
        noteViewTitle.setText(title);
        noteViewDescription.setText(description);
    }

    public static NoteViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_cell, parent, false);
        return new NoteViewHolder(view);
    }

}
