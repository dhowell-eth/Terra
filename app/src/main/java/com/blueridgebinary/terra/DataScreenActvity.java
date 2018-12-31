package com.blueridgebinary.terra;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
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
import com.blueridgebinary.terra.loaders.LoaderIds;

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
        private int sessionId;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_data_screen_actvity);

            // Initialize the toolbar/app bar
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_home_activity);
            setSupportActionBar(myToolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            localityId = getIntent().getIntExtra("localityId",0);
            sessionId = getIntent().getIntExtra("session_id",0);

            // Get the recycler view and hook it up to the Adapter
            mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_data_screen_compass);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAdapter = new CompassDataCursorAdapter(this,this);
            mRecyclerView.setAdapter(mAdapter);
//            mRecyclerView.getAdapter().notifyDataSetChanged();

            // Initialize Loader for Session/Project Data
            this.getSupportLoaderManager().initLoader(COMPASS_DATA_LOADER,
                    null,
                    this);
        }

        @Override
        protected void onResume() {
            super.onResume();
            getSupportLoaderManager().restartLoader(COMPASS_DATA_LOADER, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String selectionStatement = TerraDbContract.CompassMeasurementEntry.COLUMN_LOCALITYID + " = ?";
            String[] selectionArgs = new String[]{Integer.toString(localityId)};
            return new CursorLoader(this,
                    TerraDbContract.JoinedCompassEntry.CONTENT_URI,
                    null,
                    selectionStatement,
                    selectionArgs,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);
        }

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
                    // User pressed Settings Button
                    Intent intent = new Intent(this, PreferencesActivity.class);
                    intent.putExtra("session_id",this.sessionId);
                    startActivity(intent);
                case android.R.id.home:
                    this.onBackPressed();
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        public void deleteSelectedRows() {
            // First, get the array of selected ids and convert from int to string array
            int numIds = mAdapter.selectedRecyclerViewItems.size();
            String[] measurementIds = new String[numIds];
            for (int i = 0; i < numIds; i++) {
                measurementIds[i] = Integer.toString(mAdapter.selectedRecyclerViewItems.get(i));
            }

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

            this.itemsSelectedInRecyclerView = isSelected;
            this.onPrepareOptionsMenu(mOptionsMenu);

            return;
        }




        @Override
        public void onClick(int localityId) {
            // do nothing
        }

    }
