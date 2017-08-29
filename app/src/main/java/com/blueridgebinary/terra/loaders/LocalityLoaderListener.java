package com.blueridgebinary.terra.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.fragments.HomeScreenFragment;

import java.lang.reflect.Array;

/**
 * Created by dorra on 8/29/2017.
 */

public class LocalityLoaderListener implements LoaderManager.LoaderCallbacks<Cursor> {

    final private static String TAG = LocalityLoaderListener.class.getSimpleName();

    private Integer mLocalityId;
    private HomeScreenFragment mContext;
    private Integer mSessionId;
    private boolean mIsSingleQuery;

    public LocalityLoaderListener(HomeScreenFragment homeScreenFragment,
        Integer sessionId,
        @Nullable Integer localityId) {

        mContext = homeScreenFragment;
        mSessionId = sessionId;
        mLocalityId = localityId;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri;
        if (mSessionId == 0) return null;

        if (mLocalityId == null) {
            baseUri = TerraDbContract.LocalityEntry.CONTENT_URI;
            mIsSingleQuery = false;
        } else {
            baseUri = Uri.withAppendedPath(TerraDbContract.LocalityEntry.CONTENT_URI,Uri.encode(mLocalityId.toString()));
            mIsSingleQuery = true;
        }

        String selectionStatement = TerraDbContract.LocalityEntry.COLUMN_SESSIONID + " = ?";
        String[] selectionArgs = new String[]{mSessionId.toString()};

        return new CursorLoader(mContext.getActivity(), baseUri, null,
                selectionStatement,selectionArgs,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Pass new data back to the HomeScreenFragment for it to do stuff with it
            Log.d(TAG,data.toString());
            mContext.handleNewLocalityData(data,mIsSingleQuery);


            // DEBUG ONLY
            String debugLocalityText;
            if (mLocalityId == null) {
                debugLocalityText = "null";
            }
            else {
                debugLocalityText = mLocalityId.toString();
            }
            Log.d(TAG,"Successfully Locality Loader for (SESSION/LOCALITY) (" + mSessionId.toString() +"/" + debugLocalityText+ ") with " + Integer.toString(data.getCount()) +" rows and passed it back to the fragment. :)");
        }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mContext.handleNewLocalityData(null,mIsSingleQuery);
    }
}
