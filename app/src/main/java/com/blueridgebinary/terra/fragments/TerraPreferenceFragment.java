package com.blueridgebinary.terra.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

import com.blueridgebinary.terra.AboutActivity;
import com.blueridgebinary.terra.R;
import com.blueridgebinary.terra.WelcomeActivity;
import com.blueridgebinary.terra.data.TerraDbContract;


public class TerraPreferenceFragment extends PreferenceFragmentCompat {

    final private String TAG = this.getClass().getSimpleName();
    private int sessionId;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context activityContext = getActivity();

        // Get the current session id for delete operation
        sessionId = ((FragmentActivity) activityContext).getIntent().getIntExtra("session_id",0);


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
        compassEnabledPreference.setDefaultValue(true);
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

        Preference aboutTerraPreference = new Preference(contextThemeWrapper);
        aboutTerraPreference.setTitle("About");
        aboutTerraPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent aboutIntent = new Intent(getActivity(),AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            }
        });
        screen.addPreference(aboutTerraPreference);

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
                                activityContext.getContentResolver().delete(
                                        Uri.withAppendedPath(TerraDbContract.SessionEntry.CONTENT_URI,
                                        Uri.encode(Integer.toString(sessionId))),
                                        null,null);
                                // SWAP OVER TO THE WELCOME SCREEN ACTIVITY
                                Intent homeIntent = new Intent(activityContext,WelcomeActivity.class);
                                ((FragmentActivity) activityContext).finishAffinity();
                                startActivity(homeIntent);
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
