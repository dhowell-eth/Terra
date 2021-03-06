package com.blueridgebinary.terra.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.blueridgebinary.terra.data.CurrentLocality;
import com.blueridgebinary.terra.data.CurrentSession;
import com.blueridgebinary.terra.fragments.HomeScreenDataOverviewFragment;
import com.blueridgebinary.terra.fragments.HomeScreenFragment;
import com.blueridgebinary.terra.fragments.HomeScreenMapFragment;
import com.blueridgebinary.terra.fragments.HomeScreenOverviewFragment;

import java.util.ArrayList;

/**
 * Created by dorra on 8/24/2017.
 */

public class HomeScreenPagerAdapter extends FragmentPagerAdapter {

    private CurrentSession currentSession;
    private CurrentLocality currentLocality;
    private int currentSessionId;
    private String currentSessionName;

    public HomeScreenPagerAdapter(FragmentManager fm,int id, String name) {
        super(fm);
        currentSessionId = id;
        currentSessionName = name;
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
          case 0: return HomeScreenMapFragment.newInstance(currentSessionName,currentSessionId);
          case 1: return HomeScreenOverviewFragment.newInstance(currentSessionName,currentSessionId);
          case 2: return HomeScreenDataOverviewFragment.newInstance(currentSessionName,currentSessionId);
          default: return HomeScreenOverviewFragment.newInstance(currentSessionName,currentSessionId);
      }
    }

    @Override
    //
    public int getCount() {
        return 3;
    }

   /* @Override
    public int getItemPosition(Object object) {
        HomeScreenFragment f = (HomeScreenFragment) object;
        if (f != null) {

            f.refreshFragmentData(currentSession);
            f.refreshFragmentData(currentLocality);
        }
        // debug
        else {

        }

        return super.getItemPosition(object);
    }*/

    public void setCurrentSession(CurrentSession currentSession) {
        this.currentSession = currentSession;
    }

    public void setCurrentLocality(CurrentLocality currentLocality) {
        this.currentLocality = currentLocality;
    }

    public CurrentSession getCurrentSession() {
        return currentSession;
    }

    public CurrentLocality getCurrentLocality() {
        return currentLocality;
    }



}
