package com.blueridgebinary.terra.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blueridgebinary.terra.R;

public class EmptyViewHolder extends RecyclerView.ViewHolder {

    TextView mTvMessage;
    public EmptyViewHolder(View itemView) {
        super(itemView);
        mTvMessage = (TextView) itemView.findViewById(R.id.tv_empty_message);
    }
}
