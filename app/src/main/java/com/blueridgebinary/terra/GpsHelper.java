package com.blueridgebinary.terra;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by dorran on 8/18/2017.
 */

// GpsHelper contains helper methods for interfacing with a device's GPS

public class GpsHelper {

    public void listenForLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        testLocationListener locationListener = new testLocationListener();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        } catch (SecurityException e) {
            Log.e("PERMISSIONS",e.toString());
        }
    }

    // --------------------------------------------------------------------------------
    // For testing purposes, create an internal class to use as a location listener
    public class testLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("GPS","Location changed...");
            Log.d("GPS",String.format("Lat: %.3f - Lon: %.3f",location.getLatitude(), location.getLongitude()));
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
    // -----------------------------END CLASS----------------------------------------------




}
