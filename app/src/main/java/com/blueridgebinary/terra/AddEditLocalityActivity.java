package com.blueridgebinary.terra;

import android.media.Image;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_locality);

        // Get UI References
        btnGps = (ImageButton) findViewById(R.id.imbt_add_edit_locality_toggle_gps);
        btnEditLocation = (ImageButton) findViewById(R.id.imbt_add_edit_locality_toggle_edit);
        etLatitude = (EditText) findViewById(R.id.et_add_edit_locality_lat);
        etLongitude = (EditText) findViewById(R.id.et_add_edit_locality_long);
        etAccuracy = (EditText) findViewById(R.id.et_add_edit_locality_acc);

        // Set image button onclick listener to enable/disable GPS
        // TODO: Can set this default value in user preferences
        isGpsEnabled = false;
        isMapEditEnabled = false;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_add_edit_locality);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void toggleGpsEnabled(View v) {
        if (isGpsEnabled == true) {
            stopGpsMode();
            btnGps.setImageResource(R.drawable.ic_gps_off_white_36dp);
            btnGps.setColorFilter(null);
        }
        else {
            if (isMapEditEnabled == true) btnEditLocation.performClick();
            startGpsMode();
            btnGps.setImageResource(R.drawable.ic_gps_fixed_white_36dp);
            btnGps.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
            // call function to enable GPS listeners, etc.

        }
        isGpsEnabled = !isGpsEnabled;
    }

    public void toggleMapEditLocation(View v) {
        if (isMapEditEnabled == true) {
            stopMapEditMode();
            btnEditLocation.setColorFilter(null);
        }
        else {
            // if the GPS is on, shut it off to enter manual mode
            if (isGpsEnabled == true) btnGps.performClick();
            startMapEditMode();
            // Highlight the button color to indicate you are in this mode
            btnEditLocation.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
            // Call a function that enables the map edit mode

        }
        isMapEditEnabled = !isMapEditEnabled;
    }

    public boolean startGpsMode() {
        // Start GPS Listening
        return false;
    }

    public boolean stopGpsMode() {
        return false;
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


}
