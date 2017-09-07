package com.blueridgebinary.terra.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeScreenMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeScreenMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeScreenMapFragment extends HomeScreenFragment implements OnMapReadyCallback{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_CURRENTSESSIONID = "currentSessionId";
    private static final String TAG = HomeScreenMapFragment.class.getSimpleName();


    // TODO: Rename and change types of parameters
    private String mParam1;
    private int currentSessionId;

    public ListenableInteger selectedLocalityId;

    private OnTerraFragmentInteractionListener mListener;

    private GoogleMap mGoogleMap;
    private MapView mMapView;
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
            }
        });

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
        UiSettings settings =  mGoogleMap.getUiSettings();
        settings.setRotateGesturesEnabled(true);
        settings.setZoomControlsEnabled(true);

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
            markers[i] = googleMap.addMarker(options);
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
        if (cursor != null) {
            resetMapForNewData();
            this.mMarkers = this.addMapMarkersFromLocalityCursor(this.mGoogleMap, cursor);
            if (mMarkers != null) this.setMapExtentToMarkers(mGoogleMap, mMarkers, mMapView);
        }
    }

    @Override
    public void setCurrentLocality(int localityId) {
        Log.d(TAG,"I was notified via listener that the currently selected locality has changed to:  " + Integer.toString(localityId));
    }

    @Override
    public void updateSessionUI() {

    }

    @Override
    public void handleNewSessionData(Cursor cursor) {

    }
}
