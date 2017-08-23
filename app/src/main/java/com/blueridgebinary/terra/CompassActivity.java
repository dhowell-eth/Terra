package com.blueridgebinary.terra;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

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
public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    public SensorManager mSensorManager;
    public SensorEventListener mSensorEventListener;
    public TextView tvAzimuth;
    public TextView tvAccuracy;
    public TextView tvDip;

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


        // Get Text Views for displaying values
        tvAzimuth = (TextView) findViewById(R.id.tv_compass_azi_value);
        tvAccuracy = (TextView) findViewById(R.id.tv_compass_accuracy_value);
        tvDip = (TextView) findViewById(R.id.tv_dip_value);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register Listeners
        // Doing this here should allow the compass to keep running when resuming from a pause
        mSensorManager.registerListener(this, mAccelerometerSensor, mSensorDelay);
        mSensorManager.registerListener(this, mMagnetometerSensor, mSensorDelay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometerSensor);
        mSensorManager.unregisterListener(this, mMagnetometerSensor);
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
                // TODO: TESTING ONLY
                tvAzimuth.setText(String.format(java.util.Locale.US, "%.2f", Math.toDegrees(orientationMatrix[0])));
                tvDip.setText(String.format(java.util.Locale.US, "%.2f", Math.toDegrees(orientationMatrix[2])));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String sensorStatus;
        boolean needsWarning;
        String sensorName = sensor.getStringType();
        Log.d("COMPASS","ACCURACY = " + Integer.toString(accuracy));
        Log.d("COMPASS","SensorManager.SENSOR_STATUS_ACCURACY_HIGH =  " + Integer.toString(SensorManager.SENSOR_STATUS_ACCURACY_HIGH));
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
        setSensorAccuracyText(sensorName, sensorStatus, needsWarning);
    }

    // TODO: this method will do an action based on the accuracy of the compass
    public void setSensorAccuracyText(String sensorName, String sensorStatus, boolean needsWarning) {
        // TODO: TESTING ONLY
        tvAccuracy.setText(sensorName + "\n" + sensorStatus + ".");
        Log.d("COMPASS",Boolean.toString(needsWarning));

    }
}