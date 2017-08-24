package com.blueridgebinary.terra;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;

import com.blueridgebinary.terra.adapters.SessionCursorAdapter;
import com.blueridgebinary.terra.data.TerraDbContract;

public class OpenProjectActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SessionCursorAdapter.SessionAdapterOnClickHandler
{


    private static final String TAG = CreateProjectActivity.class.getSimpleName();
    private static final int SESSION_LOADER_ID = 0;

    RecyclerView mRecyclerView;
    private SessionCursorAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_project);


        // Get the recycler view and hook it up to the Adapter
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_open_sessions);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SessionCursorAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);


        // Add any onclick, swipe functionality here...


        // Initialize Loader for Session/Project Data
        getSupportLoaderManager().initLoader(SESSION_LOADER_ID, null, this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LOADER-DEBUG","onResume() called!");
        getSupportLoaderManager().restartLoader(SESSION_LOADER_ID, null, this);
    }

    /**
     * Instantiates and returns a new AsyncTaskLoader with the given ID.
     * This loader will return task data as a Cursor or null if an error occurs.
     *
     * Implements the required callbacks to take care of loading data at all stages of loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

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


    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // FROM GOOGLE - Update the data that the adapter uses to create ViewHolders
        mAdapter.swapCursor(data);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.
     * onLoaderReset removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onClick(int sessionId) {
        Intent intent = new Intent(OpenProjectActivity.this,com.blueridgebinary.terra.MainActivity.class);
        intent.putExtra("sessionId",sessionId);
        startActivity(intent);
        OpenProjectActivity.this.finish(); // Remove this activity from the backstack
    }
}
