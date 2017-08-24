package com.blueridgebinary.terra;


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

public class MainActivity extends FragmentActivity implements OnTerraFragmentInteractionListener<String> {

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);


        int sessionId = getIntent().getIntExtra("sessionId",0);
        mViewPager = (ViewPager) findViewById(R.id.vp_home);
        mViewPager.setAdapter(new HomeScreenPagerAdapter(getSupportFragmentManager()));
        mViewPager.setCurrentItem(1);
    }

    @Override
    public void onFragmentInteraction(String tag, String data) {
        return;
    }
}
