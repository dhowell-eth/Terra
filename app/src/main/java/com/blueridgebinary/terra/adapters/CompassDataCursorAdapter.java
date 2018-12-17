package com.blueridgebinary.terra.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blueridgebinary.terra.DataScreenActvity;
import com.blueridgebinary.terra.R;
import com.blueridgebinary.terra.data.TerraDbContract;
import com.github.florent37.shapeofview.shapes.CircleView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by dorra on 10/10/2017.
 */

public class CompassDataCursorAdapter extends RecyclerView.Adapter<CompassDataCursorAdapter.CompassDataViewHolder> {

        private Cursor mCursor;
        private Context mContext;

        private int mLocalityIdIndex;
        private int mDipIndex;
        private int mAziIndex;
        private int mModeIndex;
        private int mCreatedIndex;
        private int mUpdatedIndex;
        private int mCategoryIndex;
        private int mAccuracyIndex;
        private int mIdIndex;

        public ArrayList<Integer> selectedRecyclerViewItems = new ArrayList<Integer>();

    final private CompassDataAdapterOnClickHandler mClickHandler;

        public interface CompassDataAdapterOnClickHandler {
            void onClick(int localityId);
        }

        public CompassDataCursorAdapter(Context mContext, CompassDataAdapterOnClickHandler clickHandler) {
            this.mContext = mContext;
            mClickHandler = clickHandler;
        }

        @Override
        public CompassDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.compass_data_layout, parent, false);
            return new CompassDataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CompassDataViewHolder holder, int position) {

            // Note: hardcoded "compassId" column in join to avoid confusion from multiple joined "_id" columns in query result
            mIdIndex = mCursor.getColumnIndex("compassId");
            mLocalityIdIndex = mCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry.COLUMN_LOCALITYID);
            mDipIndex = mCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry.COLUMN_DIP);
            mAziIndex = mCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry.COLUMN_DIPDIRECTION);
            mModeIndex= mCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry.COLUMN_MEASUREMENTMODE);
            mCategoryIndex = mCursor.getColumnIndex(TerraDbContract.MeasurementCategoryEntry.COLUMN_NAME);
            mCursor.moveToPosition(position);

            final int id = mCursor.getInt(mIdIndex);
            int localityid = mCursor.getInt(mLocalityIdIndex);
            double azi = mCursor.getDouble(mAziIndex);
            double dip = mCursor.getDouble(mDipIndex);
            String mode = mCursor.getString(mModeIndex);
            String category = mCursor.getString(mCategoryIndex);

            Resources res = mContext.getResources();

            // Bind data from our loader to our view holder
            holder.itemView.setTag(id);
            holder.mTvLocalityName.setText(Integer.toString(localityid));
            holder.mTvAzi.setText(String.format(Locale.US,"%.2f",azi));
            holder.mTvDip.setText(String.format(Locale.US,"%.2f",dip));
            String directionLabel = "Dip Direction:";
            switch (mode) {
                case "BEARING":
                    directionLabel = "Azimuth:";
                    holder.mTvDip.setVisibility(View.GONE);
                    holder.mTvDipLabel.setVisibility(View.GONE);
                    break;
                case "VECTOR":
                    directionLabel = "Dip Direction:";
                    holder.mTvDip.setVisibility(View.VISIBLE);
                    holder.mTvDipLabel.setVisibility(View.VISIBLE);
                    break;
                case "PLANE":
                    directionLabel = "Dip Direction:";
                    holder.mTvDip.setVisibility(View.VISIBLE);
                    holder.mTvDipLabel.setVisibility(View.VISIBLE);
                    break;
            }
            holder.mTvAziLabel.setText(directionLabel);

            holder.mTvMode.setText(mode);
            holder.mTvCategory.setText(category);

            final CompassDataViewHolder mHolder = holder;

            // Add an onclick listener for selecting rows for deletion
            holder.mIvRowIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView clickedImage = (ImageView) v;
                    //LocalityViewHolder holder = (LocalityViewHolder) v.getParent();
                    // If you click on the icon and it's already checked, deselect it
                    if (mHolder.isChecked) {
                        // Swap image to "unselected" icon
                        clickedImage.setImageResource(R.drawable.baseline_explore_black_48);
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
                    if (mContext instanceof DataScreenActvity)
                    {
                        DataScreenActvity activity = (DataScreenActvity) mContext;
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

        // View holder for recycler view (holds each individual row's view)
        class CompassDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView mTvLocalityName;
            TextView mTvAzi;
            TextView mTvDip;
            TextView mTvMode;
            TextView mTvCategory;
            ImageView mIvRowIcon;
            CircleView mCircleView;
            TextView mTvAziLabel;
            TextView mTvDipLabel;

            private boolean isChecked;

            public CompassDataViewHolder(View itemView) {
                super(itemView);
                mTvLocalityName = (TextView) itemView.findViewById(R.id.tv_compass_data_locality);
                mTvAzi = (TextView) itemView.findViewById(R.id.tv_compass_data_azi);
                mTvDip = (TextView) itemView.findViewById(R.id.tv_compass_data_dip);
                mTvMode = (TextView) itemView.findViewById(R.id.tv_compass_data_mode);
                mTvCategory = (TextView) itemView.findViewById(R.id.tv_compass_data_category);
                mIvRowIcon = (ImageView) itemView.findViewById(R.id.image_compass_data_icon);
                mCircleView = (CircleView) itemView.findViewById(R.id.image_compass_data_icon_circleview);
                mTvAziLabel = (TextView) itemView.findViewById(R.id.tv_compass_data_azi_label);
                mTvDipLabel = (TextView) itemView.findViewById(R.id.tv_compass_data_dip_label);
                isChecked = false;
                itemView.setOnClickListener(this);
            }

            public void setChecked(boolean status) {
                isChecked = status;
            }

            @Override
            public void onClick(View v) {
                /*itemView.setSelected(true);
                int adapterPosition = getAdapterPosition();
                mCursor.moveToPosition(adapterPosition);
                int localityId = mCursor.getInt(mIdIndex);
                mClickHandler.onClick(localityId);*/
            }
        }








}

