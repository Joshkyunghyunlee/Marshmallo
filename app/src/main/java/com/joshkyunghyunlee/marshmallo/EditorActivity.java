package com.joshkyunghyunlee.marshmallo;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditorActivity extends AppCompatActivity {

    private String action;
    private EditText editorText;
    private EditText editorTitle;
    private String noteFilter;
    private String oldText;
    private String oldTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editorText = (EditText) findViewById(R.id.editText);
        editorTitle = (EditText) findViewById(R.id.editTitle);

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);

        if(uri == null){
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));
        }
        else{
            action = Intent.ACTION_EDIT;
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri,
                    DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
            oldTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE));
            editorText.setText(oldText);
            editorTitle.setText(oldTitle);
            editorText.requestFocus();
            editorTitle.requestFocus();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //method allows user to switch from landscape view to portrait.
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(action.equals(Intent.ACTION_EDIT)){
            getMenuInflater().inflate(R.menu.menu_editor, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_delete:
                deleteNote();
                break;
        }
        return super.onOptionsItemSelected(item);
     }

    private void deleteNote() {
        //Listener
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (id == DialogInterface.BUTTON_POSITIVE) {
                            getContentResolver().delete(NotesProvider.CONTENT_URI,
                                    noteFilter, null);
                            //Tell user what happened
                            Toast.makeText(EditorActivity.this, getString(R.string.note_deleted),
                                    Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            //go back to main activity & update displayed data
                            finish();
                        }
                    }
                };
        //Message prompt
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure_single))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
}

    private void finishEditing() {
        String newText = editorText.getText().toString().trim();
        String newTitle = editorTitle.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_INSERT:
                if(newText.length() == 0 && newTitle.length() == 0) {
                    setResult(RESULT_CANCELED);
                }
                else if(newText.length() !=0 && newTitle.length() == 0){
                    Toast.makeText(EditorActivity.this, R.string.save_failed, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED);
                }
                else {
                    Toast.makeText(EditorActivity.this, getString(R.string.note_saved),
                            Toast.LENGTH_SHORT).show();
                    insertNote(newText, newTitle);
                }
                break;
            case Intent.ACTION_EDIT:
                //if title is deleted or no change is made, return to original state
                if (newTitle.length() == 0 || (oldText.equals(newText) && oldTitle.equals(newTitle))){
                    setResult(RESULT_CANCELED);
                }
                else {
                    updateNote(newText, newTitle);
                }
                break;
        }
        finish();
    }

    //Updating the database
    private void updateNote(String noteText, String noteTitle) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText, String noteTitle) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle);

        //insert a row into database table
        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
}
