package com.blueridgebinary.terra.loaders;

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

/**
 * Created by dorra on 8/29/2017.
 */

public class SessionLoaderListener implements LoaderManager.LoaderCallbacks<Cursor> {

        final private static String TAG = com.blueridgebinary.terra.loaders.SessionLoaderListener.class.getSimpleName();

        private Integer mLocalityId;
        private HomeScreenFragment mContext;
        private Integer mSessionId;

        public SessionLoaderListener(HomeScreenFragment homeScreenFragment,
                                      @Nullable Integer sessionId) {

            mContext = homeScreenFragment;
            mSessionId = sessionId;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri baseUri;

            if (mSessionId == null) {
                baseUri = TerraDbContract.SessionEntry.CONTENT_URI;
            } else {
                baseUri = Uri.withAppendedPath(TerraDbContract.SessionEntry.CONTENT_URI,Uri.encode(mSessionId.toString()));
            }

            return new CursorLoader(mContext.getActivity(), baseUri, null,
                    null,null,null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Pass new data back to the HomeScreenFragment for it to do stuff with it
            mContext.handleNewSessionData(data);


        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mContext.handleNewSessionData(null);
        }
    }

