package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SelectionActivity extends AppCompatActivity {

    private TextView txtUserName;
    private Button buttonLocate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        txtUserName = (TextView)findViewById(R.id.textViewSelectionActivityUserName);
        buttonLocate = (Button)findViewById(R.id.buttonLocateMenu);

        String userName = getIntent().getStringExtra("userName");
        txtUserName.setText(getString(R.string.identified_user) + " " + userName);
    }

    public void openSelectedActivity(View view){
        Intent i = new Intent(getApplicationContext(),LocateActivity.class);
        startActivity(i);
    }
}