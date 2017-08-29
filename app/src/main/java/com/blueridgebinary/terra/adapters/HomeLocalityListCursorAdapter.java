package com.blueridgebinary.terra.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blueridgebinary.terra.R;
import com.blueridgebinary.terra.data.TerraDbContract;

/**
 * Created by dorra on 8/29/2017.
 */

public class HomeLocalityListCursorAdapter extends RecyclerView.Adapter<HomeLocalityListCursorAdapter.LocalityViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    private int mIdIndex;
    private int mLatIndex;
    private int mLongIndex;
    private int mElevIndex;
    private int mCreatedIndex;
    private int mUpdatedIndex;

    final private HomeLocalityListCursorAdapter.LocalityAdapterOnClickHandler mClickHandler;

    public interface LocalityAdapterOnClickHandler {
        void onClick(int localityId);
    }

    public HomeLocalityListCursorAdapter(Context mContext, HomeLocalityListCursorAdapter.LocalityAdapterOnClickHandler clickHandler) {
        this.mContext = mContext;
        mClickHandler = clickHandler;
    }

    @Override
    public HomeLocalityListCursorAdapter.LocalityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.locality_layout, parent, false);
        return new HomeLocalityListCursorAdapter.LocalityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeLocalityListCursorAdapter.LocalityViewHolder holder, int position) {

        mIdIndex = mCursor.getColumnIndex(TerraDbContract.LocalityEntry._ID);
        mLatIndex = mCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_LAT);
        mLongIndex = mCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_LONG);
        mElevIndex= mCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_ELEVATION);
        mCreatedIndex = mCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_CREATED);
        mUpdatedIndex = mCursor.getColumnIndex(TerraDbContract.LocalityEntry.COLUMN_UPDATED);

        mCursor.moveToPosition(position);

        final int id = mCursor.getInt(mIdIndex);
        double lat = mCursor.getDouble(mLatIndex);
        double lon = mCursor.getDouble(mLongIndex);
        double elev = mCursor.getDouble(mElevIndex);
        String createdDate = mCursor.getString(mCreatedIndex);
        String updatedDate = mCursor.getString(mUpdatedIndex);

        holder.itemView.setTag(id);

        Resources res = mContext.getResources();

        holder.mTvLocalityName.setText(res.getString(R.string.locality_list_item_base) + " " + Integer.toString(id));
        holder.mTvLat.setText(res.getString(R.string.locality_list_lat_base) + " " + Double.toString(lat));
        holder.mTvLong.setText(res.getString(R.string.locality_list_long_base) + " " + Double.toString(lon));
        holder.mTvElev.setText(res.getString(R.string.locality_list_elevation_base) + " " + Double.toString(elev));
        holder.mTvCreated.setText(res.getString(R.string.locality_list_created_date_base) + " " + createdDate);
        holder.mTvUpdated.setText(res.getString(R.string.locality_list_updated_date_base) + " " + updatedDate);

    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    // TODO: add onclick listener logic here

    class LocalityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTvLocalityName;
        TextView mTvLat;
        TextView mTvLong;
        TextView mTvElev;
        TextView mTvCreated;
        TextView mTvUpdated;

        public LocalityViewHolder(View itemView) {
            super(itemView);
            mTvLocalityName = (TextView) itemView.findViewById(R.id.tv_locality_list_name);
            mTvLat = (TextView) itemView.findViewById(R.id.tv_locality_list_lat);
            mTvLong = (TextView) itemView.findViewById(R.id.tv_locality_list_long);
            mTvElev = (TextView) itemView.findViewById(R.id.tv_locality_list_elev);
            mTvCreated = (TextView) itemView.findViewById(R.id.tv_locality_list_created);
            mTvUpdated = (TextView) itemView.findViewById(R.id.tv_locality_list_updated);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemView.setSelected(true);
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int localityId = mCursor.getInt(mIdIndex);
            mClickHandler.onClick(localityId);
        }
    }








}
