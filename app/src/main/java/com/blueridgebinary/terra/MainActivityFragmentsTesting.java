package com.blueridgebinary.terra;


//import android.support.v4.app.FragmentManager
//import android.support.v4.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivityFragmentsTesting extends AppCompatActivity implements OpenDataset.OnFragmentInteractionListener {

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TESTING -----------
        // Have to wrap calls in logic for handling the "dangerous" fine locaction permission

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        else {
            // Otherwise go ahead and do what we want to do cause you've already got permissions
            GpsHelper gpsHelper = new GpsHelper();
            gpsHelper.listenForLocation(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("GPS", "called onRequestPermissionsResult...");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_FINE_LOCATION) {
            GpsHelper gpsHelper = new GpsHelper();
            gpsHelper.listenForLocation(this);
        }
    }

    // ------------------------------------------------------
    // TODO - Created a simple  menu for getting things started.  Will want to reorganize these functions.
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    // TODO - Add function calls to case statement.

    @Override
    public boolean onOptionItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new:
                return true;
            case R.id.menu_item_open:
                menuOpenExisting();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    } */

    public void menuOpenExisting(View view) {
       /* FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        OpenDataset fragment = new OpenDataset();
        fragmentTransaction.add(R.id.test_fragment_viewgroup, fragment,"testFragID");
        fragmentTransaction.commit();
        */

        Intent intent = new Intent(this, CompassActivity.class);
        startActivity(intent);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        return;
    }



    // END TEST MENU STUFF -----------------------------------
}
