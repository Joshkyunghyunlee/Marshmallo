package com.joshkyunghyunlee.marshmallo;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

//Customized CursorAdapter to handle special case of a multi line note
public class NotesCursorAdapter extends CursorAdapter{
    public NotesCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.note_list_item, parent, false
        );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String noteTitle = cursor.getString(
                cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE));
        int pos = noteTitle.indexOf(10); //10 is the ASCII value of a linefeed character
        if(pos != -1){
            noteTitle = noteTitle.substring(0, pos) + "...";
        }

        TextView tv = (TextView) view.findViewById(R.id.tvNote);
        tv.setText(noteTitle);
    }
}
