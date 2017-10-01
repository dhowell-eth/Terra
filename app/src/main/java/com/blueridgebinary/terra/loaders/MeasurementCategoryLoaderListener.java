package com.blueridgebinary.terra.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.fragments.MeasurementCategoryUi;

/**
 * Created by dorra on 8/29/2017.
 */

public class MeasurementCategoryLoaderListener implements LoaderManager.LoaderCallbacks<Cursor> {

    final private static String TAG = MeasurementCategoryLoaderListener.class.getSimpleName();

    private Context mContext;
    private Integer mSessionId;
    private MeasurementCategoryUi mUi;

    public MeasurementCategoryLoaderListener(MeasurementCategoryUi ui,
                                  Context loaderContext,
                                  Integer sessionId) {

        mContext = loaderContext;
        mSessionId = sessionId;
        mUi = ui;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri;
        if (mSessionId == 0) return null;

        baseUri = TerraDbContract.MeasurementCategoryEntry.CONTENT_URI;
        // Loads data where the sessionId = this session ID or where it is null (to get default measurement categories which have no session ID)
        String selectionStatement = TerraDbContract.MeasurementCategoryEntry.COLUMN_SESSIONID + " = ? OR " +
                TerraDbContract.MeasurementCategoryEntry.COLUMN_SESSIONID + " IS NULL";
        String[] selectionArgs = new String[]{mSessionId.toString()};
        // Sorting results by category name
        String sortOrder = TerraDbContract.MeasurementCategoryEntry.COLUMN_NAME + " ASC";

        return new CursorLoader(mContext, baseUri, null,
                selectionStatement,selectionArgs,sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Pass new data back to the HomeScreenFragment for it to do stuff with it
        mUi.handleNewMeasurementCategoryData(data);


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // UI Should be able to handle situations where a null is returned
        mUi.handleNewMeasurementCategoryData(null);
    }
}
