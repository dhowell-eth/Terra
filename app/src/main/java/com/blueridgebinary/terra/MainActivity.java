package com.blueridgebinary.terra;


import android.database.Cursor;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.blueridgebinary.terra.OpenDataset;
import com.blueridgebinary.terra.adapters.HomeScreenPagerAdapter;
import com.blueridgebinary.terra.data.CurrentLocality;
import com.blueridgebinary.terra.data.CurrentSession;
import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.fragments.HomeScreenOverviewFragment;
import com.blueridgebinary.terra.fragments.OnTerraFragmentInteractionListener;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


// Note: Want to implement a loader on this activity then pass load results to child fragments

public class MainActivity extends FragmentActivity implements
        OnTerraFragmentInteractionListener<String> {

    ViewPager mViewPager;
    HomeScreenPagerAdapter homeScreenPagerAdapter;

    BottomNavigationView mBottomNavView;
    final List<MenuItem> items=new ArrayList<>();

    public int sessionId;

    final static int SESSION_LOADER_ID = 10;
    final static int LOCALITY_LOADER_ID = 20;
    final static int OVERVIEW_PAGE = 1;

    public CurrentSession currentSession;
    public CurrentLocality currentLocality;


    // Define loader and respective callbacks to be used by this activity
    // this can eventually get moved out into separate files for organizational purposes

    // <-----   Session Data Loader ----->
    private LoaderManager.LoaderCallbacks<Cursor> sessionLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    Uri baseUri;
                    // sessionId defaults to 0 if no id is passed with the
                    // intent that creates this activity
                    if (sessionId != 0) {
                        // Querying the uri for this specific session (that way listener works properly)
                        baseUri = Uri.withAppendedPath(TerraDbContract.SessionEntry.CONTENT_URI,Uri.encode(Integer.toString(sessionId)));
                    } else {
                        // don't do anything if this activity doesn't have a sessionId
                        return null;
                    }
                    return new CursorLoader(getBaseContext(), baseUri, null,
                            null,null,null);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    // Populate current Session object

                    //
                    Log.d("Loader-DEBUG","called onLoadFinished()!");
                    sessionCursorToCurrentSession(data);

                    // Force refresh on fragment level
                    //mViewPager.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    // Clear data for the current session
                    // TODO: not sure if I also need to store this data in this activity or if it can just sit in the adapter
                    currentSession = null;
                    //homeScreenPagerAdapter.setCurrentSession(null);
                }
            };

    // <-----   Locality Data Loader ----->



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        // Get sessionId from Intent Extras
        sessionId = getIntent().getIntExtra("sessionId",0);

        // Get ViewPager
        mViewPager = (ViewPager) findViewById(R.id.vp_home);
        // Get Bottom Menu Widget
        mBottomNavView = (BottomNavigationView) findViewById(R.id.navigation_home);
        //  Add Menu items to a list for getting their index below
        Menu menu = mBottomNavView.getMenu();
        for(int i=0; i<menu.size(); i++){
            items.add(menu.getItem(i));
        }
        // Connect adapter to view pager (this feeds the relevant Fragments to it)
        homeScreenPagerAdapter = new HomeScreenPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(homeScreenPagerAdapter);
        // Set the default page to the center page [0 -- (1) -- 2]
        mViewPager.setCurrentItem(OVERVIEW_PAGE);
        // Set the menu item to match current page
        mBottomNavView.setSelectedItemId(R.id.menu_home_overview);
        // Create Listener for Bottom Menu that changes the ViewPager accordingly
        mBottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int position=items.indexOf(item);
                mViewPager.setCurrentItem(position);
                return true;
            }
        });
        // Create listener to update bottom menu if page is  swiped
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()  {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
            // When a new page is selected, change the nav menu to the corresponding item
            @Override
            public void onPageSelected(int position) {
                if (position < items.size()) {
                    mBottomNavView.setSelectedItemId(items.get(position).getItemId());
                }
            }
        });

        getSupportLoaderManager().initLoader(SESSION_LOADER_ID,null,sessionLoaderListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(SESSION_LOADER_ID, null, sessionLoaderListener);
    }


    @Override
    public void onFragmentInteraction(String tag, String data) {
        return;
    }



    public void sessionCursorToCurrentSession(Cursor sessionCursor) {

        Log.d("NewURIDEBUG",Integer.toString(sessionCursor.getCount()) + "Records in Cursor");
        Log.d("NewURIDEBUG","Got cursor and am trying to retrieve data");
        if (sessionCursor.getCount() != 1) return;
        sessionCursor.moveToFirst();

        int idIndex = sessionCursor.getColumnIndex(TerraDbContract.SessionEntry._ID);
        int nameIndex = sessionCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_SESSIONNAME);
        int notesIndex = sessionCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_NOTES);
        int updatedIndex = sessionCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_UPDATED);
        int createdIndex = sessionCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_CREATED);

        int id = sessionCursor.getInt(idIndex);
        String name = sessionCursor.getString(nameIndex);
        String notes = sessionCursor.getString(notesIndex);
        String updated = sessionCursor.getString(updatedIndex);
        String created = sessionCursor.getString(createdIndex);

        currentSession = new CurrentSession(id,name,notes,updated,created);
        Log.d("NewURIDEBUG","Loaded Session:" + currentSession.getSessionName());
        // TODO: REMOVE
        //homeScreenPagerAdapter.setCurrentSession(currentSession);
    }

}
