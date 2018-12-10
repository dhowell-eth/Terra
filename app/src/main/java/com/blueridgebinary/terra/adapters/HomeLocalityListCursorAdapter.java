package com.blueridgebinary.terra.adapters;



import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blueridgebinary.terra.DataScreenActvity;
import com.blueridgebinary.terra.MainActivity;
import com.blueridgebinary.terra.R;
import com.blueridgebinary.terra.data.TerraDbContract;
import com.blueridgebinary.terra.fragments.HomeScreenDataOverviewFragment;
import com.github.florent37.shapeofview.shapes.CircleView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dorra on 8/29/2017.
 */

public class HomeLocalityListCursorAdapter extends RecyclerView.Adapter<HomeLocalityListCursorAdapter.LocalityViewHolder> {

    public final String TAG = HomeLocalityListCursorAdapter.class.getSimpleName();
    private Cursor mCursor;
    private Context mContext;

    private int mIdIndex;
    private int mLatIndex;
    private int mLongIndex;
    private int mElevIndex;
    private int mCreatedIndex;
    private int mUpdatedIndex;

    public List<Integer> selectedRecyclerViewItems = new ArrayList<Integer>();

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

        // Reset our viewholder to defaults each time our recyclerview is recreated
        this.selectedRecyclerViewItems.clear();
        holder.setChecked(false);
        holder.mIvRowIcon.setImageResource(R.drawable.baseline_location_on_black_48);
        MainActivity activity = (MainActivity) mContext;
        activity.refreshAppBar(false);


        Resources res = mContext.getResources();

        final LocalityViewHolder mHolder = holder;

        holder.mTvLocalityName.setText(res.getString(R.string.locality_list_item_base) + " " + Integer.toString(id));
        holder.mTvLat.setText(res.getString(R.string.locality_list_lat_base) + " " + Double.toString(lat));
        holder.mTvLong.setText(res.getString(R.string.locality_list_long_base) + " " + Double.toString(lon));
        holder.mTvElev.setText(res.getString(R.string.locality_list_elevation_base) + " " + Double.toString(elev));
        holder.mTvCreated.setText(res.getString(R.string.locality_list_created_date_base) + " " + createdDate);
        holder.mIvRowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView clickedImage = (ImageView) v;
                //LocalityViewHolder holder = (LocalityViewHolder) v.getParent();
                // If you click on the icon and it's already checked, deselect it
                if (mHolder.isChecked) {
                    // Swap image to "unselected" icon
                    clickedImage.setImageResource(R.drawable.baseline_location_on_black_48);
                    mHolder.mCircleView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.recyclerViewRowDefault));
                    // Remove from this activity's selection
                    Iterator<Integer> iterator = selectedRecyclerViewItems.iterator();
                    while(iterator.hasNext()) {
                        Integer next = iterator.next();
                        if(next.equals(id)) {
                            iterator.remove();
                        }
                    }
                    // Set the view to "unchecked"
                    mHolder.setChecked(false);
                }
                // Otherwise, add this to our selection
                else {
                    // Swap image to "unselected" icon
                    clickedImage.setImageResource(R.drawable.baseline_delete_black_48);
                    mHolder.mCircleView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.recyclerViewRowSelected));
                    // Remove from this activity's selection
                    selectedRecyclerViewItems.add(id);
                    // Set the view to "unchecked"
                    mHolder.setChecked(true);
                }
                // Finally, refresh the app bar so the icons displayed a relevant to whether we have
                // selected something
                if (mContext instanceof MainActivity)
                {
                    MainActivity activity = (MainActivity) mContext;
                    boolean anythingSelected = !selectedRecyclerViewItems.isEmpty();
                    activity.refreshAppBar(anythingSelected);
                    // Then call the method in the activity.
                }
            }
        });
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
        ImageView mIvRowIcon;
        CircleView mCircleView;

        private boolean isChecked;

        //TextView mTvUpdated;

        public LocalityViewHolder(View itemView) {
            super(itemView);
            mTvLocalityName = (TextView) itemView.findViewById(R.id.tv_locality_list_name);
            mTvLat = (TextView) itemView.findViewById(R.id.tv_locality_list_lat);
            mTvLong = (TextView) itemView.findViewById(R.id.tv_locality_list_long);
            mTvElev = (TextView) itemView.findViewById(R.id.tv_locality_list_elev);
            mTvCreated = (TextView) itemView.findViewById(R.id.tv_locality_list_created);
            mIvRowIcon = (ImageView) itemView.findViewById(R.id.image_locality_list_icon);
            mCircleView = (CircleView) itemView.findViewById(R.id.image_locality_list_icon_circleview);
            isChecked = false;

            itemView.setOnClickListener(this);
        }

        public void setChecked(boolean status) {
            isChecked = status;
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
