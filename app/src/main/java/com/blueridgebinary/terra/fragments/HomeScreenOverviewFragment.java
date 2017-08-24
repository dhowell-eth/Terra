package com.blueridgebinary.terra.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.blueridgebinary.terra.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeScreenOverviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeScreenOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeScreenOverviewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnTerraFragmentInteractionListener mListener;

    private Spinner mSpinner;


    public HomeScreenOverviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeScreenOverviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeScreenOverviewFragment newInstance(String param1, String param2) {
        HomeScreenOverviewFragment fragment = new HomeScreenOverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home_screen_overview, container, false);
        mSpinner = (Spinner) v.findViewById(R.id.home_spinner_locality);
        setSpinnerAdapter(mSpinner);
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

    private void setSpinnerAdapter(Spinner s) {
        ArrayList<LocalitySpinnerItem> localities = new ArrayList<>();

        // TODO: Hook this up to a query that is written in the main activity
        for (int i=0;i<10;i++){
            localities.add(new LocalitySpinnerItem(Integer.toString(i),i));
        }

        ArrayAdapter<LocalitySpinnerItem> adapter =
                new ArrayAdapter<LocalitySpinnerItem>(this.getActivity(),R.layout.support_simple_spinner_dropdown_item,
                localities);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        s.setAdapter(adapter);

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
}
