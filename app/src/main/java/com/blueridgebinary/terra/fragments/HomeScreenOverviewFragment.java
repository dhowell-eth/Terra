package com.blueridgebinary.terra.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.blueridgebinary.terra.AddEditLocalityActivity;
import com.blueridgebinary.terra.CompassActivity;
import com.blueridgebinary.terra.MainActivity;
import com.blueridgebinary.terra.R;
import com.blueridgebinary.terra.data.CurrentDataset;
import com.blueridgebinary.terra.data.CurrentLocality;
import com.blueridgebinary.terra.data.CurrentSession;
import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.loaders.LoaderIds;
import com.blueridgebinary.terra.loaders.LocalityLoaderListener;
import com.blueridgebinary.terra.loaders.SessionLoaderListener;
import com.blueridgebinary.terra.utils.ListenableInteger;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeScreenOverviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeScreenOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeScreenOverviewFragment extends HomeScreenFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CURRENTSESSIONNAME = "currentSessionName";
    private static final String ARG_CURRENTSESSIONID = "currentSessionId";
    private static final String TAG = HomeScreenOverviewFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String currentSessionName;
    private Integer currentSessionId;
//    private Integer currentLocalityId;

    public ListenableInteger selectedLocalityId;

    CurrentSession currentSession;
    CurrentLocality currentLocality;

    private OnTerraFragmentInteractionListener mListener;

    private Spinner mSpinner;
    private TextView tvSessionName;
    private ImageButton imbtToggleGps;
    private ImageButton imbtNotes;
    private ImageButton imbtEditLocality;
    private ImageButton imbtCompass;


    private TextView tvLat;
    private TextView tvLong;
    private TextView tvAcc;
    private TextView tvNotes;

    public HomeScreenOverviewFragment() {
        // Required empty public constructor
    }

    public static HomeScreenOverviewFragment newInstance(String currentSessionName, int currentSessionId) {
        HomeScreenOverviewFragment fragment = new HomeScreenOverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CURRENTSESSIONNAME, currentSessionName);
        args.putInt(ARG_CURRENTSESSIONID, currentSessionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Start Session Loader
        this.getActivity().getSupportLoaderManager().initLoader(LoaderIds.SESSION_LOADER_ID,null,new SessionLoaderListener(this,currentSessionId));
        // Start All Localities Loader
        this.getActivity().getSupportLoaderManager().initLoader(LoaderIds.LOCALITY_LOADER_ID,
                null,
                new LocalityLoaderListener(this,this.getActivity(),currentSessionId,null));
        // Start Single Locality Loader
        this.getActivity().getSupportLoaderManager().initLoader(LoaderIds.SINGLE_LOCALITY_LOADER_ID,
                null,
                new LocalityLoaderListener(this,this.getActivity(),currentSessionId,selectedLocalityId.getValue()));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentSessionName = getArguments().getString(ARG_CURRENTSESSIONNAME);
            currentSessionId = getArguments().getInt(ARG_CURRENTSESSIONID);
        }

        // Get current locality and add listener
        selectedLocalityId = ((MainActivity) getActivity()).selectedLocality;
        selectedLocalityId.addListener(new ListenableInteger.ChangeListener() {
            @Override
            public void onChange() {
                int newId = selectedLocalityId.getValue();
                setCurrentLocality(newId);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home_screen_overview, container, false);

        Log.d(TAG,"onCreateView() called!");

        //  Get UI Components
        tvSessionName = (TextView) v.findViewById(R.id.tv_home_overview_title);
        tvLat = (TextView) v.findViewById(R.id.tv_home_overview_lat);
        tvLong = (TextView) v.findViewById(R.id.tv_home_overview_long);
        tvAcc = (TextView) v.findViewById(R.id.tv_home_overview_accuracy);
        tvNotes = (TextView) v.findViewById(R.id.tv_home_overview_desc);

        // Get Spinner
        mSpinner = (Spinner) v.findViewById(R.id.home_spinner_locality);


        // Set Project Name Text
        tvSessionName.setText(currentSessionName);

        // TODO: Get Buttons and set onclick listeners
        imbtToggleGps = (ImageButton) v.findViewById(R.id.imbt_home_overview_new_station);
        imbtToggleGps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddEditLocalityActivity.class);
                intent.putExtra("sessionId",currentSessionId);
                intent.putExtra("sessionName",currentSessionName);
                intent.putExtra("localityId",selectedLocalityId.getValue());
                intent.putExtra("isCreateNewLocality",true);
                startActivity(intent);
            }
        });

        imbtEditLocality = (ImageButton) v.findViewById(R.id.imbt_home_overview_update_location);
        imbtEditLocality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isALocalitySelected()) {return;}
                Intent intent = new Intent(getActivity(), AddEditLocalityActivity.class);
                intent.putExtra("sessionId",currentSessionId);
                intent.putExtra("sessionName",currentSessionName);
                intent.putExtra("localityId",selectedLocalityId.getValue());
                intent.putExtra("isCreateNewLocality",false);
                startActivity(intent);
            }
        });

        imbtCompass = (ImageButton) v.findViewById(R.id.imbt_home_overview_compass);
        imbtCompass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isALocalitySelected()) {return;}
                Intent intent = new Intent(getActivity(), CompassActivity.class);
                intent.putExtra("sessionId",currentSessionId);
                intent.putExtra("sessionName",currentSessionName);
                intent.putExtra("localityId",selectedLocalityId.getValue());
                startActivity(intent);
            }
        });


        // Set scrolling for notes text incase someone writes a ton
        tvNotes.setMovementMethod(new ScrollingMovementMethod());
        // Set onclick listener for the notes button
        imbtNotes = (ImageButton) v.findViewById(R.id.imbt_home_overview_notes);
        imbtNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isALocalitySelected()) {return;}
                // Create builder for notes entry and assign to notes button
                AlertDialog.Builder builder = createEnterNotesDialog();
                builder.show();
            }
        });

        // Return the view for this fragment (required)
        return v;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String tag, String fakeData) {
        if (mListener != null) {
            mListener.onFragmentInteraction(tag, fakeData);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTerraFragmentInteractionListener) {
            mListener = (OnTerraFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mSpinner.setAdapter(null);
    }

    private void updateSessionUiComponents() {
        //
    }

    private boolean isALocalitySelected() {
        if (this.selectedLocalityId.getValue() == 0) {
            Toast.makeText(this.getActivity(), "Error: You must create/select a locality to add data!",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        else {return true;}
    }

    // TODO: Remove this function and all references to it (use the one below)
    private void updateLocalityUiComponents() {
        if (currentLocality != null) {
            tvLat.setText(String.format(Locale.US,"%.6f",currentLocality.getLatitude()));
            tvLong.setText(String.format(Locale.US,"%.6f",currentLocality.getLongitude()));
            tvAcc.setText(String.format(Locale.US,"%.6f",currentLocality.getAccuracy()));
            tvNotes.setText(currentLocality.getLocalityNotes());
        }
    }

    @Override
    public void updateLocalityUI() {
        if (currentLocality != null) {
            tvLat.setText(String.format(Locale.US,"%.6f",currentLocality.getLatitude()));
            tvLong.setText(String.format(Locale.US,"%.6f",currentLocality.getLongitude()));
            tvAcc.setText(String.format(Locale.US,"%.6f",currentLocality.getAccuracy()));
            tvNotes.setText(currentLocality.getLocalityNotes());
        }
    }

    @Override
    public void handleNewLocalityData(Cursor cursor,boolean isSingleQuery) {

        // If cursor is null, data is no longer available and you need to disconnect any adapters, etc
        if (cursor == null) {
            mSpinner.setAdapter(null);
            return;
        }

        // If the cursor queried all localities, it is for the spinner
        if (!isSingleQuery) {
            SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(this.getContext(),
                    android.R.layout.simple_spinner_item,
                    cursor,
                    new String[]{TerraDbContract.LocalityEntry._ID},
                    new int[] {android.R.id.text1},
                    0); // Not sure about this
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(spinnerAdapter);
            // Default item is the last one (i.e. most recently added station)
            mSpinner.setSelection(mSpinner.getAdapter().getCount()-1);

            // TODO:  add onItemSelected() listener for Spinner
            mSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(getContext(), "CLICKED ITEM: " + Long.toString(id), Toast.LENGTH_SHORT).show();
                    selectedLocalityId.setValue((int) id);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
        else {

            Log.d(TAG,"Received data for a single Locality query, current locality = " + Integer.toString(selectedLocalityId.getValue()));
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                // Otherwise it is the data for the current locality
                int latIndex = cursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_LAT);
                int lonIndex = cursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_LONG);
                int accIndex = cursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_GPSACCURACY);
                int notesIndex = cursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_NOTES);

                tvLat.setText(String.format(Locale.US, "%.6f", cursor.getDouble(latIndex)));
                tvLong.setText(String.format(Locale.US, "%.6f", cursor.getDouble(lonIndex)));
                tvAcc.setText(String.format(Locale.US, "%.1f", cursor.getDouble(accIndex)));
                tvNotes.setText(cursor.getString(notesIndex));
            }
        }

    }

    @Override
    public void setCurrentLocality(int localityId) {
        // Restart single locality loader using a new listener that has the latest selectedLocalityId
        this.getActivity().getSupportLoaderManager().restartLoader(LoaderIds.SINGLE_LOCALITY_LOADER_ID,
                null,
                new LocalityLoaderListener(this,this.getActivity(),currentSessionId,selectedLocalityId.getValue()));
    }

    @Override
    public void updateSessionUI() {

    }

    @Override
    public void handleNewSessionData(Cursor cursor) {

    }

    // --------------- Class for each Spinner Entry-----------------------
    private class LocalitySpinnerItem {
        private String localityName;
        private int localityId;

        public LocalitySpinnerItem() {
        }

        public LocalitySpinnerItem(String localityName, int localityId) {
            this.localityName = localityName;
            this.localityId = localityId;
        }

        public String getLocalityName() {
            return localityName;
        }

        public void setLocalityName(String localityName) {
            this.localityName = localityName;
        }

        public int getLocalityId() {
            return localityId;
        }

        public void setLocalityId(int localityId) {
            this.localityId = localityId;
        }

        @Override
        public String toString() {
            return localityName;
        }
    }

    public AlertDialog.Builder createEnterNotesDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("Add/Edit Notes");

        // Set up the input
        final EditText input = new EditText(this.getActivity());
        input.setText(tvNotes.getText());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            // TODO: Set the below onClick to write to the DB
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues contentValues = new ContentValues();
                // Put the task description and selected mPriority into the ContentValues
                contentValues.put(TerraDbContract.LocalityEntry.COLUMN_NOTES, input.getText().toString());
                // Insert the content values via a ContentResolver
                Uri baseUri = TerraDbContract.LocalityEntry.CONTENT_URI;
                Uri  idUri = baseUri.buildUpon().appendPath(selectedLocalityId.toString()).build();
                // Execute Update statement
                int rowsUpdated = getActivity().getContentResolver().update(idUri, contentValues,null,null);

                Log.d(TAG,"UPDATED NOTES #ROWS = " + Integer.toString(rowsUpdated));
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder;
    }

    public void sessionCursorToCurrentSession(Cursor sessionCursor) {

        Log.d("NewURIDEBUG",Integer.toString(sessionCursor.getCount()) + "Records in Cursor");
        Log.d("NewURIDEBUG","Got cursor and am trying to retrieve data");
        if (sessionCursor.getCount() != 1) return;
        sessionCursor.moveToFirst();

        int idIndex = sessionCursor.getColumnIndex(TerraDbContract.SessionEntry._ID);
        int nameIndex = sessionCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_SESSIONNAME);
        int notesIndex = sessionCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_NOTES);
        int updatedIndex = sessionCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_UPDATED);
        int createdIndex = sessionCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_CREATED);

        int id = sessionCursor.getInt(idIndex);
        String name = sessionCursor.getString(nameIndex);
        String notes = sessionCursor.getString(notesIndex);
        String updated = sessionCursor.getString(updatedIndex);
        String created = sessionCursor.getString(createdIndex);

        currentSession = new CurrentSession(id,name,notes,updated,created);
        Log.d("NewURIDEBUG","Loaded Session:" + currentSession.getSessionName());
    }

    public void localityCursorToCurrentLocality(Cursor localityCursor) {

        Log.d("NewURIDEBUG",Integer.toString(localityCursor.getCount()) + "Records in Cursor");
        Log.d("NewURIDEBUG","Got cursor and am trying to retrieve data");

        if (localityCursor.getCount() != 1) return;
        localityCursor.moveToFirst();

        int idIndex = localityCursor.getColumnIndex(TerraDbContract.LocalityEntry._ID);
        int latIndex = localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_LAT);
        int longIndex = localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_LONG);
        int elevationIndex = localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_ELEVATION);
        int accuracyIndex = localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_GPSACCURACY);
        int notesIndex = localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_NOTES);
        int updatedIndex = localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_UPDATED);
        int createdIndex = localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_CREATED);

        int id = localityCursor.getInt(idIndex);
        double lat = localityCursor.getDouble(latIndex);
        double lon = localityCursor.getDouble(longIndex);
        double elev = localityCursor.getDouble(elevationIndex);
        double acc = localityCursor.getDouble(accuracyIndex);
        String notes = localityCursor.getString(notesIndex);
        String updated = localityCursor.getString(updatedIndex);
        String created = localityCursor.getString(createdIndex);

        currentLocality = new CurrentLocality(id,lat,lon,acc,elev,notes,created,updated);
    }

}
