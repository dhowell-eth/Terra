package com.blueridgebinary.terra.fragments;

import com.blueridgebinary.terra.data.CurrentDataset;

/**
 * Created by dorra on 8/25/2017.
 */

public interface RefreshableFragment {
    public void refreshFragmentData(CurrentDataset cd);
}
