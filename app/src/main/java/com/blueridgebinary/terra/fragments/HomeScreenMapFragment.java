package com.blueridgebinary.terra.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.blueridgebinary.terra.MainActivity;
import com.blueridgebinary.terra.R;
import com.blueridgebinary.terra.data.CurrentDataset;
import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.loaders.LoaderIds;
import com.blueridgebinary.terra.loaders.LocalityLoaderListener;
import com.blueridgebinary.terra.utils.ListenableInteger;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeScreenMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeScreenMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeScreenMapFragment extends HomeScreenFragment implements
        OnMapReadyCallback,
        OnMarkerClickListener,
        Spinner.OnItemSelectedListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_CURRENTSESSIONID = "currentSessionId";
    private static final String TAG = HomeScreenMapFragment.class.getSimpleName();


    public static final int REQUEST_FINE_PERMISSION = 32323;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private int currentSessionId;

    public ListenableInteger selectedLocalityId;

    private OnTerraFragmentInteractionListener mListener;

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private Spinner mSpinner;

    //private int mapType;

    private LocalityLoaderListener mLocalityLoaderListener;
    private Marker[] mMarkers;

    public HomeScreenMapFragment() {
        // Required empty public constructor
    }

    public static HomeScreenMapFragment newInstance(String param1, int currentSessionId) {
        HomeScreenMapFragment fragment = new HomeScreenMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putInt(ARG_CURRENTSESSIONID, currentSessionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate() called!");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            currentSessionId = getArguments().getInt(ARG_CURRENTSESSIONID);
        }
        mLocalityLoaderListener = new LocalityLoaderListener(this, this.getActivity(), currentSessionId, null);

        // Get current locality and add listener
        selectedLocalityId = ((MainActivity) getActivity()).selectedLocality;
        selectedLocalityId.addListener(new ListenableInteger.ChangeListener() {
            @Override
            public void onChange() {
                int newId = selectedLocalityId.getValue();
                setCurrentLocality(newId);

                getActivity().getSupportLoaderManager().restartLoader(LoaderIds.OVERVIEW_MAP_LOCALITY_LOADER_ID,
                        null,
                        mLocalityLoaderListener);

            }
        });

        //mapType = GoogleMap.MAP_TYPE_NORMAL;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_home_screen_map, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapview_home_map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        Log.d(TAG,"onCreateView() called!");

        MapsInitializer.initialize(getActivity().getApplicationContext());
        mMapView.getMapAsync(this);

        Spinner mSpinner = view.findViewById(R.id.map_fragment_layer_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.map_modes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (this.getView() != null) {
            this.getActivity().getSupportLoaderManager().initLoader(LoaderIds.OVERVIEW_MAP_LOCALITY_LOADER_ID,
                    null,
                    mLocalityLoaderListener);
        }
        // Set a listener for marker click.
        mGoogleMap.setOnMarkerClickListener(this);
        UiSettings settings =  mGoogleMap.getUiSettings();
        settings.setRotateGesturesEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setMapToolbarEnabled(false);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mGoogleMap != null) {
                        mGoogleMap.setMyLocationEnabled(true);
                    }
                }
                return;
            }
        }
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Parse the ID from the marker title and set it as the current locality
        int clickedId = Integer.parseInt(marker.getTitle());
        this.selectedLocalityId.setValue(clickedId);
        this.getActivity().getSupportLoaderManager().restartLoader(LoaderIds.OVERVIEW_MAP_LOCALITY_LOADER_ID,
                null,
                mLocalityLoaderListener);
        Log.d(TAG, "onMarkerClick: clicked a marker" + Integer.toString(clickedId));
        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        marker.showInfoWindow();
        return true;
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
        mLocalityLoaderListener = null;

    }

    public Marker[] addMapMarkersFromLocalityCursor(GoogleMap googleMap, Cursor localityCursor) {

        Marker[] markers;
        if (localityCursor.getCount() < 1) return null;

        markers = new Marker[localityCursor.getCount()];

        int latIndex = localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_LAT);
        int longIndex = localityCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_LONG);
        int idIndex = localityCursor.getColumnIndex((TerraDbContract.LocalityEntry._ID));
        localityCursor.moveToFirst();
        int i = 0;
        while (!localityCursor.isAfterLast()) {
            Log.d(TAG,"ADDING MARKERS for i=" + Integer.toString(i));
            double lat = localityCursor.getDouble(latIndex);
            double lon = localityCursor.getDouble(longIndex);
            int  id = localityCursor.getInt(idIndex);

            MarkerOptions options = new MarkerOptions();
            options.position(new LatLng(lat,lon)).title(Integer.toString(id));
            Log.d(TAG, "addMapMarkersFromLocalityCursor: " + selectedLocalityId.toString());
            if (id == selectedLocalityId.getValue()) {

                 options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }

            markers[i] = googleMap.addMarker(options);
            if (id == selectedLocalityId.getValue()) {
                markers[i].showInfoWindow();
            }

            localityCursor.moveToNext();
            ++i;
        }
        return markers;
    }

    public void setMapExtentToMarkers(GoogleMap map, Marker[] markers, MapView parentView) {
        //Calculate the markers to get their position
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (Marker m : markers) {
            b.include(m.getPosition());
        }
        LatLngBounds bounds = b.build();
        int width = parentView.getResources().getDisplayMetrics().widthPixels;
        int height = parentView.getResources().getDisplayMetrics().heightPixels;
        //Change the padding as per needed
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width,height,(int) Math.round(width*0.25));
        map.animateCamera(cu);
    }


    @Override
    public void updateLocalityUI() {

    }

    public void resetMapForNewData() {
        this.mGoogleMap.clear();
    }

    @Override
    public void handleNewLocalityData(Cursor cursor, boolean isSingleQuery) {
        if (cursor != null && this.mGoogleMap != null) {
            resetMapForNewData();
            this.mMarkers = this.addMapMarkersFromLocalityCursor(this.mGoogleMap, cursor);
            if (mMarkers != null) this.setMapExtentToMarkers(mGoogleMap, mMarkers, mMapView);
        }
    }

    @Override
    public void updateSessionUI() {

    }

    @Override
    public void handleNewSessionData(Cursor cursor) {

    }

    @Override
    public void setCurrentLocality(int localityId) {
/*        // Restart single locality loader using a new listener that has the latest selectedLocalityId
        this.getActivity().getSupportLoaderManager().restartLoader(LoaderIds.SINGLE_LOCALITY_LOADER_ID,
                null,
                new LocalityLoaderListener(this,this.getActivity(),currentSessionId,null);*/
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch(position) {
            case 0:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case 2:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
