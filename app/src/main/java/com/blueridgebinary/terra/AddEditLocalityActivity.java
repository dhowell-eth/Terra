package com.blueridgebinary.terra;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.utils.PermissionIds;
import com.blueridgebinary.terra.utils.util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class AddEditLocalityActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        TextWatcher {

    private GoogleMap mMap;
    ImageButton btnGps;
    ImageButton btnEditLocation;
    Button btnOk;
    EditText etLatitude;
    EditText etLongitude;
    EditText etAccuracy;
    ImageView ivCrosshairs;
    TextView tvTitle;

    private boolean isGpsEnabled;
    private boolean isMapEditEnabled;
    private boolean displayCoordsInDms;

    private AddEditActivityLocationListener locationListener;
    private LocationManager locationManager;
    private Marker currentLocationMarker;

    private int defaultZoomLevel;
    private int sessionId;
    private String sessionName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_locality);

        defaultZoomLevel = 16;

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
        ivCrosshairs = (ImageView) findViewById(R.id.iv_add_edit_crosshairs);
        tvTitle = (TextView) findViewById(R.id.tv_add_edit_locality_title);

        // Get extras
        sessionId = getIntent().getIntExtra("sessionId",0);
        sessionName = getIntent().getStringExtra("sessionName");

        // Update title with the actual session/project name
        tvTitle.setText(sessionName);

        // Set input type to numeric for edit texts
        etLatitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        etLongitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        etAccuracy.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        toggleEditTextEnabled(true);

        // Get the LocationManager
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

        // Set image button onclick listener to enable/disable GPS
        // TODO: Can set this default value in user preferences
        ivCrosshairs.setVisibility(View.INVISIBLE);

        // Initialize flags used by the UI
        isGpsEnabled = false;
        isMapEditEnabled = false;
        displayCoordsInDms = false;

        // Start with GPS on by default
        btnGps.callOnClick();

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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Get a reference to the map
        mMap = googleMap;
        // Set Map UI Settings
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
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
        if (mMap != null) {
            if (currentLocationMarker != null) {
                currentLocationMarker.setVisible(false);
            }
            ivCrosshairs.setVisibility(View.VISIBLE);
            mMap.setOnCameraMoveListener(this);
            toggleEditTextEnabled(false);
            updateEntryFieldsWithMapCenter();
            return true;
        }
        else {return false;}
    }

    public boolean stopMapEditMode() {
        if (mMap != null) {
            mMap.setOnCameraMoveListener(null);
            toggleEditTextEnabled(true);
            ivCrosshairs.setVisibility(View.INVISIBLE);
            // set Marker as visible
            if (currentLocationMarker != null) {
                currentLocationMarker.setVisible(true);
            }
            return true;
        }
        else {return false;}
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
            coordArray[1] = Double.parseDouble(etLongitude.getText().toString());
        }
        catch (NumberFormatException e ) {return null;}
        return coordArray;
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
        if (enable) {
            etLatitude.addTextChangedListener(this);
            etLongitude.addTextChangedListener(this);
        }
        else {
            etLatitude.removeTextChangedListener(this);
            etLongitude.removeTextChangedListener(this);
        }
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


    public void updateEntryFieldsWithMapCenter() {
        if (mMap != null) {
            LatLng center = mMap.getCameraPosition().target;
            String[] formattedCoords = formatGpsText(displayCoordsInDms, new double[]{center.latitude, center.longitude, 0.0});
            updateEntryFields(formattedCoords);
            if (currentLocationMarker == null) {
                currentLocationMarker = mMap.addMarker(new MarkerOptions().position(center).title("NEW LOCATION").visible(false));
            } else {
                currentLocationMarker.setPosition(center);
            }
        }
    }

    @Override
    public void onCameraMove() {
        updateEntryFieldsWithMapCenter();
    }

    public void updateEntryFields(String[] formattedCoords) {
        etLatitude.setText(formattedCoords[0], TextView.BufferType.EDITABLE);
        etLongitude.setText(formattedCoords[1], TextView.BufferType.EDITABLE);
        etAccuracy.setText(formattedCoords[2], TextView.BufferType.EDITABLE);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        Log.d("EDIT_TEXT","TEXT CHANGED!");
        double[] currentCoordinates = getCurrentCoordinates();
        if (currentCoordinates == null) {return;}
        if (mMap == null) {return;}
        LatLng currentLocation = new LatLng(currentCoordinates[0],currentCoordinates[1]);
        if (currentLocationMarker == null) {
            currentLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("NEW LOCATION"));
        } else {
            currentLocationMarker.setPosition(currentLocation);
        }
        // Center the camera over the current location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, defaultZoomLevel));
    }

    // TODO: implement this method for validating the data entered into the edit texts
    private boolean validateEntries() {
        // Checks whether all  of the entered values are valid decimal  numbers
        boolean isValid;
        double[] currentCoordinates = getCurrentCoordinates();
        if (currentCoordinates == null) {
            isValid = false;
        }
        else {isValid = true;}

        try {
            double accuracy = Double.parseDouble(etAccuracy.getText().toString());
        }
        catch (NumberFormatException e ) {
            if (etAccuracy.getText().toString().equals("")) {
                etAccuracy.setText("0");
            }
            else {
                isValid = false;
            }
        }

        return isValid;
    }

    //  TODO: implement a method for saving
    public void saveNewLocation(View v) {
        // To be set as the onClick() event for the "OK" Button
        // Validate entered data
        if (!validateEntries()) {
            Toast.makeText(this, "Error: Number formatting incorrect, please enter only numerical values" +
                    " for locations", Toast.LENGTH_LONG).show();
            return;
        }

        double[] currentCoordinates = getCurrentCoordinates();
        String accuracy = etAccuracy.getText().toString();
        String dateTimestamp =  util.getDateTime();


        ContentValues contentValues = new ContentValues();
        // Put the task description and selected mPriority into the ContentValues
        contentValues.put(TerraDbContract.LocalityEntry.COLUMN_LAT, currentCoordinates[0]);
        contentValues.put(TerraDbContract.LocalityEntry.COLUMN_LONG, currentCoordinates[1]);
        contentValues.put(TerraDbContract.LocalityEntry.COLUMN_GPSACCURACY, accuracy);
        contentValues.put(TerraDbContract.LocalityEntry.COLUMN_SESSIONID, sessionId);
        contentValues.put(TerraDbContract.LocalityEntry.COLUMN_CREATED, dateTimestamp);
        contentValues.put(TerraDbContract.LocalityEntry.COLUMN_UPDATED, dateTimestamp);

        // Insert the content values via a ContentResolver
        Uri uri = getContentResolver().insert(TerraDbContract.LocalityEntry.CONTENT_URI, contentValues);
        Log.d("ADDLOCALITY","Added new locality!: "+ uri.toString());
        finish();
    }

    // For testing purposes, create an internal class to use as a location listener
    public class AddEditActivityLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            // Update Edit Texts with current coordinates
            double[] coords = new double[3];
            coords[0] = location.getLatitude();
            coords[1] = location.getLongitude();
            coords[2] = location.getAccuracy();
            String[] formattedCoords = formatGpsText(displayCoordsInDms,coords);
            updateEntryFields(formattedCoords);

            if (mMap != null) {
                // Add a marker at the current location, or update the current marker if it exists
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                if (currentLocationMarker == null) {
                    currentLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("NEW LOCATION"));
                } else {
                    currentLocationMarker.setPosition(currentLocation);
                }
                // Center the camera over the current location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, defaultZoomLevel));
            }
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
