package com.blueridgebinary.terra;

import android.content.ContentValues;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blueridgebinary.terra.data.TerraDbContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestDB extends AppCompatActivity {

  /*  EditText etEnterSessionName;
    EditText etEnterSessionNotes;
    Button btnCreateNewSession;
    Button btnQuerySessions;
    TextView tvDisplayQueryResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        etEnterSessionName = (EditText) findViewById(R.id.et_enter_session_name);
        etEnterSessionNotes = (EditText) findViewById(R.id.et_enter_session_notes);
        btnCreateNewSession = (Button) findViewById(R.id.bt_create_session);
        tvDisplayQueryResults = (TextView) findViewById(R.id.tv_query_results);

    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    // Methods for Button Clicks

    public void onClickCreateSession(View v) {
        String inputName = etEnterSessionName.getText().toString();
        String inputNotes = etEnterSessionNotes.getText().toString();
        String currentDatetime = getDateTime();

        if (inputName.length() == 0) return;

        ContentValues contentValues = new ContentValues();
        contentValues.put(TerraDbContract.SessionEntry.COLUMN_SESSIONNAME, inputName);
        contentValues.put(TerraDbContract.SessionEntry.COLUMN_NOTES, inputNotes);
        contentValues.put(TerraDbContract.SessionEntry.COLUMN_CREATED, currentDatetime);
        contentValues.put(TerraDbContract.SessionEntry.COLUMN_UPDATED, currentDatetime);

        // Insert the content values via a ContentResolver
        Uri uri = getContentResolver().insert(TerraDbContract.SessionEntry.CONTENT_URI, contentValues);

        // Display the URI that's returned with a Toast
        // [Hint] Don't forget to call finish() to return to MainActivity after this insert is complete
        if (uri != null) {
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
        }

    }

*//*    public void onClickQuerySessions(View v) {
        String inputName = etEnterSessionName.getText().toString();
        String inputNotes = etEnterSessionNotes.getText().toString();
        String currentDatetime = getDateTime();

        getContentResolver().query(TerraDbContract.SessionEntry.CONTENT_URI)
        // Insert the content values via a ContentResolver
        Uri uri = getContentResolver().insert(TerraDbContract.SessionEntry.CONTENT_URI, contentValues);

    }*//*

    // TODO: implement loader for querying DB then use that in the main activity for pulling data
    // the insert method seems to be working fine though :)*/
}