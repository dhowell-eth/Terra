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
import com.blueridgebinary.terra.utils.AdapterViewTypes;

/**
 * Created by dorra on 8/23/2017.
 */

public class SessionCursorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    private int idIndex;
    private int nameIndex;
    private int notesIndex;
    private int updatedIndex;

    final private SessionAdapterOnClickHandler mClickHandler;

    public interface SessionAdapterOnClickHandler {
        void onClick(int sessionId, String sessionName);
    }

    public SessionCursorAdapter(Context mContext, SessionAdapterOnClickHandler clickHandler) {
        this.mContext = mContext;
        mClickHandler = clickHandler;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch(viewType){
            case AdapterViewTypes.FULL_VIEW:
                view = LayoutInflater.from(mContext)
                        .inflate(R.layout.session_layout, parent, false);
                return new SessionViewHolder(view);
            case AdapterViewTypes.EMPTY_VIEW:
                view = LayoutInflater.from(mContext)
                        .inflate(R.layout.empty_recyclerview, parent, false);
                return new EmptyViewHolder(view);
            default:
                view = LayoutInflater.from(mContext)
                        .inflate(R.layout.empty_recyclerview, parent, false);
                return new EmptyViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder plainHolder, int position) {
        if (getItemViewType(position) == AdapterViewTypes.EMPTY_VIEW){
            EmptyViewHolder holder = (EmptyViewHolder) plainHolder;
            String message = mContext.getResources().getString(R.string.rv_empty_card_message_projects);
            holder.mTvMessage.setText(message);
        }
        else if (getItemViewType(position) == AdapterViewTypes.FULL_VIEW) {
            SessionViewHolder holder = (SessionViewHolder) plainHolder;
            idIndex = mCursor.getColumnIndex(TerraDbContract.SessionEntry._ID);
            nameIndex = mCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_SESSIONNAME);
            notesIndex = mCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_NOTES);
            updatedIndex = mCursor.getColumnIndex(TerraDbContract.SessionEntry.COLUMN_UPDATED);

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
    }


    @Override
    public int getItemViewType(int position) {
        if (mCursor == null) {
            return AdapterViewTypes.EMPTY_VIEW;
        }
        if (mCursor.getCount()<1) {
            return AdapterViewTypes.EMPTY_VIEW;
        }
        else{
            return AdapterViewTypes.FULL_VIEW;
        }
    }


    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        else if (mCursor.getCount()==0) {
            return 1; // still need to render something if our cursor is empty
        }
        else {
            return mCursor.getCount();
        }
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

    class SessionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvSessionName;
        TextView tvSessionDate;
        TextView tvSessionNotes;

        public SessionViewHolder(View itemView) {
            super(itemView);
            tvSessionName = (TextView) itemView.findViewById(R.id.tv_locality_list_name);
            tvSessionDate = (TextView) itemView.findViewById(R.id.tv_locality_list_created);
            tvSessionNotes = (TextView) itemView.findViewById(R.id.tv_session_description);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemView.setSelected(true);
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int sessionId = mCursor.getInt(idIndex);
            String sessionName = mCursor.getString(nameIndex);
            mClickHandler.onClick(sessionId,sessionName);
        }
    }


}
