package com.blueridgebinary.terra.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dorra on 8/22/2017.
 */


// Table schema, etc. lie here

public class TerraDbContract {

    // General Contract Stuff

    public static final String AUTHORITY = "com.blueridgebinary.terra";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Table paths
    public static final String PATH_TBLSESSION = "session";
    public static final String PATH_TBLLOCALITY = "locality";
    public static final String PATH_TBLCOMPASSMEASUREMENT = "compassMeasurement";
    public static final String PATH_TBLPICTURE = "picture";
    public static final String PATH_TBLMEASUREMENTCATEGORY = "measurementCategory";

    //public static final String PATH_TBLNOTESCAN = "noteScan";
    // TODO: Add table for tracking user-defined map layers, etc.

    // Tables
    // ----------------------- tblSession-----------------------------
    public static final class SessionEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                TerraDbContract.BASE_CONTENT_URI.buildUpon().appendPath(PATH_TBLSESSION).build();
        public static final String TABLE_NAME = "tblSession";
        // columns
        //public static final String COLUMN_SESSIONID = "sessionId";
        public static final String COLUMN_SESSIONNAME = "sessionName";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_CREATED = "createdDatetime";
        public static final String COLUMN_UPDATED = "updatedDatetime";

    }

    // ----------------------- tblLocality-----------------------------
    public static final class LocalityEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                TerraDbContract.BASE_CONTENT_URI.buildUpon().appendPath(PATH_TBLLOCALITY).build();
        public static final String TABLE_NAME = "tblLocality";
        // columns
        public static final String COLUMN_SESSIONID = "sessionId";
        public static final String COLUMN_LAT = "latitude";
        public static final String COLUMN_LONG = "longitude";
        public static final String COLUMN_GPSACCURACY = "gpsAccuracy";
        public static final String COLUMN_ELEVATION = "elevation";
        public static final String COLUMN_ISMANUALMEASUREMENT = "isManualMeasurement";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_CREATED = "createdDatetime";
        public static final String COLUMN_UPDATED = "updatedDatetime";
    }

    // ----------------------- tblCompassMeasurement-----------------------------
    public static final class CompassMeasurementEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                TerraDbContract.BASE_CONTENT_URI.buildUpon().appendPath(PATH_TBLCOMPASSMEASUREMENT).build();
        public static final String TABLE_NAME = "tblCompassMeasurement";
        // columns
        public static final String COLUMN_LOCALITYID = "localityId";
        public static final String COLUMN_STRIKE = "strike";
        public static final String COLUMN_DIP = "dip";
        public static final String COLUMN_DIPDIRECTION = "dipDirection";
        public static final String COLUMN_ISMANUALMEASUREMENT = "isManualMeasurement";
        public static final String COLUMN_COMPASSRELIABILITY = "compassReliability";
        public static final String COLUMN_MEASUREMENTMODE = "measurementMode";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_CREATED = "createdDatetime";
        public static final String COLUMN_UPDATED = "updatedDatetime";
        public static final String COLUMN_MEASUREMENTCATEGORYID = "measurementCategoryId";
    }

    // ----------------------- tblPicture-----------------------------
    public static final class PictureEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                TerraDbContract.BASE_CONTENT_URI.buildUpon().appendPath(PATH_TBLPICTURE).build();
        public static final String TABLE_NAME = "tblPicture";
        // columns
        public static final String COLUMN_LOCALITYID = "localityId";
        public static final String COLUMN_FILEPATH = "filepath";
        public static final String COLUMN_HASORIENTATION = "hasOrientation";
        public static final String COLUMN_ORIENTATIONX = "orientationX";
        public static final String COLUMN_ORIENTATIONY = "orientationY";
        public static final String COLUMN_ORIENTATIONZ = "orientationZ";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_CREATED = "createdDatetime";
        public static final String COLUMN_UPDATED = "updatedDatetime";
    }
        // TODO: Maybe add DB structure for handling annotations

        // ----------------------- tblMeasurementCategory-----------------------------
        public static final class MeasurementCategoryEntry implements BaseColumns {
            public static final Uri CONTENT_URI =
                    TerraDbContract.BASE_CONTENT_URI.buildUpon().appendPath(PATH_TBLMEASUREMENTCATEGORY).build();
            public static final String TABLE_NAME = "tblMeasurementCategory";
            // columns
            public static final String COLUMN_SESSIONID = "sessionId";
            public static final String COLUMN_NAME = "name";
            public static final String COLUMN_NOTES = "notes";
            public static final String COLUMN_CREATED = "createdDatetime";
            public static final String COLUMN_UPDATED = "updatedDatetime";

    }
}