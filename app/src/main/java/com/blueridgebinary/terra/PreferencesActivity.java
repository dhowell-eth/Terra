package com.blueridgebinary.terra;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.blueridgebinary.terra.fragments.TerraPreferenceFragment;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preferences_container, new TerraPreferenceFragment())
                .commit();


    }
}
