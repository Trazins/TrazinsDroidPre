package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class SelectionActivity extends AppCompatActivity {

    private TextView txtUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        txtUserName = (TextView)findViewById(R.id.textViewSelectionActivityUserName);

        String userName = getIntent().getStringExtra("userName");
        txtUserName.setText(getString(R.string.identified_user) + " " + userName);
    }
}