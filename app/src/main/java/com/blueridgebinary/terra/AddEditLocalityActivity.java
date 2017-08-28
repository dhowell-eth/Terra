package com.blueridgebinary.terra;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blueridgebinary.terra.utils.PermissionIds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class AddEditLocalityActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ImageButton btnGps;
    ImageButton btnEditLocation;
    Button btnOk;
    EditText etLatitude;
    EditText etLongitude;
    EditText etAccuracy;

    private boolean isGpsEnabled;
    private boolean isMapEditEnabled;

    private AddEditActivityLocationListener locationListener;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_locality);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_add_edit_locality);
        mapFragment.getMapAsync(this);

        // Get UI References
        btnGps = (ImageButton) findViewById(R.id.imbt_add_edit_locality_toggle_gps);
        btnEditLocation = (ImageButton) findViewById(R.id.imbt_add_edit_locality_toggle_edit);
        etLatitude = (EditText) findViewById(R.id.et_add_edit_locality_lat);
        etLongitude = (EditText) findViewById(R.id.et_add_edit_locality_long);
        etAccuracy = (EditText) findViewById(R.id.et_add_edit_locality_acc);

        etLatitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        etLongitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        etAccuracy.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);


        // Set image button onclick listener to enable/disable GPS
        // TODO: Can set this default value in user preferences
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        isGpsEnabled = false;
        isMapEditEnabled = false;

        Log.d("DEBUG","Made it through onCreate()");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("GPS", "called onRequestPermissionsResult...");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionIds.FINE_LOCATION_PERMISSION_ID:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listenForLocation();
                    toggleEditTextEnabled(false);
                }
                else {
                    permissionsDenied();
                }
                break;
        };
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void toggleGpsEnabled(View v) {
        if (isGpsEnabled) {
            stopGpsMode();
            btnGps.setImageResource(R.drawable.ic_gps_off_white_36dp);
            btnGps.setColorFilter(null);
        }
        else {
            if (isMapEditEnabled) btnEditLocation.performClick();
            startGpsMode();
            btnGps.setImageResource(R.drawable.ic_gps_fixed_white_36dp);
            btnGps.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
            // call function to enable GPS listeners, etc.

        }
        isGpsEnabled = !isGpsEnabled;
    }

    public void toggleMapEditLocation(View v) {
        if (isMapEditEnabled) {
            stopMapEditMode();
            btnEditLocation.setColorFilter(null);
        }
        else {
            // if the GPS is on, shut it off to enter manual mode
            if (isGpsEnabled) btnGps.performClick();
            startMapEditMode();
            // Highlight the button color to indicate you are in this mode
            btnEditLocation.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
            // Call a function that enables the map edit mode

        }
        isMapEditEnabled = !isMapEditEnabled;
    }

    public boolean startGpsMode() {
        // Start GPS Listening
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("GPS","I dont have permission so I am going to request it");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PermissionIds.FINE_LOCATION_PERMISSION_ID);
            return false;
            }
        else {
            listenForLocation();
            toggleEditTextEnabled(false);
            return true;
        }
    }


    public void permissionsDenied() {} {
        //Toast.makeText(this, "LOCATION PERMISSIONS DENIED!", Toast.LENGTH_LONG).show();
    }

    public boolean stopGpsMode() {
        locationManager.removeUpdates(locationListener);
        toggleEditTextEnabled(true);
        return true;
    }

    public boolean startMapEditMode() {
        return false;
    }

    public boolean stopMapEditMode() {
        return false;
    }

    public double[] getCurrentCoordinates() {
        // Returns an array of length 2, index 0 is latitude and index 1 is longitude,
        // if the data in the edit texts cannot be parsed this will return null
        double[] coordArray = new double[2];
        try {
            coordArray[0] = Double.parseDouble(etLatitude.getText().toString());
        }
        catch (NumberFormatException e) {return null;}

        try {
            coordArray[1] = Double.parseDouble(etLatitude.getText().toString());
        }
        catch (NumberFormatException e ) {return null;}

        return coordArray;
    }

    public void saveNewLocation() {
        // To be set as the onClick() event for the "OK" Button
    }

    public String[] formatGpsText(boolean displayDMS, double[] coords) {
        String formattedCoords[] = new String[3];
        if (!displayDMS) {
            formattedCoords[0] = String.format(Locale.US,"%.6f",coords[0]);
            formattedCoords[1] = String.format(Locale.US,"%.6f",coords[1]);
            formattedCoords[2] = String.format(Locale.US,"%.1f",coords[2]);
        }
        return formattedCoords;
    }

    public void toggleEditTextEnabled(boolean enable) {
        etLatitude.setEnabled(enable);
        etLongitude.setEnabled(enable);
        etAccuracy.setEnabled(enable);
    }

    public void listenForLocation() {
        locationListener = new AddEditActivityLocationListener();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            Log.d("GPS","Requested Location Updates.");
        } catch (SecurityException e) {
            Log.e("PERMISSIONS",e.toString());
        }
    }

    // For testing purposes, create an internal class to use as a location listener
    public class AddEditActivityLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            double coords[] = new double[3];
            coords[0] = location.getLatitude();
            coords[1] = location.getLongitude();
            coords[2] = location.getAccuracy();
            // TODO: Updated the formatGpsText  function  and this call to support  user defined units (DD vs DMS)
            String[] formattedCoords = formatGpsText(false,coords);
            Log.d("GPS","PING! NEW COORDS!");
            etLatitude.setText(formattedCoords[0], TextView.BufferType.EDITABLE);
            etLongitude.setText(formattedCoords[1], TextView.BufferType.EDITABLE);
            etAccuracy.setText(formattedCoords[2], TextView.BufferType.EDITABLE);

            // Add a marker in Sydney and move the camera
            LatLng currentLocation = new LatLng(coords[0], coords[1]);
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("NEW LOCATION"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,18));
            mMap.getUiSettings().setZoomGesturesEnabled(true);
        }


        // TODO: implement the below methods for handling changes  in Gps status
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




}
