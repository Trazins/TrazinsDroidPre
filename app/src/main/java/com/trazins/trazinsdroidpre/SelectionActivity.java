package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;

public class SelectionActivity extends AppCompatActivity {

    private TextView txtUserName;

    private ImageButton buttonSettings;
    private UserOutputModel userLogged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        txtUserName = findViewById(R.id.textViewSelectionActivityUserName);
        buttonSettings = findViewById(R.id.buttonSettings);

        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        txtUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);

        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PrinterSettingsActivity.class);
                startActivity(i);
            }
        });
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
            case R.id.buttonSurgicalProcess:
                i = new Intent(getApplicationContext(), SurgicalProcessPreviousDataActivity.class);
                break;
            default:
                break;
        }

        i.putExtra("userLogged", this.userLogged);
        startActivity(i);
    }
}