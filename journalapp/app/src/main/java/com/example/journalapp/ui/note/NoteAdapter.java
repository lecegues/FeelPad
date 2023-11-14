package com.example.journalapp.ui.note;

import android.content.DialogInterface;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.journalapp.R;

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
    private OnItemFocusChangeListener onItemFocusChangeListener; // listener to handle item focus
    private OnKeyListener onKeyListener; // listener to handle key presses
    private int focusedItem = -1; // -1 means no focused item
    private Integer highlightedItem = -1; // -1 is invalid

    /**
     * Constructs a NoteAdapter with a list of NoteItems
     * @param noteItems
     */
    public NoteAdapter(List<NoteItem> noteItems){
        this.noteItems = noteItems;
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
            return new TextViewHolder(view, onNoteItemChangeListener, onItemFocusChangeListener, onKeyListener);
        }

        // if image
        else if (viewType == NoteItem.ItemType.IMAGE.ordinal()){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(com.example.journalapp.R.layout.item_note_image, parent, false);
            return new ImageViewHolder(view, onItemFocusChangeListener);
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
        boolean isHighlighted = position == highlightedItem;

        // Bind the data to the holder based on the item type
        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).bind(noteItem, isHighlighted);
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(noteItem, isHighlighted);
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
     * Returns the size of the dataset
     * inveoked by layout manager
     * @return
     */
    @Override
    public int getItemCount() {
        return noteItems.size();
    }

    /**
     * Gets the index of the currently focused item in the adapter.
     * @return
     */
    public int getCurrentCursorIndex(){
        return focusedItem;
    }

    /**
     * Setter method to assign a listener for note item changes
     * @param listener
     */
    public void setOnNoteItemChangeListener(OnNoteItemChangeListener listener){
        this.onNoteItemChangeListener = listener;
    }

    /**
     * Setter method to assign a listener for focused item in the adapter.
     * @param listener
     */
    public void setOnItemFocusChangeListener(OnItemFocusChangeListener listener){
        this.onItemFocusChangeListener = listener;
    }

    /**
     * Setter method to assign a listener for keys pressed
     * @param listener
     */
    public void setOnKeyListener(OnKeyListener listener){
        this.onKeyListener = listener;
    }

    /**
     * Highlights the item at the given position in the adapter
     * @param position int position of item to be highlighted
     * @TODO fix highlight UI
     */
    public void highlightItem(int position){

        // Check if there's already a highlighted item
        if (highlightedItem != null && highlightedItem != position){
            notifyItemChanged(highlightedItem); // notify for rebinding
        }

        highlightedItem = position; // set new highlight
        notifyItemChanged(position);
    }

    /**
     * Clears any highlighted items in the adapter
     */
    public void clearHighlights(){
        if (highlightedItem != null){
            int oldPosition = highlightedItem;
            highlightedItem = null;
            notifyItemChanged(oldPosition); // rebind previously highlighted item
        }
    }

    /**
     * Interface implemented by activities or fragments that use this adapter.
     * Used to notify when a note item's content has changed
     */
    public interface OnNoteItemChangeListener {
        void onNoteItemContentChanged();
    }

    /**
     * Interface implemented by activities or fragments that use this adapter.
     * Used to notify when the focus has changed
     */
    public interface OnItemFocusChangeListener {
        // methods indicate that its intended to handle two types of events
        void onItemFocusChange(int position, boolean hasFocus);
        void onItemLongClick(int position);
    }

    /**
     * Interface implemented by activities or fragments that use this adapter.
     * Used to notify when a key is pressed
     */
    public interface OnKeyListener {
        void onEnterKeyPressed(int position);
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

        // Auto save Variables
        private CompositeDisposable compositeDisposable = new CompositeDisposable();
        private static final long SAVE_DELAY = 1000; // Delay for 1 second before auto-saving

        // Listeners
        private OnNoteItemChangeListener noteItemChangeListener;
        private OnItemFocusChangeListener focusChangeListener;
        private OnKeyListener onKeyListener;


        /**
         * Constructs a TextviewHolder for content
         * @param itemView
         * @param noteItemChangeListener
         * @param focusChangeListener
         */
        public TextViewHolder(View itemView, OnNoteItemChangeListener noteItemChangeListener, OnItemFocusChangeListener focusChangeListener, OnKeyListener onKeyListener) {
            super(itemView);
            this.noteItemChangeListener = noteItemChangeListener; // to pass onto save function later
            this.focusChangeListener = focusChangeListener;
            this.onKeyListener = onKeyListener;
            editText = itemView.findViewById(R.id.edit_text_note_text);

            // Set up FocusChangeListener. Built-In Listener --> Custom Listener
            editText.setOnFocusChangeListener((view, hasFocus) -> { // Built-in listener
                if (hasFocus) {
                    // Update focusedItem in Activity
                    focusChangeListener.onItemFocusChange(getAdapterPosition(), true);

                }
                else{
                    focusChangeListener.onItemFocusChange(getAdapterPosition(), false);
                }
            });

            editText.setOnLongClickListener(view -> { // Built-in listener
                // call the long click method from the listener
                focusChangeListener.onItemLongClick(getAdapterPosition());
                return true;
            });
        }

        /**
         * Binds text content from a NoteItem to the EditText
         * @param noteItem
         */
        public void bind(NoteItem noteItem, boolean isHighlighted) {
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

            // Set the highlight if this ViewHolder needs to be highlighted
            int backgroundId = isHighlighted ? R.drawable.edit_text_background_highlight : R.drawable.edit_text_background;
            editText.setBackgroundResource(backgroundId);

            // OnKeyListener for enter key. @TODO not being used at the moment.
            editText.setOnKeyListener(new View.OnKeyListener(){

                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == keyEvent.KEYCODE_ENTER){
                        if (onKeyListener != null){
                            onKeyListener.onEnterKeyPressed(getAdapterPosition());
                        }
                        return true; // consume event
                    }
                    return false; // do not consume event
                }
            });
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
                if (noteItemChangeListener != null){
                    noteItemChangeListener.onNoteItemContentChanged();
                }
            }
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
         * Requests focus for the EditText in this ViewHolder
         */
        public void requestEditTextFocus(){
            editText.requestFocus();

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
        private OnItemFocusChangeListener focusChangeListener;

        /**
         * Constructs an ImageviewHolder for content
         * @param itemView
         * @param focusChangeListener
         */
        public ImageViewHolder(View itemView, OnItemFocusChangeListener focusChangeListener) {
            super(itemView);
            this.focusChangeListener = focusChangeListener;
            imageView = itemView.findViewById(R.id.image_view_note_image);


            // Set up FocusChangeListener (focus = touched)
            imageView.setFocusableInTouchMode(true);
            imageView.setOnClickListener(v -> { // Built-In Listener
                // when image is clicked, consider as focused
                focusChangeListener.onItemFocusChange(getAdapterPosition(), true);
            });

            imageView.setOnLongClickListener(v -> { // Built-In Listener
                // when image is held, call long click method
                focusChangeListener.onItemLongClick(getAdapterPosition());

                // also consider as focused
                focusChangeListener.onItemFocusChange(getAdapterPosition(), true);
                return true;
            });



        }

        /**
         * Binds the image content from a NoteItem to the imageview
         * @param noteItem
         */
        public void bind(NoteItem noteItem, boolean isHighlighted) {
            Uri imageUri = noteItem.getContentImageUri();
            if (imageUri != null){
                // Use Glide to load the iamge from the URI @TODO add .placeholders/error images
                Glide.with(itemView.getContext())
                        .load(imageUri)
                        .into(imageView);
            }

            // Set highlights
            int backgroundId = isHighlighted ? R.drawable.image_view_background_highlight : R.drawable.image_view_background;
            imageView.setBackgroundResource(backgroundId);
        }
    }
}
