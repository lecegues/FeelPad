package com.example.journalapp.newnote;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
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

import org.w3c.dom.Text;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Adapter for managing the display of different types of items within a note.
 * It handles the creation and binding of view holders for text and image content.
 */
public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<NoteItem> noteItems;
    private OnNoteItemChangeListener onNoteItemChangeListener;

    // Interface for callback
    public interface OnNoteItemChangeListener {
        void onNoteItemContentChanged();
    }

    // Setter for the listener
    public void setOnNoteItemChangeListener(OnNoteItemChangeListener listener){
        this.onNoteItemChangeListener = listener;
    }

    /**
     * Constructs a NoteAdapter with a list of Noteitems
     * @param noteItems
     */
    public NoteAdapter(List<NoteItem> noteItems){
        this.noteItems = noteItems;
    }

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
            return new TextViewHolder(view, onNoteItemChangeListener); // @TODO custom viewholder class
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

    /**
     * Binds the data at the specified position into the corresponding viewHolder
     * @TODO not setup yet because Database cannot retrieve data properly yet
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
     * We want to delete the TextWatcher
     * @param holder The ViewHolder for the view being recycled
     */
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof TextViewHolder){
            ((TextViewHolder) holder).clearTextWatcher();
        }
    }

    @Override
    public int getItemCount() {
        return noteItems.size();
    }


    /**
     * ViewHolder for text content within a note
     */
    static class TextViewHolder extends RecyclerView.ViewHolder {
        // Define your text view holder components
        private EditText editText;
        private CompositeDisposable compositeDisposable = new CompositeDisposable();
        private static final long SAVE_DELAY = 1000; // Delay for 1 second before auto-saving
        private NoteItem currentNoteItem;
        private OnNoteItemChangeListener listener;

        /**
         * Constructs a TextViewHolder for text content
         * @param itemView
         */
        public TextViewHolder(View itemView, OnNoteItemChangeListener listener) {
            super(itemView);
            this.listener = listener;
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
            /* 5 seconds */
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

        public void saveNoteContents(String content){
            // Save to noteItem
            if (currentNoteItem != null){
                currentNoteItem.setContent(content);

                // notify the activity that content has changed
                if (listener != null){
                    listener.onNoteItemContentChanged();
                }
            }


        }

    }

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
