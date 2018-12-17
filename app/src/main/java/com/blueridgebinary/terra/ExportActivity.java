package com.blueridgebinary.terra;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blueridgebinary.terra.data.TerraDbContentProvider;
import com.blueridgebinary.terra.data.TerraDbContract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ExportActivity extends AppCompatActivity {

    final String TAG = ExportActivity.class.getSimpleName();

    private TextView mFolderText;
    private EditText mFilenameEditText;
    private Button mExportButton;

    private DocumentFile exportDirectory;
    private DocumentFile dataDirectory;

    final int EXPORT_FOLDER_REQUEST_CODE = 3217;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call Super Methods
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        // Get UI Elements
        mFolderText = (TextView) findViewById(R.id.tv_export_select_folder);
        mFilenameEditText = (EditText) findViewById(R.id.et_export_filename);
        mExportButton = (Button) findViewById(R.id.btn_export_ok);

        // Configure folder selection;
        mFolderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(Intent.createChooser(intent, "Choose a directory"), EXPORT_FOLDER_REQUEST_CODE);
                }
        });

        // Configure button click
        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean got_file = getAndProcessFile();
                // If we were able to create our output file(s)
                if (got_file) {
                    boolean is_exported = exportData();
                    // if exporting failed, try to delete our file (softly)
                    if (!is_exported) {
                        dataDirectory.delete(); // This never throws an exception
                    }
                    // Append our export flag to our intent and return to the calling activity
                    Intent resultData = new Intent();
                    resultData.putExtra("export_result", is_exported);
                    setResult(RESULT_OK, resultData);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case EXPORT_FOLDER_REQUEST_CODE:
                Uri treeUri = data.getData();
                DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
                exportDirectory = pickedDir;
                mFolderText.setText("Folder: " + pickedDir.getName());
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }





    private boolean getAndProcessFile() {
        // Checks input fields and attempts to create the export directory
        if (exportDirectory == null) {
            Toast.makeText(this, "Error: No folder selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mFilenameEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Error: No file name", Toast.LENGTH_SHORT).show();
        }

        String rawText = mFilenameEditText.getText().toString();

        // Only take text to the left of the first "."
        String[] rawParts = rawText.split("\\.");
        String dataDirName = rawParts[0];

        // See if our export dir already exists, if so, delete it.
        DocumentFile existingDir = exportDirectory.findFile(dataDirName);
        if (existingDir != null) {
            boolean deleteExistDirResult = existingDir.delete();
            Log.d(TAG, "getAndProcessFile: tried to delete existing dir" + Boolean.toString(deleteExistDirResult));
        }

        // Try to create output directory, if there is an error show a toast
        dataDirectory = exportDirectory.createDirectory(dataDirName);
        if (dataDirectory == null) {
            Toast.makeText(this, "Error: Invalid export file name", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private boolean exportData() {
        // For csv export, want to create a series of csv files
        // info.txt
        // Stations.csv
        // Measurements.csv
        // Project.kml/z (for future release)

        DocumentFile infoFile = dataDirectory.createFile("plain/txt","export_info.txt");
        DocumentFile stationFile = dataDirectory.createFile("plain/txt","stations.csv");
        DocumentFile measurementFile = dataDirectory.createFile("plain/txt","measurements.csv");

        // 1. ------- Get our project meta data and write it to our txt file
        Cursor projectCursor = getContentResolver().query(
                ContentUris.withAppendedId(TerraDbContract.SessionEntry.CONTENT_URI,
                        this.getIntent().getIntExtra("session_id",0)),
                    null,
                    null,
                    null,
                null);

        projectCursor.moveToFirst();

        String infoString = "Terra Project Export\n"+
                "Project: " + projectCursor.getString(projectCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_SESSIONNAME)) + "\n" +
                "Description: " + projectCursor.getString(projectCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_NOTES)) + "\n" +
                "Export Date: " + DateFormat.getDateTimeInstance().format(new Date()) + "\n";

        Log.d(TAG, "exportData: " + infoString);

        // 1. Write info file
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(infoFile.getUri());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(infoString);
            projectCursor.close();
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.d(TAG, "exportData: Couldn't find export_info.txt file.");
            return false;
        }

        // 2. ------- Get our Station meta data and write it to csv file
        String[] selectionArgs = new String[1];
        selectionArgs[0] = Integer.toString(this.getIntent().getIntExtra("session_id",0));
        Cursor localityCursor = getContentResolver().query(
                TerraDbContract.LocalityEntry.CONTENT_URI,
                null,
                TerraDbContract.LocalityEntry.COLUMN_SESSIONID + "= ?",
                selectionArgs,
                TerraDbContract.LocalityEntry._ID + " ASC");

        localityCursor.moveToFirst();

        // Need to store locality Ids for querying compass table
        String[] localityIds = new String[localityCursor.getCount()];

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(stationFile.getUri());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String rowText = "StationId,Lat_WGS84,Long_WGS84,GpsAccuracy_m,Elevation_m,Notes,Created,Updated\n";
            outputStreamWriter.write(rowText);
            for (int i=0; i<localityCursor.getCount(); i++) {
                rowText = "" +
                        Integer.toString(localityCursor.getInt(localityCursor.getColumnIndex(TerraDbContract.LocalityEntry._ID))) + "," +
                        Double.toString(localityCursor.getDouble(localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_LAT))) + "," +
                        Double.toString(localityCursor.getDouble(localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_LONG))) + "," +
                        Double.toString(localityCursor.getDouble(localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_GPSACCURACY))) + "," +
                        Double.toString(localityCursor.getDouble(localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_ELEVATION))) + "," +
                        "\"" + localityCursor.getString(localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_NOTES)) + "\"," +
                        localityCursor.getString(localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_CREATED)) + "," +
                        localityCursor.getString(localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_UPDATED)) + "\n";
                outputStreamWriter.append(rowText);
                localityIds[i] = Integer.toString(localityCursor.getInt(localityCursor.getColumnIndex(TerraDbContract.LocalityEntry._ID)));
                localityCursor.moveToNext();
            }
            localityCursor.close();
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.d(TAG, "exportData: Couldn't find locality csv file.");
            return false;
        }


        // 3. ------- Get our Measurement meta data and write it to csv file
        Cursor measurementCursor = getContentResolver().query(
                TerraDbContract.JoinedCompassEntry.CONTENT_URI,
                null,
                null,
                localityIds,
                TerraDbContract.CompassMeasurementEntry.COLUMN_LOCALITYID + " ASC, " + TerraDbContract.CompassMeasurementEntry._ID + " ASC");

        Log.d(TAG, "exportData: " + DatabaseUtils.dumpCursorToString(measurementCursor));
        measurementCursor.moveToFirst();

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(measurementFile.getUri());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String rowText = "MeasurementId,StationId,Strike,Dip,DipDirection,AndroidCompassReliability,MeasurementMode,MeasurementType,Created\n";
            outputStreamWriter.write(rowText);
            for (int i=0; i<measurementCursor.getCount(); i++) {
                rowText = "" +
                        Integer.toString(measurementCursor.getInt(measurementCursor.getColumnIndex("compassId"))) + "," +
                        Integer.toString(measurementCursor.getInt(measurementCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry.COLUMN_LOCALITYID))) + "," +
                        Double.toString(measurementCursor.getDouble(measurementCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry.COLUMN_STRIKE))) + "," +
                        Double.toString(measurementCursor.getDouble(measurementCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry.COLUMN_DIP)))  + "," +
                        Double.toString(measurementCursor.getDouble(measurementCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry.COLUMN_DIPDIRECTION)))  + "," +
                        measurementCursor.getString(measurementCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry.COLUMN_COMPASSRELIABILITY)) + "," +
                        measurementCursor.getString(measurementCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry.COLUMN_MEASUREMENTMODE)) + "," +
                        measurementCursor.getString(measurementCursor.getColumnIndex(TerraDbContract.MeasurementCategoryEntry.COLUMN_NAME)) + "," +
                        measurementCursor.getString(measurementCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry.COLUMN_CREATED)) + "\n";
                outputStreamWriter.append(rowText);
                measurementCursor.moveToNext();
            }
            measurementCursor.close();
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.d(TAG, "exportData: Couldn't find locality csv file.");
            return false;
        }

        return true;

    }


}
