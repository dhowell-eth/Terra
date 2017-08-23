package com.blueridgebinary.terra.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blueridgebinary.terra.R;
import com.blueridgebinary.terra.data.TerraDbContract;

/**
 * Created by dorra on 8/23/2017.
 */

public class SessionCursorAdapter extends RecyclerView.Adapter<SessionCursorAdapter.SessionViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    public SessionCursorAdapter(Context mContext) { this.mContext = mContext;}

    @Override
    public SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.session_layout, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SessionViewHolder holder, int position) {

        int idIndex = mCursor.getColumnIndex(TerraDbContract.SessionEntry._ID);
        int nameIndex = mCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_SESSIONNAME);
        int notesIndex = mCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_NOTES);
        int updatedIndex = mCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_UPDATED);

        mCursor.moveToPosition(position);

        final int id = mCursor.getInt(idIndex);
        String name = mCursor.getString(nameIndex);
        String notes = mCursor.getString(notesIndex);
        String updatedDate = mCursor.getString(updatedIndex);

        holder.itemView.setTag(id);
        holder.tvSessionName.setText("Project: " + name);
        holder.tvSessionDate.setText("Last Accessed: " + updatedDate);
        holder.tvSessionNotes.setText("Description: " + notes);

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

    class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionName;
        TextView tvSessionDate;
        TextView tvSessionNotes;

        public SessionViewHolder(View itemView) {
            super(itemView);
            tvSessionName = (TextView) itemView.findViewById(R.id.tv_session_name);
            tvSessionDate = (TextView) itemView.findViewById(R.id.tv_session_updated);
            tvSessionNotes = (TextView) itemView.findViewById(R.id.tv_session_description);
        }
    }


}
