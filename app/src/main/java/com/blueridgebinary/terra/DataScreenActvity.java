package com.blueridgebinary.terra;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.blueridgebinary.terra.adapters.CompassDataCursorAdapter;
import com.blueridgebinary.terra.adapters.SessionCursorAdapter;
import com.blueridgebinary.terra.data.TerraDbContract;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataScreenActvity extends AppCompatActivity implements
    LoaderManager.LoaderCallbacks<Cursor>,
    CompassDataCursorAdapter.CompassDataAdapterOnClickHandler
    {


        private static final String TAG = DataScreenActvity.class.getSimpleName();
        private static final int COMPASS_DATA_LOADER = 696969;
        public RecyclerView mRecyclerView;
        private CompassDataCursorAdapter mAdapter;
        private boolean itemsSelectedInRecyclerView;
        private Menu mOptionsMenu;
        private int localityId;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_data_screen_actvity);

            // Initialize the toolbar/app bar
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_home_activity);
            setSupportActionBar(myToolbar);

            localityId = getIntent().getIntExtra("localityId",0);

            // Get the recycler view and hook it up to the Adapter
            mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_data_screen_compass);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAdapter = new CompassDataCursorAdapter(this,this);
            mRecyclerView.setAdapter(mAdapter);


            // Initialize Loader for Session/Project Data
            getSupportLoaderManager().initLoader(COMPASS_DATA_LOADER, null, this);

        }

        @Override
        protected void onResume() {
            super.onResume();
            Log.d("LOADER-DEBUG","onResume() called!");
            getSupportLoaderManager().restartLoader(COMPASS_DATA_LOADER, null, this);
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

                String[] localityIdArray = new String[1];
                localityIdArray[0]=Integer.toString(localityId);

                try {
                    return getContentResolver().query(TerraDbContract.JoinedCompassEntry.CONTENT_URI,
                            null,
                            null,
                            localityIdArray,
                            null);

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
            Log.d(TAG, "onLoadFinished: New data loaded from asyncloader...");
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


    // ---------------------------- MENU LOGIC --------------------------------------------

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            mOptionsMenu = menu;
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.options_main, menu);
            return true;
        }

        @Override
        public boolean onPrepareOptionsMenu(Menu menu) {
            MenuItem delete = menu.findItem(R.id.menu_execute_delete);
            // If there are items selected, show the delete button
            if (itemsSelectedInRecyclerView) {
                delete.setVisible(true);
                delete.setEnabled(true);
            }
            // Otherwise hide it
            else
            {
                delete.setVisible(false);
                delete.setEnabled(false);
            }
            // Hide export button
            MenuItem export = menu.findItem(R.id.menu_export);
            export.setEnabled(false);
            export.setVisible(false);

            return super.onPrepareOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_execute_delete:
                    // User pressed Delete Button
                    this.deleteSelectedRows();
                    return true;

                case R.id.menu_export:
                    // User pressed Export Button
                    return true;
                case R.id.menu_settings:
                    // User pressed Settings Button

                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        public void deleteSelectedRows() {
            Log.d(TAG, "deleteSelectedRows: Tried to delete some stuff. Implement logic here.");
            // First, get the array of selected ids and convert from int to string array
            int numIds = mAdapter.selectedRecyclerViewItems.size();
            String[] measurementIds = new String[numIds];
            for (int i = 0; i < numIds; i++) {
                measurementIds[i] = Integer.toString(mAdapter.selectedRecyclerViewItems.get(i));
            }
            Log.d(TAG, "deleteSelectedRows: " + measurementIds.toString());
            // Then, attempt to execute a delete with this selection
            int nRowsDeleted = getContentResolver().delete(TerraDbContract.JoinedCompassEntry.CONTENT_URI, "",measurementIds);
            // Once we've deleted the data, update our selection container to reflect changes
            getSupportLoaderManager().restartLoader(COMPASS_DATA_LOADER, null, this);
            mAdapter.selectedRecyclerViewItems.clear();
            mAdapter.notifyDataSetChanged();
            // And finally, refresh the app bar
            this.refreshAppBar(false);
        }


        public void refreshAppBar(Boolean isSelected) {
            Log.d(TAG,  "refreshAppBar: "+ isSelected.toString());
            this.itemsSelectedInRecyclerView = isSelected;
            this.onPrepareOptionsMenu(mOptionsMenu);
            Log.d(TAG, "refreshAppBar: called.");
            return;
        }




        @Override
        public void onClick(int localityId) {

        }
}
