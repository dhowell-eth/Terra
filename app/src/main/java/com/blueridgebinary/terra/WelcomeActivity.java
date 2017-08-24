package com.blueridgebinary.terra;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    Button mNewButton;
    Button mOpenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Set on click events for each button
        mNewButton = (Button) findViewById(R.id.bt_welcome_create_new);
        mOpenButton = (Button) findViewById(R.id.bt_welcome_open_existing);


        // TODO: Something weird is happening wth the history stack  using the below FLAG_ACTIVITY_NO_HISTORY

        mNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this,com.blueridgebinary.terra.CreateNewProjectActivity.class);
                startActivity(intent);
            }
        });

        mOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this,com.blueridgebinary.terra.OpenProjectActivity.class);
                startActivity(intent);
            }
        });

    }
}
