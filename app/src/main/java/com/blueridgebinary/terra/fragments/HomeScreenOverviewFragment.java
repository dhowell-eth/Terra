package com.blueridgebinary.terra.fragments;

import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.blueridgebinary.terra.AddEditLocalityActivity;
import com.blueridgebinary.terra.MainActivity;
import com.blueridgebinary.terra.R;
import com.blueridgebinary.terra.data.CurrentDataset;
import com.blueridgebinary.terra.data.CurrentLocality;
import com.blueridgebinary.terra.data.CurrentSession;
import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.loaders.LoaderIds;
import com.blueridgebinary.terra.loaders.LocalityLoaderListener;
import com.blueridgebinary.terra.loaders.SessionLoaderListener;

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

    // TODO: Rename and change types of parameters
    private String currentSessionName;
    private Integer currentSessionId;
    private Integer currentLocalityId;

    CurrentSession currentSession;
    CurrentLocality currentLocality;

    private OnTerraFragmentInteractionListener mListener;

    private Spinner mSpinner;
    private TextView tvSessionName;
    private ImageButton imbtToggleGps;

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
                new LocalityLoaderListener(this,currentSessionId,null));
        // Start Single Locality Loader
        this.getActivity().getSupportLoaderManager().initLoader(LoaderIds.SINGLE_LOCALITY_LOADER_ID,
                null,
                new LocalityLoaderListener(this,currentSessionId,currentLocalityId));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentSessionName = getArguments().getString(ARG_CURRENTSESSIONNAME);
            currentSessionId = getArguments().getInt(ARG_CURRENTSESSIONID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home_screen_overview, container, false);
        //  Get UI Components
        tvSessionName = (TextView) v.findViewById(R.id.tv_home_overview_title);
        tvLat = (TextView) v.findViewById(R.id.tv_home_overview_lat);
        tvLong = (TextView) v.findViewById(R.id.tv_home_overview_long);
        tvAcc = (TextView) v.findViewById(R.id.tv_home_overview_accuracy);
        tvNotes = (TextView) v.findViewById(R.id.tv_home_overview_desc);

        // Set Project Name Text
        tvSessionName.setText(currentSessionName);
        // Pre-set Current Locality
        currentLocalityId = ((MainActivity) getActivity()).getCurrentLocalityId();
        // TODO: Load and populate Locality details

        // TODO: Get Buttons and set onclick listeners
        imbtToggleGps = (ImageButton) v.findViewById(R.id.imbt_home_overview_new_station);
        imbtToggleGps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddEditLocalityActivity.class);
                intent.putExtra("sessionId",currentSessionId);
                intent.putExtra("sessionName",currentSessionName);
                startActivity(intent);
            }
        });


        // Populate Spinner
        mSpinner = (Spinner) v.findViewById(R.id.home_spinner_locality);
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
        }
        // If the cursor has more than one locality, it is for the spinner
        if (!isSingleQuery) {
            SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(this.getContext(),
                    android.R.layout.simple_spinner_item,
                    cursor,
                    new String[]{TerraDbContract.LocalityEntry._ID},
                    new int[] {android.R.id.text1},
                    0); // Not sure about this
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(spinnerAdapter);

            // TODO:  add onItemSelected() listener for Spinner

        }

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


    // Define loader and respective callbacks to be used by this activity
    // this can eventually get moved out into separate files for organizational purposes

   /* // <-----   Session Data Loader ----->
    private LoaderManager.LoaderCallbacks<Cursor> sessionLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    Uri baseUri;
                    // sessionId defaults to 0 if no id is passed with the
                    // intent that creates this activity
                    if (currentSessionId != 0) {
                        // Querying the uri for this specific session (that way listener works properly)
                        baseUri = Uri.withAppendedPath(TerraDbContract.SessionEntry.CONTENT_URI,Uri.encode(Integer.toString(currentSessionId)));
                    } else {
                        // don't do anything if this activity doesn't have a sessionId
                        return null;
                    }
                    return new CursorLoader(getContext(), baseUri, null,
                            null,null,null);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    // Populate current Session object

                    //
                    Log.d("Loader-DEBUG","called onLoadFinished()!");
                    sessionCursorToCurrentSession(data);
                    updateSessionUiComponents();
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    // Clear data for the current session
                    // TODO: not sure if I also need to store this data in this activity or if it can just sit in the adapter
                    currentSession = null;
                }
            };*/

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
