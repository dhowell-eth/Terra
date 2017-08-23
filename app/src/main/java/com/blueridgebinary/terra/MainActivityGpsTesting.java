package com.blueridgebinary.terra;


//import android.support.v4.app.FragmentManager
//import android.support.v4.app.FragmentTransaction;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivityGpsTesting extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 42;
    TextView tvGpsValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvGpsValues = (TextView) findViewById(R.id.tv_gps_values);

        Intent intent = new Intent(this, TestDB.class);
        startActivity(intent);
        // TESTING -----------
        // Have to wrap calls in logic for handling the "dangerous" fine locaction permission
/*
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                Log.d("GPS","I dont have permission so I am going to request it");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        else {
            // Otherwise go ahead and do what we want to do cause you've already got permissions
            listenForLocation();
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("GPS", "called onRequestPermissionsResult...");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listenForLocation();
                }
                else {
                    Log.d("GPS","I requested permissions but they were not granted... :(");
                }
                break;
        };
    }

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

    public void listenForLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        testLocationListener locationListener = new testLocationListener();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            Log.d("GPS","Requested Location Updates.");
        } catch (SecurityException e) {
            Log.e("PERMISSIONS",e.toString());
        }
    }

    // --------------------------------------------------------------------------------
    // For testing purposes, create an internal class to use as a location listener
    public class testLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            String textGpsValues = String.format("Lat: %.3f - Lon: %.3f - Accuracy: %.1f",location.getLatitude(),
                    location.getLongitude(),location.getAccuracy());
            tvGpsValues.setText(textGpsValues);
            Log.d("GPS","Location changed...");
            Log.d("GPS",textGpsValues);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }



    // END TEST MENU STUFF -----------------------------------
}
