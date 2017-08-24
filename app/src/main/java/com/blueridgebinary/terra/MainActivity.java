package com.blueridgebinary.terra;


import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.Manifest;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;

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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.blueridgebinary.terra.OpenDataset;
import com.blueridgebinary.terra.adapters.HomeScreenPagerAdapter;
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

public class MainActivity extends FragmentActivity implements OnTerraFragmentInteractionListener<String> {

    ViewPager mViewPager;
    BottomNavigationView mBottomNavView;
    final List<MenuItem> items=new ArrayList<>();
    final int OVERVIEW_PAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        // Get sessionId from Intent Extras
        int sessionId = getIntent().getIntExtra("sessionId",0);

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
        mViewPager.setAdapter(new HomeScreenPagerAdapter(getSupportFragmentManager()));
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
    public void onFragmentInteraction(String tag, String data) {
        return;
    }
}
