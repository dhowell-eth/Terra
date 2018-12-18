package com.blueridgebinary.terra.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.blueridgebinary.terra.R;

import java.util.ArrayList;
import java.util.List;

public class TerraCompassModeArrayAdapter extends ArrayAdapter {

    Context mContext;
    private List<String> stringList;


    public TerraCompassModeArrayAdapter(@NonNull Context context, List<String> list) {
        super(context, 0, list);
        mContext = context;
        stringList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String selectedItem = stringList.get(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View currentView = convertView;
        if(currentView == null)
            currentView = LayoutInflater.from(mContext).inflate(R.layout.terra_main_spinner,parent,false);

        TextView mainLabel=(TextView) currentView.findViewById(R.id.tv_terra_spinner);
        mainLabel.setText(selectedItem);
        return currentView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String thisItem = stringList.get(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View currentView = convertView;
        if(currentView == null)
            currentView = LayoutInflater.from(mContext).inflate(R.layout.terra_compass_mode_spinner_item,parent,false);
        TextView label=(TextView) currentView.findViewById(R.id.tv_compass_mode_spinner_item);
        label.setText(thisItem);
        return currentView;
    }
}
