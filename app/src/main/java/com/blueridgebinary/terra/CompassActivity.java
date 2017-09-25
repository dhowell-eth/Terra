package com.blueridgebinary.terra;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.blueridgebinary.terra.utils.ListenableBoolean;

import org.w3c.dom.Text;

import java.util.Locale;

/*
    Notes 8/16/17:

    TODO: This compass activity needs to get put into a Fragment

    As of of now this activity pulls current azimuth and dip data assuming the user is holding the phone
    towards the direction of interest. I want to update these methods to allow for the option of
    automatically determining the strike/dip of the phone (using the plane defined by the phone screen) like
    other similar apps do.

    I also want to add functionality for accuracy warnings, etc.

    -DH
*/
public class CompassActivity extends AppCompatActivity implements SensorEventListener,TextWatcher {

    static private final String TAG = CompassActivity.class.getSimpleName();

    private CompassView mCompassView;
    private EditText mAzimuthEditText;
    private EditText mDipEditText;
    private Spinner mCompassMeasurementSpinner;
    private Spinner mCompassModeSpinner;
    private Button mOkButton;
    private SpinnerAdapter mCompassMeasurementSpinnerAdapter;
    public  ListenableBoolean isEnabled;

    public SensorManager mSensorManager;
    public SensorEventListener mSensorEventListener;

    public Sensor mAccelerometerSensor;
    public Sensor mMagnetometerSensor;
    public int mSensorDelay;

    public float[] orientationMatrix;
    public float[] geomagneticMatrix;
    public float[] gravityMatrix;
    public float[] rRotationMatrix;
    public float[] iRotationMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        // Get UI Components
        mCompassView = (CompassView) findViewById(R.id.compass_view_add);
        mAzimuthEditText = (EditText) findViewById(R.id.et_compass_azi);
        mDipEditText = (EditText) findViewById(R.id.et_compass_dip);
        mOkButton = (Button) findViewById(R.id.btn_compass_ok);
        mCompassModeSpinner = (Spinner) findViewById(R.id.spinner_compass_mode);
        mCompassMeasurementSpinner = (Spinner) findViewById(R.id.spinner_compass_measurement);

        // Set edit text input types and default them to off (only populated by compass view)
        mAzimuthEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        mDipEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        toggleEditTextEnabled(false);


        // Get Sensor Manager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get Sensors
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        // Set Sensor Delay
        mSensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
        // Prepare orientation matrix
        orientationMatrix = new float[3];
        rRotationMatrix = new float[9];
        iRotationMatrix = new float[9];

        // Add listener for changes in the compass view enabled status
        isEnabled = mCompassView.isEnabled;
        isEnabled.addListener(new ListenableBoolean.ChangeListener() {
            @Override
            public void onChange() {
                if (mCompassView.isEnabled()) {
                    toggleEditTextEnabled(false);
                    mSensorManager.registerListener(CompassActivity.this, mAccelerometerSensor, mSensorDelay);
                    mSensorManager.registerListener(CompassActivity.this, mMagnetometerSensor, mSensorDelay);
                    Toast.makeText(CompassActivity.this, getResources().getString(R.string.compass_enabled_message), Toast.LENGTH_SHORT).show();
                }
                else {
                    toggleEditTextEnabled(true);
                    mSensorManager.unregisterListener(CompassActivity.this, mAccelerometerSensor);
                    mSensorManager.unregisterListener(CompassActivity.this, mMagnetometerSensor);
                    Toast.makeText(CompassActivity.this, getResources().getString(R.string.compass_disabled_message), Toast.LENGTH_SHORT).show();
                }
            }});

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.compass_modes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCompassModeSpinner.setAdapter(adapter);
        mCompassModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //  Assumes pos 0 = bearing pos 1 = vector pos 2 = plane
                mCompassView.setNeedleModeId(position + 1);
                if (position == 0) {
                    mDipEditText.setVisibility(View.GONE);
                }
                else {
                    mDipEditText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Make a spinner adapter for the measurement types
        // will just use a simple query adapter
        // need to make sure that we also have an option here to add a new measurement type

    }



    @Override
    protected void onResume() {
        super.onResume();

        // Register Listeners
        // Doing this here should allow the compass to keep running when resuming from a pause
        if (mCompassView.isEnabled()) {
            mSensorManager.registerListener(this, mAccelerometerSensor, mSensorDelay);
            mSensorManager.registerListener(this, mMagnetometerSensor, mSensorDelay);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCompassView.isEnabled()) {
            mSensorManager.unregisterListener(this, mAccelerometerSensor);
            mSensorManager.unregisterListener(this, mMagnetometerSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticMatrix = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityMatrix = event.values;
        }
        if (gravityMatrix != null && geomagneticMatrix != null) {
            boolean acquiredRotationMatrix = SensorManager.getRotationMatrix(rRotationMatrix,
                    iRotationMatrix,
                    gravityMatrix,
                    geomagneticMatrix);
            if (acquiredRotationMatrix) {
                orientationMatrix = SensorManager.getOrientation(rRotationMatrix, new float[3]);
                // TODO: DO STUFF WITH NEW SENSOR DATA HERE!

                float aziDeg = (float) Math.toDegrees(orientationMatrix[0]);
                float dipDeg = (float) Math.toDegrees(orientationMatrix[1]);
                updateWithNewCompassData(aziDeg,dipDeg);

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String sensorStatus;
        boolean needsWarning;
        String sensorName = sensor.getStringType();
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                sensorStatus = "LOW";
                needsWarning = true;
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                sensorStatus = "MEDIUM";
                needsWarning = true;
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                sensorStatus = "HIGH";
                needsWarning = false;
                break;
            default:
                sensorStatus = "UNKNOWN ACCURACY";
                needsWarning = false; // TODO: might change this to true
                break;
        };
        sendSensorAccuracyWarning();
    }

    // TODO: this method will do an action based on the accuracy of the compass
    public void sendSensorAccuracyWarning() {}

    public void updateWithNewCompassData(float azi, float dip) {
        mAzimuthEditText.setText(String.format(Locale.US,"%.2f",azi));
        mDipEditText.setText(String.format(Locale.US,"%.2f",dip));
        mCompassView.setOrientation(azi,dip);
    }

    //public float[] calculateDipDegreesVectorMode() {}
    //public float[] calculateDipDirectionPlaneMode() {}

    public float[] convertRawOrientationToViewOrientation(int compassMode) {
        float[] results = new float[2];
        switch (compassMode) {
            case 1:
                results[0] = orientationMatrix[0];
                results[1] = 0;
                break;
            case 2:
                break;
            case 3:
                break;

        }
        return results;
    }

    public void toggleEditTextEnabled(boolean enable) {
        if (enable) {
            mAzimuthEditText.addTextChangedListener(this);
            mDipEditText.addTextChangedListener(this);
        }
        else {
            mAzimuthEditText.removeTextChangedListener(this);
            mDipEditText.removeTextChangedListener(this);
        }
        mAzimuthEditText.setEnabled(enable);
        mDipEditText.setEnabled(enable);
    }

    // Methods for handling use input to azi/dip fields
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override
    public void afterTextChanged(Editable s) {
        try {
            mCompassView.setOrientation(Float.parseFloat(mAzimuthEditText.getText().toString()),
                    Float.parseFloat(mDipEditText.getText().toString()));
        }
        catch (java.lang.NumberFormatException e) {}
    }
}