package com.blueridgebinary.terra;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.utils.util;

public class CreateNewProjectActivity extends AppCompatActivity {

    EditText etProjectName;
    EditText etProjectDesc;
    Button btCreateProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_project);


        etProjectName = (EditText) findViewById(R.id.et_create_project_name);
        etProjectDesc = (EditText) findViewById(R.id.et_create_project_desciption);
        btCreateProject = (Button) findViewById(R.id.bt_create_project_finish);


        btCreateProject.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        String inputName = etProjectName.getText().toString();
                        String inputNotes = etProjectDesc.getText().toString();
                        String currentDatetime = util.getDateTime();

                        if (inputName.length() == 0) return;

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(TerraDbContract.SessionEntry.COLUMN_SESSIONNAME, inputName);
                        contentValues.put(TerraDbContract.SessionEntry.COLUMN_NOTES, inputNotes);
                        contentValues.put(TerraDbContract.SessionEntry.COLUMN_CREATED, currentDatetime);
                        contentValues.put(TerraDbContract.SessionEntry.COLUMN_UPDATED, currentDatetime);
                        // Insert the content values via a ContentResolver
                        Uri uri = getContentResolver().insert(TerraDbContract.SessionEntry.CONTENT_URI, contentValues);
                        // Get the ID of the new record
                        int newSessionId = Integer.parseInt(uri.getPathSegments().get(1));
                        // Start up the home activity for this new session
                        Intent intent = new Intent(CreateNewProjectActivity.this,com.blueridgebinary.terra.MainActivity.class);
                        intent.putExtra("sessionId", newSessionId);
                        startActivity(intent);
                        CreateNewProjectActivity.this.finish();
                    }
        });
    }
}
