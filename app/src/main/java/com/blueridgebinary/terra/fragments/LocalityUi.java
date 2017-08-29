package com.blueridgebinary.terra.fragments;

import android.database.Cursor;

/**
 * Created by dorra on 8/29/2017.
 */

public interface LocalityUi {
    public void updateLocalityUI();
    public void handleNewLocalityData(Cursor cursor,boolean isSingleQuery);
}
