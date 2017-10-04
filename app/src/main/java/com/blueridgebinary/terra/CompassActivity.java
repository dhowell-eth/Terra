package com.blueridgebinary.terra;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.fragments.MeasurementCategoryUi;
import com.blueridgebinary.terra.loaders.LoaderIds;
import com.blueridgebinary.terra.loaders.MeasurementCategoryLoaderListener;
import com.blueridgebinary.terra.utils.ListenableBoolean;
import com.blueridgebinary.terra.utils.util;

import org.w3c.dom.Text;

import java.util.Arrays;
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
public class CompassActivity extends AppCompatActivity implements
        SensorEventListener,
        MeasurementCategoryUi{

    static private final String TAG = CompassActivity.class.getSimpleName();

    private int sessionId;
    private int localityId;


    private int currentMeasurementCategoryId;
    private String preferredMeasurementCategoryName;
    private Context mContext;

    private CompassView mCompassView;
    private EditText mAzimuthEditText;
    private EditText mDipEditText;
    private Spinner mCompassMeasurementSpinner;
    private Spinner mCompassModeSpinner;
    private Button mOkButton;
    private ImageButton mNewMeasurementCategoryImageButton;
    private ImageView mLeftAlertBarImageView;
    private ImageView mRightAlertBarImageView;
    private TextView mDipTextView;
    private AlertDialog.Builder mDialogBuilder;

    public  ListenableBoolean isEnabled;

    public SensorManager mSensorManager;

    public Sensor mAccelerometerSensor;
    public Sensor mMagnetometerSensor;
    public Sensor mGravitySensor;

    public int mSensorDelay;
    private int currentAccuracy;

    public float[] orientationMatrix;
    public float[] geomagneticMatrix;
    public float[] gravityMatrix;
    public float[] rRotationMatrix;
    public float[] iRotationMatrix;

    private float[] xAxis = {1,0,0};
    private float[] yAxis = {0,1,0};
    private float[] zAxis = {0,0,1};

    public float aziDeg;
    public float dipDeg;
    public float apparentAziDeg;

    private boolean shouldDisplayedAccuracyToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        // Set SessionId from Extras
        this.sessionId = getIntent().getIntExtra("sessionId",0);
        this.localityId =  getIntent().getIntExtra("localityId",0);

        // Get UI Components
        mCompassView = (CompassView) findViewById(R.id.compass_view_add);
        mAzimuthEditText = (EditText) findViewById(R.id.et_compass_azi);
        mDipEditText = (EditText) findViewById(R.id.et_compass_dip);
        mOkButton = (Button) findViewById(R.id.btn_compass_ok);
        mCompassModeSpinner = (Spinner) findViewById(R.id.spinner_compass_mode);
        mCompassMeasurementSpinner = (Spinner) findViewById(R.id.spinner_compass_measurement);
        mDipTextView = (TextView) findViewById(R.id.tv_compass_dip_label);
        mNewMeasurementCategoryImageButton = (ImageButton) findViewById(R.id.imbt_compass_add_meas_cat);
        mLeftAlertBarImageView = (ImageView) findViewById(R.id.iv_alertbar_left);
        mRightAlertBarImageView = (ImageView) findViewById(R.id.iv_alertbar_right);

        // Initialize accuracy toast flag
        shouldDisplayedAccuracyToast = true;

        // Set edit text input types and default them to off (only populated by compass view)
        mAzimuthEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        mDipEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        toggleEditTextEnabled(false);

        // Start MeasurementCategory loader
        getSupportLoaderManager().initLoader(LoaderIds.COMPASS_MEAS_CATEGORY_LOADER_ID,null,new MeasurementCategoryLoaderListener(this, this, sessionId));

        // Get Sensor Manager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get Sensors
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        // Set Sensor Delay
        // Default is 200,000 us, currently set to 500,000 = 0.5s
        mSensorDelay = 500000;//SensorManager.SENSOR_DELAY_UI;
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

        mContext = this;
        mNewMeasurementCategoryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new dialog and show it
                mDialogBuilder = createAddMeasurementCategoryDialog(mContext);
                mDialogBuilder.show();
            }
        });

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
                    mDipTextView.setVisibility(View.GONE);
                }
                else {
                    mDipEditText.setVisibility(View.VISIBLE);
                    mDipTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                // If the sensor manager successfully retrieved new data
                orientationMatrix = SensorManager.getOrientation(rRotationMatrix, new float[3]);
                // Format new data based on the current mode of the compass view
                float[] formattedOrientationData = convertRawOrientationToViewOrientation(mCompassView.getNeedleModeId());
                // Convert angles to degres
                aziDeg = (float) Math.toDegrees(formattedOrientationData[0]);
                dipDeg = (float) Math.toDegrees(formattedOrientationData[1]);
                // Make sure all angles range from 0 to 360
                if (aziDeg < 0) aziDeg = aziDeg + 360;

                // Get the apparent azimuth to be used by the compass view.  This is the same as the actual azimuth except
                // for in cases where the compass is set to the "Plane" recording mode.
                if (mCompassView.getNeedleModeId() == 3) {
                    apparentAziDeg = (float) Math.toDegrees(getApparentCompassAzimuth());
                }
                else {
                    apparentAziDeg = aziDeg;
                }
                updateWithNewCompassData();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        refreshAccuracyAlertBars(accuracy);
    }

    // TODO: this method will do an action based on the accuracy of the compass
    public void displaySensorAccuracyWarning() {
        String message = getResources().getString(R.string.compass_sensor_acc_warning);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Method for creating an Add Measurement dialog
    public AlertDialog.Builder createAddMeasurementCategoryDialog(Context context) {
        final Context mContext = context;
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        // Inflate it's view
        View view =  inflater.inflate(R.layout.dialog_add_measurement_category,null);
        builder.setView(view);

        // Get UI Components from view
        final EditText inputName = (EditText) view.findViewById(R.id.et_add_meas_cat_name);
        final EditText inputDesc = (EditText) view.findViewById(R.id.et_add_meas_cat_desc);
        // Specify the type of input expected
        inputName.setInputType(InputType.TYPE_CLASS_TEXT);
        inputDesc.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        builder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues contentValues = new ContentValues();
                String dateTimestamp =  util.getDateTime();
                String name = inputName.getText().toString();
                String notes = inputDesc.getText().toString();
                // If no name, send a TOAST to tell the user they must put a name
                if (name.equals("")) {
                    Toast.makeText(mContext,
                            "Error: You must include a name for the new measurement category.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // Put the task description and selected mPriority into the ContentValues
                contentValues.put(TerraDbContract.MeasurementCategoryEntry.COLUMN_SESSIONID, sessionId);
                contentValues.put(TerraDbContract.MeasurementCategoryEntry.COLUMN_NAME, name);
                contentValues.put(TerraDbContract.MeasurementCategoryEntry.COLUMN_NOTES, notes);
                contentValues.put(TerraDbContract.MeasurementCategoryEntry.COLUMN_CREATED, dateTimestamp);
                contentValues.put(TerraDbContract.MeasurementCategoryEntry.COLUMN_UPDATED, dateTimestamp);
                // Insert the content values via a ContentResolver
                Uri uri = getContentResolver().insert(TerraDbContract.MeasurementCategoryEntry.CONTENT_URI, contentValues);
                // Pass the name of the new entry back to the activity for it to use in the UI
                handleNewMeasurementCategoryFromDialog(name);
                // Close the dialogs
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder;
    }

    public void handleNewMeasurementCategoryFromDialog(String newCategoryName) {
        preferredMeasurementCategoryName = newCategoryName;
        attemptToSetPreferredMeasurementCategory();
    }


    public void updateWithNewCompassData() {
        mAzimuthEditText.setText(String.format(Locale.US,"%.2f",aziDeg));
        mDipEditText.setText(String.format(Locale.US,"%.2f",dipDeg));
        mCompassView.setOrientation(apparentAziDeg,dipDeg);
    }

    public float[] convertRawOrientationToViewOrientation(int compassMode) {
        float[] results = new float[2];
        switch (compassMode) {
            case 1:
                results[0] = orientationMatrix[0];
                results[1] = 0;
                break;
            case 2:
                // Get -y device direction if rotation is negative
                if (orientationMatrix[1] < 0) {
                    results[0] = orientationMatrix[0] + (float) Math.PI;
                }
                // Otherwise use the +y direction
                else {
                    results[0] = orientationMatrix[0];
                }
                results[1] = orientationMatrix[1];
                break;
            case 3:
                results = getDipDirection();
                break;
        }
        return results;
    }

    public float[] applyLength3MatrixTransformation(float[] trans,float[] vect) {
        float[] out = new float[3];
        out[0] = vect[0]*trans[0] + vect[1]*trans[1] + vect[2]*trans[2];
        out[1] = vect[0]*trans[3] + vect[1]*trans[4] + vect[2]*trans[5];
        out[2] = vect[0]*trans[6] + vect[1]*trans[7] + vect[2]*trans[8];
        return out;
    }

    public float signedAngleBetween2VectorsRadians(float[] v1, float[] v2, float[] vn) {
        // Calculates the signed angle in radians between two 3d vectors with the same origin
        // v1 --theta--> v2 assuming both vectors lie in the XY plane.
        // Uses the relation: atan2((V2 x V1) . Vn, V1 . V2)
        // where Vn is a unit length reference vector for determining sign
        return (float) Math.atan2(dotProduct(crossProduct(v2,v1), vn),dotProduct(v1,v2));
    }


    /*
    Calculates the apparent azimuth to be used on the compass widget when in plane measurement mode.
    This azimuth is the azimuth a horizontal line lying within the device plane (aka a level line) */
    public float getApparentCompassAzimuth() {
        // Define device Y Vector, want to get the angle between the dip direction and this
        float[] deviceYVector = {0,1,0};
        // Get the dip direction from the gravity vector
        float[] dipDirection= {gravityMatrix[0],gravityMatrix[1],0f};
        // Normalize the length of the dip direction vector to length 1
        float mag = (float) (Math.sqrt(Math.pow(dipDirection[0],2) + Math.pow(gravityMatrix[1],2)));
        dipDirection[0] = dipDirection[0] / mag;
        dipDirection[1] = dipDirection[1] / mag;
        // DISPLAY ANGLE = PI - ANGLE(DIP DIRECTION --> Y AXIS)
        return (float) (Math.PI - (Math.atan2(dipDirection[1],dipDirection[0]) - Math.atan2(deviceYVector[1],deviceYVector[0])));
    }

    /*  Returns the dip direction of the device in world coordinates
    return: length 2 float[]
    output[0] = azimuth (deg)
    output[1] = dip (deg)*/
    public float[] getDipDirection() {
        float[] out = new float[2];
        // Device dip direction in device coordinates (steepest vector in the device plane)
        float[] dipDirectionDeviceCoords = {gravityMatrix[0],gravityMatrix[1],0f};
        // Device dip direction in world coordinates
        float[] dipDirectionWorldCoords = applyLength3MatrixTransformation(rRotationMatrix,dipDirectionDeviceCoords);
        // Device dip direction projected onto the world x,y plane (i.e. horizontal plane)
        float[] dipDirectionInWorldXyPlane = {dipDirectionWorldCoords[0],dipDirectionWorldCoords[1],0f};
        // Dip Direction Azimuth (Angle between y-axis and dip direction)
        out[0] = signedAngleBetween2VectorsRadians(yAxis,dipDirectionInWorldXyPlane,zAxis) + (float) Math.PI;
        // Dip Angle (Calculated as the angle between the dip direction and it's projection in the XY plane)
        out[1] = signedAngleBetween2VectorsRadians(dipDirectionWorldCoords,
                dipDirectionInWorldXyPlane,
                normalizeVector(crossProduct(dipDirectionInWorldXyPlane,dipDirectionWorldCoords)));
        // If Dip is 90, get azimuth using the device y-axis
        if (Float.isNaN(out[0])) out[0] = orientationMatrix[0];
        // If the dip angle comes out as NaN, device is horizontal so set dip to 0
        if (Float.isNaN(out[1])) out[1] = 0;
        return out;
    }

    public void toggleEditTextEnabled(boolean enable) {
        mAzimuthEditText.setEnabled(enable);
        mDipEditText.setEnabled(enable);
    }

    @Override
    public void handleNewMeasurementCategoryData(Cursor data) {
        // If cursor is null, data is no longer available and you need to disconnect any adapters, etc
        if (data == null) {
            mCompassMeasurementSpinner.setAdapter(null);
            currentMeasurementCategoryId = 0;
            return;
        }

        // Otherwise populate the spinner with the entries from the DB
        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                data,
                new String[]{TerraDbContract.MeasurementCategoryEntry.COLUMN_NAME},
                new int[] {android.R.id.text1},
                0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCompassMeasurementSpinner.setAdapter(spinnerAdapter);
        // Set current item to preferred
        attemptToSetPreferredMeasurementCategory();
        // Default item is the first one
        mCompassMeasurementSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentMeasurementCategoryId = (int) id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void attemptToSetPreferredMeasurementCategory() {
        if (preferredMeasurementCategoryName != null) {
            // If there is a preferred measurement category id, find the right spinner entry and set it as the current one
            for (int i=0; i<mCompassMeasurementSpinner.getCount(); i++) {
                Cursor thisData = (Cursor) mCompassMeasurementSpinner.getItemAtPosition(i);
                String thisEntry = thisData.getString(thisData.getColumnIndex(TerraDbContract.MeasurementCategoryEntry.COLUMN_NAME));
                if (thisEntry.equals(preferredMeasurementCategoryName)) {
                    mCompassMeasurementSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    public void refreshAccuracyAlertBars(int accuracy) {

        int newColor;
        int newDisplay;
        boolean isPoorAccuracy;
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                newColor = ContextCompat.getColor(this,R.color.alertRed);
                newDisplay = View.VISIBLE;
                isPoorAccuracy = true;
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                newColor = ContextCompat.getColor(this,R.color.alertYellow);
                newDisplay = View.VISIBLE;
                isPoorAccuracy = true;
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                newColor = ContextCompat.getColor(this,android.R.color.transparent);
                newDisplay = View.GONE;
                isPoorAccuracy = false;
                // Set flag so the next time the device detects poor accuracy a toast will be sent
                shouldDisplayedAccuracyToast = true;
                break;
            default:
                newColor = ContextCompat.getColor(this,android.R.color.transparent);
                newDisplay = View.GONE;
                isPoorAccuracy = false;
                shouldDisplayedAccuracyToast = true;
        };
        // Set Colors
        mLeftAlertBarImageView.setImageDrawable(new ColorDrawable(newColor));
        mRightAlertBarImageView.setImageDrawable(new ColorDrawable(newColor));
        // Set Visibility
        mLeftAlertBarImageView.setVisibility(newDisplay);
        mRightAlertBarImageView.setVisibility(newDisplay);
        // Send the user a toast if the accuracy is low
        if (shouldDisplayedAccuracyToast && isPoorAccuracy) {
            displaySensorAccuracyWarning();
            shouldDisplayedAccuracyToast = false;
        }
    }

    public float[] crossProduct(float[] a,float[] b) {
        float[] out = new float[3];
        out[0] = ( a[1]*b[2] -b[1]*a[2] );
        out[1] = -1*( a[0]*b[2] - a[2]*b[0]);
        out[2] = ( a[0]*b[1] - a[1]*b[0]);
        return out;
    }

    public float[] normalizeVector(float[] v) {
        float[] out = new float[3];
        float mag = (float) Math.sqrt(Math.pow(v[0],2.0) + Math.pow(v[1],2.0) + Math.pow(v[2],2.0));
        for(int i=0; i<v.length;i++) {
            out[i] = v[i]/mag;
        }
        return out;
    }

    public float dotProduct(float[] a,float[] b) {
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
    }

    // Set Physical Key Press Events
    // Clicking the volume down key toggles the compass on/off
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    mCompassView.setEnabled(!mCompassView.isEnabled.getValue());
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

}