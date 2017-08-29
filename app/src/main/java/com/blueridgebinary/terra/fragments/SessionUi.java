package com.blueridgebinary.terra.fragments;

import android.database.Cursor;

/**
 * Created by dorra on 8/29/2017.
 */

public interface SessionUi {
        public void updateSessionUI();
        public void handleNewSessionData(Cursor cursor);
}
