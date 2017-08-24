package com.blueridgebinary.terra.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.blueridgebinary.terra.fragments.HomeScreenDataOverviewFragment;
import com.blueridgebinary.terra.fragments.HomeScreenMapFragment;
import com.blueridgebinary.terra.fragments.HomeScreenOverviewFragment;

import java.util.ArrayList;

/**
 * Created by dorra on 8/24/2017.
 */

public class HomeScreenPagerAdapter extends FragmentPagerAdapter {

    String[] mFragmentClasses;

    public HomeScreenPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
          case 0: return HomeScreenMapFragment.newInstance("Map Frag.","some data");
          case 1: return HomeScreenOverviewFragment.newInstance("Home Sreen Frag.","some data");
          case 2: return HomeScreenDataOverviewFragment.newInstance("Data Overview Frag.","some data");
          default: return HomeScreenOverviewFragment.newInstance("Home Screen Default Frag.","some data");
      }
    }

    @Override
    //
    public int getCount() {
        return 3;
    }
}
