package com.blueridgebinary.terra.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

import com.blueridgebinary.terra.R;


public class TerraPreferenceFragment extends PreferenceFragmentCompat {

    final private String TAG = this.getClass().getSimpleName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context activityContext = getActivity();

        TypedValue themeTypedValue = new TypedValue();
        activityContext.getTheme().resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(activityContext, themeTypedValue.resourceId);


        // Creating the preferences screen purely programmatically
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getContext());

        String[] compassModes = getResources().getStringArray(R.array.compass_modes);

        PreferenceCategory categoryCompass = new PreferenceCategory(contextThemeWrapper);
        categoryCompass.setTitle("Compass");
        screen.addPreference(categoryCompass);

        ListPreference compassModePreference = new ListPreference(contextThemeWrapper);
        compassModePreference.setKey("compass_mode");
        compassModePreference.setTitle("Default Compass Measurement Type");
        compassModePreference.setSummary("Controls which compass mode is turned on by default. Modify to match your workflow.");
        compassModePreference.setEntries(R.array.compass_modes);
        compassModePreference.setEntryValues(R.array.compass_modes);
        screen.addPreference(compassModePreference);


        SwitchPreferenceCompat compassEnabledPreference = new SwitchPreferenceCompat(contextThemeWrapper);
        compassEnabledPreference.setKey("is_compass_enabled");
        compassEnabledPreference.setTitle("Use Device Compass By Default");
        compassEnabledPreference.setSwitchTextOff("No");
        compassEnabledPreference.setSwitchTextOn("Yes");
        screen.addPreference(compassEnabledPreference);

        PreferenceCategory categoryMap = new PreferenceCategory(contextThemeWrapper);
        categoryMap.setTitle("Map");
        screen.addPreference(categoryMap);

        ListPreference mapModePreference = new ListPreference(contextThemeWrapper);
        mapModePreference.setKey("map_mode");
        mapModePreference.setTitle("Default Google Basemap");
        mapModePreference.setSummary("Controls the default basemap for Google Maps.");
        mapModePreference.setEntries(R.array.map_modes);
        mapModePreference.setEntryValues(R.array.map_modes);
        screen.addPreference(mapModePreference);

        PreferenceCategory categoryAdmin = new PreferenceCategory(contextThemeWrapper);
        categoryAdmin.setTitle("Admin");
        screen.addPreference(categoryAdmin);

        Preference deleteProjectPreference = new Preference(contextThemeWrapper);
        deleteProjectPreference.setTitle("DELETE PROJECT");
        deleteProjectPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                new AlertDialog.Builder(activityContext)
                        .setTitle("Delete Project")
                        .setMessage("Do you really want to delete this project? This action is irreversible.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // RUN DELETE ON ALL RELATED ROWS (MUST IMPLEMENT IN CONTENT RESOLVER

                                // SWAP OVER TO THE WELCOME SCREEN ACTIVITY
                                
                                Toast.makeText(activityContext, "Yaay", Toast.LENGTH_SHORT).show();
                            }})
                        .setNegativeButton("CANCEL", null).show();



                return true;
            }
        });

        screen.addPreference(deleteProjectPreference);

        // Set the preferences screen to our screen we just created
        setPreferenceScreen(screen);

    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }
}
