package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;

public class SelectionActivity extends AppCompatActivity {

    private TextView txtUserName;
    private Button buttonLocate, buttonShipment, buttonPostCounter;
    private UserOutputModel userLogged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        txtUserName = findViewById(R.id.textViewSelectionActivityUserName);
        buttonLocate = findViewById(R.id.buttonLocateMenu);
        buttonShipment = findViewById(R.id.buttonShipmentMenu);
        buttonPostCounter = findViewById(R.id.buttonPostCounterSurgical);

        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        txtUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);
    }

    public void openSelectedActivity(View view){
        Intent i = new Intent();
        switch (view.getId()){
            case R.id.buttonLocateMenu:
                i = new Intent(getApplicationContext(), LocateActivity.class);
                break;
            case R.id.buttonShipmentMenu:
                i = new Intent(getApplicationContext(), ShipmentActivity.class);
                break;
            case R.id.buttonPostCounterSurgical:
                i = new Intent(getApplicationContext(), PostCounterSurgicalActivity.class);
                break;
            default:
                break;
        }

        i.putExtra("userLogged",this.userLogged);
        startActivity(i);
    }
}