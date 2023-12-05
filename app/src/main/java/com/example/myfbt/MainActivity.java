package com.example.myfbt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText noteField, editTextDescription;
    Button addbtnn, loadbtn;

    ListView listviewfb;

    TextView viewtext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteField = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
//        viewtext = findViewById(R.id.textView);
        loadbtn = findViewById(R.id.loadbtn);
        listviewfb = findViewById(R.id.listviewfb);

        addbtnn = findViewById(R.id.addbtn);

        addbtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = noteField.getText().toString();
                String description = editTextDescription.getText().toString();

                Map<String, Object> note = new HashMap<>();
                note.put(KEY_TITLE, title);
                note.put(KEY_DESCRIPTION, description);

                db.collection("note")
                        .add(note)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                Toast.makeText(MainActivity.this, "succes", Toast.LENGTH_SHORT);
                                noteField.setText("");
                                editTextDescription.setText("");

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
            }
        });

        loadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("note").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    List<Note> notes = new ArrayList<>();

                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        String title = document.getString("title");
                                        String description = document.getString("description");
                                        System.out.println(document);
//                                        viewtext.setText(document.toString());

//                                        notes.add("Title: " + title + "\nContent: " + description);
                                        Note note = new Note(title, description);
                                        notes.add(note);

//                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.item_note, notes);

//                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.item_note, R.id.textTitle, notes);

                                    }
//                                    ArrayAdapter<Note> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.item_note, R.id.textTitle, notes);
                                    ArrayAdapter<Note> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.item_note,R.id.textTitle, notes);


                                    listviewfb.setAdapter(adapter);

                                    listviewfb.setOnItemClickListener((parent, view, position, id) -> {
                                        Note selectedNote = notes.get(position);
                                        showUpdateDeleteDialog(selectedNote);
                                    });
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });
            }
        });



    }

    private void showUpdateDeleteDialog(Note selectedNote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an action");


        builder.setPositiveButton("Update", (dialog, which) -> {
            showUpdateDialog(selectedNote);
//            Toast.makeText(MainActivity.this, "Update clicked for " + selectedNote.getTitle(), Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            deleteNote(selectedNote);
//            Toast.makeText(MainActivity.this, "Delete clicked for " + selectedNote.getTitle(), Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> {

        });

        builder.show();
    }
    private void updateNote(Note selectedNote, String newTitle, String newDescription) {
        // Get the reference to the note in Firestore using its title
        db.collection("note")
                .whereEqualTo("title", selectedNote.getTitle())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Update the note in Firestore
                            db.collection("note")
                                    .document(document.getId())
                                    .update("title", newTitle, "description", newDescription)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "Note updated successfully", Toast.LENGTH_SHORT).show();

//                                        loadNotes();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error updating note", e);
                                        Toast.makeText(MainActivity.this, "Failed to update note", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(MainActivity.this, "Failed to update note", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showUpdateDialog(Note selectedNote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Note");

        // Set up the layout for the dialog
        View view = getLayoutInflater().inflate(R.layout.dialog_update, null);
        builder.setView(view);

        EditText editTextTitle = view.findViewById(R.id.editTextTitle);
        EditText editTextDescription = view.findViewById(R.id.editTextDescription);

        editTextTitle.setText(selectedNote.getTitle());
        editTextDescription.setText(selectedNote.getDescription());

        builder.setPositiveButton("Update", (dialog, which) -> {
            // Handle the update action
            String newTitle = editTextTitle.getText().toString();
            String newDescription = editTextDescription.getText().toString();

            updateNote(selectedNote, newTitle, newDescription);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Do nothing or dismiss the dialog
        });

        builder.show();
    }

    private void deleteNote(Note note) {
        // Get the reference to the note in Firestore using its title
        db.collection("note")
                .whereEqualTo("title", note.getTitle())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete the note from Firestore
                            db.collection("note")
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "Note deleted successfully", Toast.LENGTH_SHORT).show();
                                        // Refresh the list after deletion
//                                        loadNotes();
                                        db.collection("note").get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            List<Note> notes = new ArrayList<>();

                                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                                Log.d(TAG, document.getId() + " => " + document.getData());
                                                                String title = document.getString("title");
                                                                String description = document.getString("description");
                                                                System.out.println(document);
                                                                Note note = new Note(title, description);
                                                                notes.add(note);


                                                            }
                                                            ArrayAdapter<Note> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.item_note,R.id.textTitle, notes);


                                                            listviewfb.setAdapter(adapter);

                                                            listviewfb.setOnItemClickListener((parent, view, position, id) -> {
                                                                Note selectedNote = notes.get(position);
                                                                showUpdateDeleteDialog(selectedNote);
                                                            });
                                                        } else {
                                                            Log.w(TAG, "Error getting documents.", task.getException());
                                                        }
                                                    }
                                                });

                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error deleting note", e);
                                        Toast.makeText(MainActivity.this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(MainActivity.this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}