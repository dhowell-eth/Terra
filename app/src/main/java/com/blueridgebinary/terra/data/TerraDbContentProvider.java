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
import android.text.TextUtils;
import android.util.Log;


import com.blueridgebinary.terra.data.TerraDbContract;

import java.util.Arrays;
import java.util.Locale;


/**
 * Created by dorra on 8/22/2017.
 */

public class TerraDbContentProvider extends ContentProvider {

    public static final String TAG = TerraDbContentProvider.class.getSimpleName();

    public static final int SESSIONS = 100;
    public static final int SESSION_WITH_ID = 101;
    public static final int LOCALITIES = 200;
    public static final int LOCALITY_WITH_ID = 201;
    public static final int COMPASS_MEASUREMENTS = 300;
    public static final int COMPASS_MEASUREMENTS_WITH_ID = 301;
    public static final int PICTURES = 400;
    public static final int PICTURES_WITH_ID = 401;
    public static final int MEAS_CAT = 500;
    public static final int MEAS_CAT_WITH_ID = 501;

    public static final int COMPASS_JOINED_LOCALITIES_JOINED_MEAS_CAT = 600;
    public static final int COMPASS_JOINED_LOCALITIES_JOINED_MEAS_CAT_WITH_ID = 601;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    private TerraDbHelper mTerraDbHelper;


    // TODO: need to implement  update and delete methods

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
        // Add Measurement Category URIs
        uriMatcher.addURI(TerraDbContract.AUTHORITY,TerraDbContract.PATH_TBLMEASUREMENTCATEGORY, MEAS_CAT);
        uriMatcher.addURI(TerraDbContract.AUTHORITY,TerraDbContract.PATH_TBLMEASUREMENTCATEGORY + "/#",MEAS_CAT_WITH_ID);
        // Add joined compass tables
        uriMatcher.addURI(TerraDbContract.AUTHORITY,TerraDbContract.PATH_TBLCOMPASSMEASUREMENT + "/" + TerraDbContract.PATH_JOINEDCOMPASSMEASUREMENTS,
                COMPASS_JOINED_LOCALITIES_JOINED_MEAS_CAT);
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
        String rowId;
        switch (match) {
            // Query for the tasks directory
            case SESSIONS:
                queryTableName = TerraDbContract.SessionEntry.TABLE_NAME;
                break;
            case SESSION_WITH_ID:
                queryTableName = TerraDbContract.SessionEntry.TABLE_NAME;
                rowId = uri.getPathSegments().get(1);
                // Append id to where clause for query
                if (selection == null) {
                    selection = TerraDbContract.SessionEntry._ID + " = ?";
                } else {
                selection = selection + " AND " + TerraDbContract.SessionEntry._ID + " = ?";
                }
                if (selectionArgs != null) {
                    selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length+1);
                    selectionArgs[selectionArgs.length - 1] = rowId;
                }
                else {
                    selectionArgs = new String[] {rowId};
                }
                break;
            case LOCALITIES:
                queryTableName = TerraDbContract.LocalityEntry.TABLE_NAME;
                break;
            case LOCALITY_WITH_ID:
                queryTableName = TerraDbContract.LocalityEntry.TABLE_NAME;
                rowId = uri.getPathSegments().get(1);
                // Append id to where clause for query
                if (selection == null) {
                    selection = TerraDbContract.LocalityEntry._ID + " = ?";
                } else {
                    selection = selection + " AND " + TerraDbContract.LocalityEntry._ID + " = ?";
                }
                if (selectionArgs != null) {
                    selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length+1);
                    selectionArgs[selectionArgs.length - 1] = rowId;
                }
                else {
                    selectionArgs = new String[] {rowId};
                }
                break;
            case COMPASS_MEASUREMENTS:
                queryTableName = TerraDbContract.CompassMeasurementEntry.TABLE_NAME;
                break;
            case PICTURES:
                queryTableName = TerraDbContract.CompassMeasurementEntry.TABLE_NAME;
                break;
            case MEAS_CAT:
                queryTableName = TerraDbContract.MeasurementCategoryEntry.TABLE_NAME;
                break;
            case COMPASS_JOINED_LOCALITIES_JOINED_MEAS_CAT:
                String localityId = "";
                for (String selectArg : selectionArgs) {
                    localityId = localityId + selectArg + ",";
                }
                if(localityId.endsWith(",")) {
                    localityId = localityId.substring(0, localityId.length() - 1);
                }

                String tMain = TerraDbContract.CompassMeasurementEntry.TABLE_NAME;
                String tMainCol1 = TerraDbContract.CompassMeasurementEntry.COLUMN_LOCALITYID;
                String tMainCol2 = TerraDbContract.CompassMeasurementEntry.COLUMN_MEASUREMENTCATEGORYID;
                String tJoin1 = TerraDbContract.LocalityEntry.TABLE_NAME;
                String tJoin1Col = TerraDbContract.LocalityEntry._ID;
                String tJoin2 = TerraDbContract.MeasurementCategoryEntry.TABLE_NAME;
                String tJoinCol2 = TerraDbContract.MeasurementCategoryEntry._ID;
                String joinQuery = String.format(Locale.US,
                        "SELECT t1._id as compassId, t1.notes as compassNotes,t1.*,t2.*,t3.*" +
                        "FROM %s t1 " +
                        "JOIN %s t2 ON t1.%s = t2.%s " +
                        "JOIN %s t3 ON t1.%s = t3.%s " +
                        "WHERE t2._id IN (%s);",
                        tMain,tJoin1,tMainCol1,tJoin1Col,tJoin2,tMainCol2,tJoinCol2,localityId);
                /*Log.d(TAG,"Formatted a new join compass query: " + joinQuery);*/
                retCursor = db.rawQuery(joinQuery,null);
                // Notifications have to happen for all joined tables, so the observer is pointed at the base_content_uri
                retCursor.setNotificationUri(getContext().getContentResolver(), uri);
                Log.d(TAG, "query: set notifcation uri at " + uri);
                /*Log.d(TAG,"Queried a new join compass query: " + joinQuery);*/
                return retCursor;
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
        Uri contentUri;
        String tableName;
        switch (match) {
            case SESSIONS:
                tableName = TerraDbContract.SessionEntry.TABLE_NAME;
                contentUri = TerraDbContract.SessionEntry.CONTENT_URI;
                break;
            case LOCALITIES:
                tableName = TerraDbContract.LocalityEntry.TABLE_NAME;
                contentUri = TerraDbContract.LocalityEntry.CONTENT_URI;
                // Need to automatically populate the stationNumber field since it is incremented on a per-session basis
                String nextStationNumber = getNextStationNumber(values.getAsString(TerraDbContract.LocalityEntry.COLUMN_SESSIONID));
                values.put(TerraDbContract.LocalityEntry.COLUMN_STATIONNUMBER,nextStationNumber);
                break;
            case COMPASS_MEASUREMENTS:
                tableName = TerraDbContract.CompassMeasurementEntry.TABLE_NAME;
                contentUri = TerraDbContract.CompassMeasurementEntry.CONTENT_URI;
                break;
            case PICTURES:
                tableName = TerraDbContract.PictureEntry.TABLE_NAME;
                contentUri = TerraDbContract.PictureEntry.CONTENT_URI;
                break;
            case MEAS_CAT:
                tableName = TerraDbContract.MeasurementCategoryEntry.TABLE_NAME;
                contentUri = TerraDbContract.MeasurementCategoryEntry.CONTENT_URI;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        long id = db.insert(tableName,null,values);
        if (id > 0) {
            returnUri = ContentUris.withAppendedId(contentUri, id);
        } else {
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
        }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mTerraDbHelper.getWritableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);

        String idList = "";
        if (selectionArgs != null) {
            idList = TextUtils.join(",", selectionArgs);
        }

        // Query for the tasks directory and write a default case
        int numRowsDeleted = 0;
        switch (match) {
            // Query for the tasks directory
            case SESSIONS:
                numRowsDeleted = db.delete(TerraDbContract.SessionEntry.TABLE_NAME, "1", null);
                break;
            case SESSION_WITH_ID:
                String queryTableName = TerraDbContract.SessionEntry.TABLE_NAME;
                String rowId = uri.getPathSegments().get(1);

                // Need to delete all child tables first
                // 1. Compass Measurements
                String delCompassQuery = String.format(Locale.US,
                         "DELETE FROM %s WHERE %s in " +
                            "(SELECT c.%s AS %s FROM %s c " +
                            "JOIN %s l ON c.%s = l.%s " +
                            "JOIN %s s ON l.%s = s.%s " +
                            "WHERE s.%s = %s);",
                        TerraDbContract.CompassMeasurementEntry.TABLE_NAME,
                        TerraDbContract.CompassMeasurementEntry._ID,
                        TerraDbContract.CompassMeasurementEntry._ID,
                        TerraDbContract.CompassMeasurementEntry._ID,
                        TerraDbContract.CompassMeasurementEntry.TABLE_NAME,
                        TerraDbContract.LocalityEntry.TABLE_NAME,
                        TerraDbContract.CompassMeasurementEntry.COLUMN_LOCALITYID,
                        TerraDbContract.LocalityEntry._ID,
                        TerraDbContract.SessionEntry.TABLE_NAME,
                        TerraDbContract.LocalityEntry.COLUMN_SESSIONID,
                        TerraDbContract.SessionEntry._ID,
                        TerraDbContract.SessionEntry._ID,
                        rowId);
                Log.d(TAG, "delete: " + delCompassQuery);
                // 2. Localities
                String delLocalityQuery = String.format(Locale.US,
                        "DELETE FROM %s WHERE %s = %s;",
                        TerraDbContract.LocalityEntry.TABLE_NAME,
                        TerraDbContract.LocalityEntry.COLUMN_SESSIONID,
                        rowId);
                // 3. This Session
                String delSessionQuery = String.format(Locale.US,
                        "DELETE FROM %s WHERE %s = %s;",
                        TerraDbContract.SessionEntry.TABLE_NAME,
                        TerraDbContract.SessionEntry._ID,
                        rowId);
                // Execute SQL
                db.beginTransaction();
                db.execSQL(delCompassQuery);
                db.execSQL(delLocalityQuery);
                db.execSQL(delSessionQuery);
                db.setTransactionSuccessful();
                db.endTransaction();
                numRowsDeleted = 1;
                break;

            case LOCALITIES:
                Log.d(TAG,  "refreshAppBar: "+ Arrays.toString(selectionArgs));
                if (selectionArgs.length >= 1) {
                    Log.d(TAG,  "refreshAppBar: we have some selections to delete.");
                    // Delete relevant data in measurement table (linked as foreign keys)
                    String delete_comp_sql = "DELETE FROM " + TerraDbContract.CompassMeasurementEntry.TABLE_NAME +
                            " WHERE " + TerraDbContract.CompassMeasurementEntry.COLUMN_LOCALITYID + " IN (" +
                            idList + ");";
                    String delete_locality_sql = "DELETE FROM " + TerraDbContract.LocalityEntry.TABLE_NAME +
                            " WHERE " + TerraDbContract.LocalityEntry._ID + " IN (" +
                            idList + ");";
                    db.beginTransaction();
                    db.execSQL(delete_comp_sql);
                    db.execSQL(delete_locality_sql);
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    numRowsDeleted = selectionArgs.length;
                }
                break;
            case COMPASS_JOINED_LOCALITIES_JOINED_MEAS_CAT:
                Log.d(TAG,  "delete called in content provider for: "+ Arrays.toString(selectionArgs));
                if (selectionArgs.length >= 1) {
                    // Delete relevant data in measurement table (linked as foreign keys)
                    String delete_comp_sql = "DELETE FROM " + TerraDbContract.CompassMeasurementEntry.TABLE_NAME +
                            " WHERE " + TerraDbContract.CompassMeasurementEntry._ID + " IN (" +
                            idList + ");";
                    String query_comp_sql = "SELECT * FROM " + TerraDbContract.CompassMeasurementEntry.TABLE_NAME + ";";
                    Cursor testCursor = db.rawQuery(query_comp_sql,null);
                    String debugCompIds = "SELECT _ID FROM tblCompass : ";
                    testCursor.moveToFirst();
                    for (int i = 0; i < testCursor.getCount()-1; i++) {
                        debugCompIds = debugCompIds = debugCompIds + testCursor.getString(testCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry._ID)) + ",";
                        testCursor.moveToNext();
                    }
                    Log.d(TAG, "delete: " + debugCompIds);
                    db.beginTransaction();
                    db.execSQL(delete_comp_sql);
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    numRowsDeleted = 1;

                }
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            Log.d(TAG, "delete: Notifying change at " + uri);
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mTerraDbHelper.getWritableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);

        String tableName;
        String rowId;
        switch (match) {
            case LOCALITY_WITH_ID:
                tableName = TerraDbContract.LocalityEntry.TABLE_NAME;
                rowId = uri.getPathSegments().get(1);
                // Append id to where clause for query
                if (selection == null) {
                    selection = TerraDbContract.LocalityEntry._ID + " = ?";
                } else {
                    selection = selection + " AND " + TerraDbContract.LocalityEntry._ID + " = ?";
                }
                if (selectionArgs != null) {
                    selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length+1);
                    selectionArgs[selectionArgs.length - 1] = rowId;
                }
                else {
                    selectionArgs = new String[] {rowId};
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        int numRowsUpdated = db.update(tableName,values,selection,selectionArgs);
        if (numRowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private String getNextStationNumber(String sessionId) {
        final SQLiteDatabase db = mTerraDbHelper.getWritableDatabase();
        // Query the current highest station number for this session
        String sqlQuery = String.format(Locale.US,
                "SELECT %s FROM %s WHERE %s=%s ORDER BY %s desc LIMIT 1;",
                TerraDbContract.LocalityEntry.COLUMN_STATIONNUMBER,
                TerraDbContract.LocalityEntry.TABLE_NAME,
                TerraDbContract.LocalityEntry.COLUMN_SESSIONID,
                sessionId,
                TerraDbContract.LocalityEntry.COLUMN_STATIONNUMBER);

        db.beginTransaction();
        Cursor result = db.rawQuery(sqlQuery,null);
        db.endTransaction();

        String nextStationNumber;
        if (result.getCount() >= 1) {
            result.moveToFirst();
             nextStationNumber = Integer.toString(
                     result.getInt(result.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_STATIONNUMBER)) + 1);
        }
        else {
            nextStationNumber = "1";
        }
        return nextStationNumber;
    }




}
