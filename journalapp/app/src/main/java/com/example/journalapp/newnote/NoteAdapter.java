package com.example.journalapp.newnote;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;

import java.util.List;

/**
 * Adapter for the contents inside the Note
 * Remember: Converts data in a format that looks proper
 */
public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<NoteItem> noteItems;

    /**
     * Constructor
     * @param noteItems
     */
    public NoteAdapter(List<NoteItem> noteItems){
        this.noteItems = noteItems;
    }

    @Override
    public int getItemViewType(int position) {
        // Return the view type of the item at position for correct viewholder binding
        return noteItems.get(position).getType().ordinal();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the appropriate layout according to view type

        // if text
        if (viewType == NoteItem.ItemType.TEXT.ordinal()){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(com.example.journalapp.R.layout.item_note_text, parent, false);
            return new TextViewHolder(view); // @TODO custom viewholder class
        }

        // if image
        else if (viewType == NoteItem.ItemType.IMAGE.ordinal()){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(com.example.journalapp.R.layout.item_note_image, parent, false);
            return new ImageViewHolder(view);
        }

        // otherwise
        else{
            Log.w("NoteRecyclerView", "Unknown View Type is trying to be displayed");
            throw new RuntimeException("Unknown view type: " + viewType);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NoteItem noteItem = noteItems.get(position);

        // Bind the data to the holder based on the item type
        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).bind(noteItem);
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(noteItem);
        }
    }


    @Override
    public int getItemCount() {
        return noteItems.size();
    }

    // ViewHolder for text content
    static class TextViewHolder extends RecyclerView.ViewHolder {
        // Define your text view holder components
        private EditText editText;

        public TextViewHolder(View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.edit_text_note_text);
        }

        public void bind(NoteItem noteItem) {
            // Assuming NoteItem has a method to get text content
            editText.setText(noteItem.getContent());
            // Add more binding logic if needed
        }
    }

    // ViewHolder for image content
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        // Define your image view holder components
        private ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_note_image);
        }

        public void bind(NoteItem noteItem) {
            /*
            // Assuming NoteItem has a method to get image URI or resource ID
            Uri imageUri = noteItem.getImageUri();
            if (imageUri != null) {
                // Use a library like Glide or Picasso to load the image
                Glide.with(itemView.getContext())
                        .load(imageUri)
                        .into(imageView);
            }
            // Add more binding logic if needed
            */
        }
    }
}
