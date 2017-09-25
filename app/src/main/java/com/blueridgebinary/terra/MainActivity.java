package com.blueridgebinary.terra;


import android.support.design.widget.BottomNavigationView;

import android.support.annotation.NonNull;

import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.blueridgebinary.terra.adapters.HomeScreenPagerAdapter;
import com.blueridgebinary.terra.data.CurrentLocality;
import com.blueridgebinary.terra.data.CurrentSession;
import com.blueridgebinary.terra.fragments.OnTerraFragmentInteractionListener;
import com.blueridgebinary.terra.utils.ListenableInteger;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;


// Note: Want to implement a loader on this activity then pass load results to child fragments

public class MainActivity extends AppCompatActivity implements
        OnTerraFragmentInteractionListener<String> {

    ViewPager mViewPager;
    HomeScreenPagerAdapter homeScreenPagerAdapter;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    Toolbar mToolbar;

    BottomNavigationView mBottomNavView;
    final List<MenuItem> items=new ArrayList<>();

    private int sessionId;
    private int currentLocalityId;

    public ListenableInteger selectedLocality;

    final static int OVERVIEW_PAGE = 1;

    public CurrentSession currentSession;
    public CurrentLocality currentLocality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        //Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_home_screen);
        //setSupportActionBar(myToolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_main);
        setupToolbar();
        // Add whatever stuff you need for populating drawer on the line below

        setupDrawerToggle();

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
            }
        });


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

     void setupToolbar(){
         mToolbar = (Toolbar) findViewById(R.id.toolbar_home_activity);
         setSupportActionBar(mToolbar);
         getSupportActionBar().setDisplayShowHomeEnabled(true);
     }

     void setupDrawerToggle(){
         mDrawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this,mDrawerLayout,mToolbar,R.string.app_name, R.string.app_name);
         //This is necessary to change the icon of the Drawer Toggle upon state change.
         mDrawerToggle.syncState();
     }


    @Override
    public void onFragmentInteraction(String tag, String data) {
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
