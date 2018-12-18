package com.blueridgebinary.terra.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.blueridgebinary.terra.R;
import com.blueridgebinary.terra.data.TerraDbContract;

public class TerraLocalitySpinnerArrayAdapter extends ResourceCursorAdapter {

    private final String TAG = TerraLocalitySpinnerArrayAdapter.class.getSimpleName();

    Context mContext;

    public TerraLocalitySpinnerArrayAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // This method is called when the adapter needs to create a new view (for the unopened spinner)
        // cursor is already at the index of the currently selected item

        // Inflate the view
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View row=inflater.inflate(R.layout.terra_main_spinner, parent, false);
        Log.d(TAG, "newView: called!");
        return row;
    }

    @Override
    public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View row=inflater.inflate(R.layout.terra_station_spinner_item, parent, false);
        return row;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String thisStationNumber = cursor.getString(cursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_STATIONNUMBER));

        // Determine whether this view is a dropdown item or the main view
        View dropDownLabelView = view.findViewById(R.id.tv_category_spinner_item);
        if (dropDownLabelView != null) {
            // In dropdown view
            TextView dropDownLabel=(TextView) dropDownLabelView;
            ImageView dropDownIcon = (ImageView) view.findViewById(R.id.iv_station_spinner_icon);
            dropDownLabel.setText(thisStationNumber);

        }
        else {
            TextView mainLabel=(TextView) view.findViewById(R.id.tv_terra_spinner);
            mainLabel.setText("Station " + thisStationNumber);
        }

    }

}