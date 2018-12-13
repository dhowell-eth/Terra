package com.blueridgebinary.terra.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.blueridgebinary.terra.DataScreenActvity;
import com.blueridgebinary.terra.R;
import com.blueridgebinary.terra.adapters.HomeLocalityListCursorAdapter;
import com.blueridgebinary.terra.adapters.SessionCursorAdapter;
import com.blueridgebinary.terra.data.CurrentDataset;
import com.blueridgebinary.terra.loaders.LoaderIds;
import com.blueridgebinary.terra.loaders.LocalityLoaderListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeScreenDataOverviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeScreenDataOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeScreenDataOverviewFragment extends HomeScreenFragment implements
        HomeLocalityListCursorAdapter.LocalityAdapterOnClickHandler {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_CURRENTSESSIONID = "currentSessionId";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private int currentSessionId;

    private static final String TAG = HomeScreenDataOverviewFragment.class.getSimpleName();

    private OnTerraFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    public HomeLocalityListCursorAdapter mAdapter;
    private LocalityLoaderListener mLocalityLoaderListener;

    public HomeScreenDataOverviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeScreenDataOverviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeScreenDataOverviewFragment newInstance(String param1, int currentSessionId) {
        HomeScreenDataOverviewFragment fragment = new HomeScreenDataOverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putInt(ARG_CURRENTSESSIONID, currentSessionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG,"CALLED onActivityCreated!");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"CALLED onViewCreated!");
        View v = inflater.inflate(R.layout.fragment_home_screen_data_overview, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view_home_list_localities);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"CALLED onCreate!");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            currentSessionId = getArguments().getInt(ARG_CURRENTSESSIONID);
        }
        mAdapter = new HomeLocalityListCursorAdapter(this.getContext(), this);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"CALLED onResume!");
        if (this.getView() != null) {
            mLocalityLoaderListener = new LocalityLoaderListener(this,this.getActivity(), currentSessionId, null);
            this.getActivity().getSupportLoaderManager().initLoader(LoaderIds.LOCALITY_HOME_LIST_LOADER_ID,
                    null,
                    mLocalityLoaderListener);
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
    }

    // Launch a new data screen activity when a row in our recyclerview is clicked
    @Override
    public void onClick(int localityId) {
        Intent startDataScreenIntent = new Intent(getActivity(), DataScreenActvity.class);
        startDataScreenIntent.putExtra("session_id",this.currentSessionId);
        startDataScreenIntent.putExtra("localityId",localityId);
        startActivity(startDataScreenIntent);

    }

    @Override
    public void handleNewLocalityData(Cursor cursor, boolean isSingleQuery) {
        Log.d(TAG,"Called handleNewLocalityData in List Fragment, isnull? " + Boolean.toString(mAdapter==null));
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void setCurrentLocality(int localityId) {

    }

    @Override
    public void updateLocalityUI() {

    }


    @Override
    public void updateSessionUI() {

    }

    @Override
    public void handleNewSessionData(Cursor cursor) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

}
