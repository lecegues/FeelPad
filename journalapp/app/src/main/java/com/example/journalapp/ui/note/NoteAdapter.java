package com.example.journalapp.ui.note;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalapp.R;
import com.example.journalapp.ui.note.NoteItem;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Adapter for managing the display of different types of items within a note.
 * It handles the creation and binding of view holders for text and image content.
 */
public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<NoteItem> noteItems; // noteItems passed from the NoteActivity
    private OnNoteItemChangeListener onNoteItemChangeListener; // listener to handle changes in note items

    /**
     * Constructs a NoteAdapter with a list of NoteItems
     * @param noteItems
     */
    public NoteAdapter(List<NoteItem> noteItems){
        this.noteItems = noteItems;
    }

    /**
     * Determines the type of view needed based on the position of the item
     * @param position position to query
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        // Return the view type of the item at position for correct View holder binding
        return noteItems.get(position).getType().ordinal();
    }

    /**
     * Inflates the appropriate ViewHolder for the given view type.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return a new Viewholder that holds a View for the given viewtype
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the appropriate layout according to view type

        // if text
        if (viewType == NoteItem.ItemType.TEXT.ordinal()){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(com.example.journalapp.R.layout.item_note_text, parent, false);
            return new TextViewHolder(view, onNoteItemChangeListener); // text listener
        }

        // if image
        else if (viewType == NoteItem.ItemType.IMAGE.ordinal()){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(com.example.journalapp.R.layout.item_note_image, parent, false);
            return new ImageViewHolder(view); // needs custom listener
        }

        // otherwise
        else{
            Log.w("NoteRecyclerView", "Unknown View Type is trying to be displayed");
            throw new RuntimeException("Unknown view type: " + viewType);
        }

    }

    /**
     * Replace the contents of a view
     * invoked by the layout manager
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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

    /**
     * Deals with views that are being recycled
     * We want to delete resources or listeners
     * @param holder The ViewHolder for the view being recycled
     */
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof TextViewHolder){
            ((TextViewHolder) holder).clearTextWatcher();
        }
    }

    /**
     * Returns the size of the dataset
     * inveoked by layout manager
     * @return
     */
    @Override
    public int getItemCount() {
        return noteItems.size();
    }

    /**
     * Interface implemented by activities or fragments that use this adapter.
     * Used to notify when a note item's content has changed
     */
    public interface OnNoteItemChangeListener {
        void onNoteItemContentChanged();
    }

    /**
     * Setter method to assign a lsitener for note item changes
     * @param listener
     */
    public void setOnNoteItemChangeListener(OnNoteItemChangeListener listener){
        this.onNoteItemChangeListener = listener;
    }

    // ==============================
    // REGION: Handle Text
    // ==============================

    /**
     * ViewHolder for text content within a note
     */
    static class TextViewHolder extends RecyclerView.ViewHolder {

        private EditText editText;
        private NoteItem currentNoteItem;

        // Autosave Variables
        private CompositeDisposable compositeDisposable = new CompositeDisposable();
        private static final long SAVE_DELAY = 1000; // Delay for 1 second before auto-saving
        private OnNoteItemChangeListener listener;


        /**
         * Constructs a TextViewHolder for text content
         * @param itemView
         */
        public TextViewHolder(View itemView, OnNoteItemChangeListener listener) {
            super(itemView);
            this.listener = listener; // to pass onto save function later
            editText = itemView.findViewById(R.id.edit_text_note_text);

        }

        /**
         * Binds text content from a NoteItem to the EditText
         * @param noteItem
         */
        public void bind(NoteItem noteItem) {
            currentNoteItem = noteItem;
            editText.setText(noteItem.getContent());

            // Dispose previous subscription if any
            compositeDisposable.clear();

            // Create an Observable for text changes
            Observable<String> titleChangedObservable = Observable.create(emitter -> editText.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // actions before text is changed
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    emitter.onNext(charSequence.toString()); // emitter is notified of an update
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    // actions after text is changed
                }
            }));

            // Sets debounce time (ms) for title changes
            /* 1 second */
            Observable<String> titleObservable = titleChangedObservable
                    .debounce(SAVE_DELAY, TimeUnit.MILLISECONDS);


            // Subscribe to observables to trigger a save to database
            compositeDisposable.addAll(
                    titleObservable.subscribe(this::saveNoteContents));
        }

        /**
         * Clear the TextWatcher from the TextViewHolder
         */
        public void clearTextWatcher(){
            if (compositeDisposable != null){
                compositeDisposable.clear();
            }
        }

        /**
         * Save contents to local noteItems list and notify listener of the change
         * @param content
         */
        public void saveNoteContents(String content){
            // Save to noteItem
            if (currentNoteItem != null){
                currentNoteItem.setContent(content);

                // notify the activity that content has changed to save to database
                if (listener != null){
                    listener.onNoteItemContentChanged();
                }
            }
        }
    }

    // ==============================
    // REGION: Handle Images
    // ==============================

    /**
     * ViewHolder for image content within a note
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        // Define your image view holder components
        private ImageView imageView;

        /**
         * Constructs an ImageViewHolder for the image content
         * @param itemView
         */
        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_note_image);
        }

        /**
         * Binds the image content from a NoteItem to the imageview
         * @param noteItem
         */
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
