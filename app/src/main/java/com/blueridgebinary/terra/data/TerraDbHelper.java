package com.blueridgebinary.terra.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dorra on 8/22/2017.
 */

public class TerraDbHelper extends SQLiteOpenHelper {

    // The name of the database
    private static final String DATABASE_NAME = "TerraDb.db";
    // Populate list of tables to be used in database-wide operations (e.g. delete tables)
    private List<String> tableNames = new ArrayList<String>(Arrays.asList(
        TerraDbContract.PictureEntry.TABLE_NAME,
        TerraDbContract.CompassMeasurementEntry.TABLE_NAME,
        TerraDbContract.LocalityEntry.TABLE_NAME,
        TerraDbContract.SessionEntry.TABLE_NAME));

    // If you change the database schema, you must increment the database version
    private static final int VERSION = 4;

    // Constructor
    TerraDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    /**
     * Called when the tasks database is created for the first time.
     */

    // TODO: Need  to delete logging statements before deploy
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create Session table (careful to follow SQL formatting rules)
        final String CREATE_TABLE_SESSION = "CREATE TABLE "  + TerraDbContract.SessionEntry.TABLE_NAME + " (" +
                TerraDbContract.SessionEntry._ID                + " INTEGER PRIMARY KEY, " +
                TerraDbContract.SessionEntry.COLUMN_SESSIONNAME + " TEXT NOT NULL, " +
                TerraDbContract.SessionEntry.COLUMN_NOTES    + " TEXT, " +
                TerraDbContract.SessionEntry.COLUMN_CREATED + " DATETIME NOT NULL, " +
                TerraDbContract.SessionEntry.COLUMN_UPDATED + " DATETIME NOT NULL);";
        Log.d("DB_DEBUGGING",CREATE_TABLE_SESSION);
        db.execSQL(CREATE_TABLE_SESSION);
        // Create Locality Table
        final String CREATE_TABLE_LOCALITY = "CREATE TABLE "  + TerraDbContract.LocalityEntry.TABLE_NAME + " (" +
                TerraDbContract.LocalityEntry._ID                + " INTEGER PRIMARY KEY, " +
                TerraDbContract.LocalityEntry.COLUMN_SESSIONID + " INTEGER NOT NULL, " +
                TerraDbContract.LocalityEntry.COLUMN_LAT + " REAL, " +
                TerraDbContract.LocalityEntry.COLUMN_LONG + " REAL, " +
                TerraDbContract.LocalityEntry.COLUMN_GPSACCURACY + " INTEGER, " +
                TerraDbContract.LocalityEntry.COLUMN_ELEVATION + " REAL, " +
                TerraDbContract.LocalityEntry.COLUMN_ISMANUALMEASUREMENT + " INTEGER, " +
                TerraDbContract.LocalityEntry.COLUMN_NOTES    + " TEXT, " +
                TerraDbContract.LocalityEntry.COLUMN_CREATED + " DATETIME NOT NULL, " +
                TerraDbContract.LocalityEntry.COLUMN_UPDATED + " DATETIME NOT NULL);";
        Log.d("DB_DEBUGGING",CREATE_TABLE_LOCALITY);
        db.execSQL(CREATE_TABLE_LOCALITY);
        // Create compassMeasurement Table
        final String CREATE_TABLE_COMPASSMEASUREMENT = "CREATE TABLE "  + TerraDbContract.CompassMeasurementEntry.TABLE_NAME + " (" +
                TerraDbContract.CompassMeasurementEntry._ID                + " INTEGER PRIMARY KEY, " +
                TerraDbContract.CompassMeasurementEntry.COLUMN_LOCALITYID + " INTEGER NOT NULL, " +
                TerraDbContract.CompassMeasurementEntry.COLUMN_STRIKE + " REAL, " +
                TerraDbContract.CompassMeasurementEntry.COLUMN_DIP + " REAL, " +
                TerraDbContract.CompassMeasurementEntry.COLUMN_DIPDIRECTION + " REAL, " +
                TerraDbContract.CompassMeasurementEntry.COLUMN_ISMANUALMEASUREMENT + " INTEGER, " +
                TerraDbContract.CompassMeasurementEntry.COLUMN_COMPASSRELIABILITY + " INTEGER, " +
                TerraDbContract.CompassMeasurementEntry.COLUMN_MEASUREMENTTYPEID + " INTEGER, " +
                TerraDbContract.CompassMeasurementEntry.COLUMN_NOTES    + " TEXT, " +
                TerraDbContract.CompassMeasurementEntry.COLUMN_CREATED + " DATETIME NOT NULL, " +
                TerraDbContract.CompassMeasurementEntry.COLUMN_UPDATED + " DATETIME NOT NULL);";
        Log.d("DB_DEBUGGING",CREATE_TABLE_COMPASSMEASUREMENT);
        db.execSQL(CREATE_TABLE_COMPASSMEASUREMENT);
        // Create compassMeasurement Table
        final String CREATE_TABLE_PICTURE = "CREATE TABLE "  + TerraDbContract.PictureEntry.TABLE_NAME + " (" +
                TerraDbContract.PictureEntry._ID                + " INTEGER PRIMARY KEY, " +
                TerraDbContract.PictureEntry.COLUMN_LOCALITYID + " INTEGER NOT NULL, " +
                TerraDbContract.PictureEntry.COLUMN_FILEPATH + " TEXT, " +
                TerraDbContract.PictureEntry.COLUMN_HASORIENTATION + " INTEGER, " +
                TerraDbContract.PictureEntry.COLUMN_ORIENTATIONX + " REAL, " +
                TerraDbContract.PictureEntry.COLUMN_ORIENTATIONY + " REAL, " +
                TerraDbContract.PictureEntry.COLUMN_ORIENTATIONZ + " REAL, " +
                TerraDbContract.PictureEntry.COLUMN_NOTES    + " TEXT, " +
                TerraDbContract.PictureEntry.COLUMN_CREATED + " DATETIME NOT NULL, " +
                TerraDbContract.PictureEntry.COLUMN_UPDATED + " DATETIME NOT NULL);";
        Log.d("DB_DEBUGGING",CREATE_TABLE_COMPASSMEASUREMENT);
    }

    /**
     * This method discards the old table of data and calls onCreate to recreate a new one.
     * This only occurs when the version number for this database (DATABASE_VERSION) is incremented.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for( String tableName : tableNames) {
            Log.d("DB_DEBUGGING","DELETING TABLE: " + tableName);
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
        }
        onCreate(db);
    }
}
