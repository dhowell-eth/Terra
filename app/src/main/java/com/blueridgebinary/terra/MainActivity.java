package com.blueridgebinary.terra;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.BottomNavigationView;

import android.support.annotation.NonNull;

import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.blueridgebinary.terra.adapters.HomeScreenPagerAdapter;
import com.blueridgebinary.terra.data.CurrentLocality;
import com.blueridgebinary.terra.data.CurrentSession;
import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.fragments.HomeScreenDataOverviewFragment;
import com.blueridgebinary.terra.fragments.OnTerraFragmentInteractionListener;
import com.blueridgebinary.terra.utils.ListenableInteger;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// Note: Want to implement a loader on this activity then pass load results to child fragments

public class MainActivity extends AppCompatActivity implements
        OnTerraFragmentInteractionListener<String> {

    private static final String TAG = MainActivity.class.getSimpleName();

    static final int EXPORT_REQUEST_ID = 4756;


    ViewPager mViewPager;
    HomeScreenPagerAdapter homeScreenPagerAdapter;
    DrawerLayout mDrawerLayout;
    Toolbar mToolbar;

    BottomNavigationView mBottomNavView;
    Menu mOptionsMenu;

    final List<MenuItem> items=new ArrayList<>();

    private int sessionId;
    private int currentLocalityId;

    public ListenableInteger selectedLocality;

    public boolean itemsSelectedInRecyclerView;

    final static int OVERVIEW_PAGE = 1;

    public CurrentSession currentSession;
    public CurrentLocality currentLocality;
    List<Integer> selectedLocalitiesInRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);


        // Set up the main tool/app bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_home_activity);
        setSupportActionBar(myToolbar);

        // Get sessionId from Intent Extras
        sessionId = getIntent().getIntExtra("sessionId",0);
        String sessionName = getIntent().getStringExtra("sessionName");

        // Initialize current locality (rest of operations are handled by fragments)
        selectedLocality = new ListenableInteger(0);

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
        homeScreenPagerAdapter = new HomeScreenPagerAdapter(getSupportFragmentManager(),sessionId,sessionName);
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
                if (position == 2 && itemsSelectedInRecyclerView) {
                    mOptionsMenu.findItem(R.id.menu_execute_delete).setEnabled(true);
                    mOptionsMenu.findItem(R.id.menu_execute_delete).setVisible(true);
                }
                else {
                    mOptionsMenu.findItem(R.id.menu_execute_delete).setEnabled(false);
                    mOptionsMenu.findItem(R.id.menu_execute_delete).setVisible(false);
                }


            }
        });

    }


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
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_execute_delete:
                // User pressed Delete Button
                this.deleteSelectedLocalities();
                return true;

            case R.id.menu_export:
                // User pressed Export Button
                Intent exportIntent = new Intent(this, ExportActivity.class);
                exportIntent.putExtra("session_id",this.sessionId);
                startActivityForResult(exportIntent,EXPORT_REQUEST_ID);
                return true;
            case R.id.menu_settings:
                // User pressed Settings Button
                Intent settingsIntent = new Intent(this, PreferencesActivity.class);
                settingsIntent.putExtra("session_id",this.sessionId);
                startActivity(settingsIntent);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case EXPORT_REQUEST_ID:
                if (data.getBooleanExtra("export_result",true)) {
                    Toast.makeText(this, "Successfully exported data", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Error: Export failed", Toast.LENGTH_SHORT).show();
                }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void deleteSelectedLocalities() {
        // First, get the array of selected ids and convert from int to string array
        HomeScreenDataOverviewFragment dataOverviewFragment = (HomeScreenDataOverviewFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.vp_home + ":" + "2");
        int numIds = dataOverviewFragment.mAdapter.selectedRecyclerViewItems.size();
        String[] localityIds = new String[numIds];
        for (int i = 0; i < numIds; i++) {
            localityIds[i] = Integer.toString(dataOverviewFragment.mAdapter.selectedRecyclerViewItems.get(i));
        }
        Log.d(TAG,  "refreshAppBar: "+ Arrays.toString(localityIds));
        // Then, attempt to execute a delete with this selection
        int nRowsDeleted = getContentResolver().delete(TerraDbContract.LocalityEntry.CONTENT_URI, "",localityIds);
        // Once we've deleted the data, update our selection container to reflect changes
        dataOverviewFragment.mAdapter.selectedRecyclerViewItems.clear();
        dataOverviewFragment.mAdapter.notifyDataSetChanged();
        // And finally, refresh the app bar
        this.refreshAppBar(false);
    }

    // TODO:
    // Add onclick for delete button that will launch an alert
    // Will need to get the fragment with the recycler view to access the ids for the selected
    // stations we want to delete.
    // e.g. Fragment currentFragment = getActivity().getFragmentManager().findFragmentById(R.id.fragment_container);
    // Then, if the user selects yes we need to dispatch a command do our contentprovider to delete those entries
    // along with their associated data

    @Override
    public void onFragmentInteraction(String tag, String data) {
        return;
    }


    public void refreshAppBar(Boolean isSelected) {
        Log.d(TAG,  "refreshAppBar: "+ isSelected.toString());
        this.itemsSelectedInRecyclerView = isSelected;
        this.onPrepareOptionsMenu(mOptionsMenu);
        Log.d(TAG, "refreshAppBar: called.");
        return;
    }

    public int getCurrentLocalityId() {
        return currentLocalityId;
    }
    public void setCurrentLocalityId(int newId) {
        currentLocalityId = newId;
    }
    public int getSessionId() {
        return sessionId;
    }
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }



 }
