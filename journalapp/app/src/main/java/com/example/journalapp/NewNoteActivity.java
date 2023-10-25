package com.example.journalapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.journalapp.note.Note;
import com.example.journalapp.note.NoteRepository;
import com.example.journalapp.utils.DateUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class NewNoteActivity extends AppCompatActivity {
    private TextView dateTextView;
    private NoteRepository noteRepository;
    private Note note;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_note);
        initWidgets();
        initOptionsMenu();
        setNote();
    }

    private void initWidgets() {
        dateTextView = findViewById(R.id.dateTextView);
        EditText titleEditText = findViewById(R.id.titleEditText);
        EditText descriptionEditText = findViewById(R.id.descriptionEditText);
        noteRepository = NoteRepository.getInstance(getApplication());
        Observable<String> titleChangedObservable = Observable.create(emitter -> titleEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emitter.onNext(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        }));

        /* 5 seconds */
        int SAVE_DELAY = 1000;
        Observable<String> titleObservable = titleChangedObservable
                .debounce(SAVE_DELAY, TimeUnit.MILLISECONDS);

        Observable<String> descriptionChangedObservable = Observable.create(emitter -> descriptionEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emitter.onNext(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        }));

        Observable<String> descriptionObservable = descriptionChangedObservable
                .debounce(SAVE_DELAY, TimeUnit.MILLISECONDS);

        compositeDisposable.addAll(
                descriptionObservable.subscribe(this::saveNoteDescription),
                titleObservable.subscribe(this::saveNoteTitle));
    }

    private void initOptionsMenu() {
        findViewById(R.id.optionsMenu).setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.getMenuInflater().inflate(R.menu.journal_options_menu, popupMenu.getMenu());
            try {
                popupMenu.getClass().getDeclaredMethod("setForceShowIcon", boolean.class)
                        .invoke(popupMenu, true);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            } finally {
                popupMenu.show();
            }

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                /* Don't ask why it's not a switch statement, it's just not. */
                if (menuItem.getItemId() == R.id.item1a) {
                    Toast.makeText(getApplicationContext(), "Take Photo/Video", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.item1b) {
                    Toast.makeText(getApplicationContext(), "Add Photo/Video From Library", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.item2) {
                    Toast.makeText(getApplicationContext(), "Add Voice Note", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.item3) {
                    Toast.makeText(getApplicationContext(), "Insert", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.item4) {
                    Toast.makeText(getApplicationContext(), "Save Note", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.item5) {
                    Toast.makeText(getApplicationContext(), "Add Template", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return true;
            });
        });
    }

    private void setNote() {
        Date currentDate = new Date();
        String dateString = DateUtils.DateToString(currentDate);
        note = new Note("", "", currentDate.toString());
        dateTextView.setText(dateString.split(" ")[0]);
        noteRepository.insertNote(note);
    }

    public void saveNoteTitle(String title) {
        Log.d("TextWatcher", "Updating the title: " + title);
        note.setTitle(title);
        noteRepository.updateNoteTitle(note);
    }

    public void saveNoteDescription(String description) {
        Log.d("TextWatcher", "Updating the description: " + description);
        note.setDescription(description);
        noteRepository.updateNoteDescription(note);
    }

    public void exitNote(View view) {
        String description = note.getDescription();
        String title = note.getTitle();
        if (description.isEmpty() && title.isEmpty()) {
            noteRepository.deleteNote(note);
        }
        finish();
    }
}
