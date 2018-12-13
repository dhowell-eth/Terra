package com.blueridgebinary.terra.data;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

/**
 * Created by dorra on 8/24/2017.
 */



// TODO - DELETE THIS CLASS

// class containing helper methods for querying the content provider
public class TerraDbOperationsHelper {

  /*  public static Cursor queryLocalitybyId(ContentResolver resolver, int localityId) {

        class queryLoader implements LoaderManager.LoaderCallbacks<Cursor> {

            public queryLoader


            // e.g. new queryLoader(Activity activity, Uri uri, selection, where clause, where values, sortby)
            // loader creates an async task, executes the task, and callsback to the activity when complete to update  the UI.


            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return null;

                Uri uri = TerraDbContract.LocalityEntry.CONTENT_URI;
                return resolver.query(ContentUris.withAppendedId(uri, localityId), null, null, null, null);

                return new AsyncTaskLoader<Cursor>(this) {

                    // Initialize a Cursor, this will hold all the task data
                    Cursor mSessionData = null;

                    // onStartLoading() is called when a loader first starts loading data
                    @Override
                    protected void onStartLoading() {
                        if (mSessionData != null) {
                            // Delivers any previously loaded data immediately
                            deliverResult(mSessionData);
                        } else {
                            // Force a new load
                            forceLoad();
                        }
                    }

                    // loadInBackground() performs asynchronous loading of data
                    @Override
                    public Cursor loadInBackground() {
                        // Will implement to load data

                        // Query and load all task data in the background; sort by priority
                        // [Hint] use a try/catch block to catch any errors in loading data

                        try {
                            return getContentResolver().query(TerraDbContract.SessionEntry.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    TerraDbContract.SessionEntry.COLUMN_CREATED);

                        } catch (Exception e) {
                            Log.e(TAG, "Failed to asynchronously load data.");
                            e.printStackTrace();
                            return null;
                        }
                    }

                    // deliverResult sends the result of the load, a Cursor, to the registered listener
                    public void deliverResult(Cursor data) {
                        mSessionData = data;
                        super.deliverResult(data);
                    }
                };


            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        }


    }

    public static Cursor querySessionbyId(ContentResolver resolver, int sessionId) {
        Uri uri = TerraDbContract.SessionEntry.CONTENT_URI;
        return resolver.query(ContentUris.withAppendedId(uri, sessionId), null, null, null, null);
    }
*/


}
