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


            // TODO: Need to set up content provider that is a joined table before the rest of this
            // will  work.

            mIdIndex = mCursor.getColumnIndex(TerraDbContract.CompassMeasurementEntry._ID);
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

            holder.itemView.setTag(id);

            Resources res = mContext.getResources();

            holder.mTvLocalityName.setText(Integer.toString(localityid));
            holder.mTvAzi.setText(String.format(Locale.US,"%.2f",azi));
            holder.mTvDip.setText(String.format(Locale.US,"%.2f",dip));
            holder.mTvMode.setText(mode);
            holder.mTvCategory.setText(category);
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

        class CompassDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView mTvLocalityName;
            TextView mTvAzi;
            TextView mTvDip;
            TextView mTvMode;
            TextView mTvCategory;

            public CompassDataViewHolder(View itemView) {
                super(itemView);
                mTvLocalityName = (TextView) itemView.findViewById(R.id.tv_compass_data_locality);
                mTvAzi = (TextView) itemView.findViewById(R.id.tv_compass_data_azi);
                mTvDip = (TextView) itemView.findViewById(R.id.tv_compass_data_dip);
                mTvMode = (TextView) itemView.findViewById(R.id.tv_compass_data_mode);
                mTvCategory = (TextView) itemView.findViewById(R.id.tv_compass_data_category);
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

