package com.blueridgebinary.terra.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


import com.blueridgebinary.terra.data.TerraDbContract;


/**
 * Created by dorra on 8/22/2017.
 */

public class TerraDbContentProvider extends ContentProvider {


    public static final int SESSIONS = 100;
    public static final int SESSION_WITH_ID = 101;
    public static final int LOCALITIES = 200;
    public static final int LOCALITY_WITH_ID = 201;
    public static final int COMPASS_MEASUREMENTS = 300;
    public static final int COMPASS_MEASUREMENTS_WITH_ID = 301;
    public static final int PICTURES = 400;
    public static final int PICTURES_WITH_ID = 401;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    private TerraDbHelper mTerraDbHelper;


    // TODO: need to implement methods for working with the DB

    public static UriMatcher buildUriMatcher() {
        // This is a helper method for registering all of the table/row URIs with a matcher
        // to be used in the other methods below.
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Add Session URIs to the matcher
        uriMatcher.addURI(TerraDbContract.AUTHORITY,TerraDbContract.PATH_TBLSESSION, SESSIONS);
        uriMatcher.addURI(TerraDbContract.AUTHORITY,TerraDbContract.PATH_TBLSESSION + "/#",SESSION_WITH_ID);
        // Add Locality URIs
        uriMatcher.addURI(TerraDbContract.AUTHORITY,TerraDbContract.PATH_TBLLOCALITY, LOCALITIES);
        uriMatcher.addURI(TerraDbContract.AUTHORITY,TerraDbContract.PATH_TBLLOCALITY + "/#",LOCALITY_WITH_ID);
        // Add Compass Measurement URIs
        uriMatcher.addURI(TerraDbContract.AUTHORITY,TerraDbContract.PATH_TBLCOMPASSMEASUREMENT, COMPASS_MEASUREMENTS);
        uriMatcher.addURI(TerraDbContract.AUTHORITY,TerraDbContract.PATH_TBLCOMPASSMEASUREMENT + "/#",COMPASS_MEASUREMENTS_WITH_ID);
        // Add Picture Table URIs
        uriMatcher.addURI(TerraDbContract.AUTHORITY,TerraDbContract.PATH_TBLPICTURE, PICTURES);
        uriMatcher.addURI(TerraDbContract.AUTHORITY,TerraDbContract.PATH_TBLPICTURE + "/#",PICTURES_WITH_ID);
        return uriMatcher;
    }


    @Override
    public boolean onCreate() {
        Context context =  getContext();
        mTerraDbHelper = new TerraDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mTerraDbHelper.getReadableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        // Query for the tasks directory and write a default case
        String queryTableName;
        switch (match) {
            // Query for the tasks directory
            case SESSIONS:
                queryTableName = TerraDbContract.SessionEntry.TABLE_NAME;
                break;
            case LOCALITIES:
                queryTableName = TerraDbContract.LocalityEntry.TABLE_NAME;
                break;
            case COMPASS_MEASUREMENTS:
                queryTableName = TerraDbContract.CompassMeasurementEntry.TABLE_NAME;
                break;
            case PICTURES:
                queryTableName = TerraDbContract.CompassMeasurementEntry.TABLE_NAME;
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor =  db.query(queryTableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        // Return the desired Cursor
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mTerraDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case SESSIONS:
                // Create a new Session Entry

                Log.d("DB_DEBUGGING",values.toString());
                long id = db.insert(TerraDbContract.SessionEntry.TABLE_NAME,null,values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(TerraDbContract.SessionEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
        }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mTerraDbHelper.getWritableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        // Query for the tasks directory and write a default case
        int numRowsDeleted;
        switch (match) {
            // Query for the tasks directory
            case SESSIONS:
                numRowsDeleted = db.delete(TerraDbContract.SessionEntry.TABLE_NAME, "1", null);
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
