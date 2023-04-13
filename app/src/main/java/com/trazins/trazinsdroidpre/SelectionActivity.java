package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.surgicalprocessactivities.SelectSurgicalProcessActivity;

import java.util.ArrayList;
import java.util.List;

public class SelectionActivity extends AppCompatActivity {

    private TextView txtUserName;

    private Button btnLocateMenu, btnShipmentMenu, btnSPMenu, btnSteriShipMenu;

    private androidx.gridlayout.widget.GridLayout glMainMenu;

    //Version 1:
    private ArrayList<Button>version1Menu = new ArrayList<>();
    private ArrayList<Button>version2Menu = new ArrayList<>();

    private ImageButton imbSettings;
    private UserOutputModel userLogged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        txtUserName = findViewById(R.id.textViewSelectionActivityUserName);
        imbSettings = findViewById(R.id.buttonSettings);
        btnLocateMenu = findViewById(R.id.buttonLocateMenu);
        btnShipmentMenu = findViewById(R.id.buttonShipmentMenu);
        btnSPMenu = findViewById(R.id.buttonSurgicalProcess);
        btnSteriShipMenu = findViewById(R.id.buttonSteriShipment);
        glMainMenu = findViewById(R.id.gridLayoutSelectionMenu);

        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");

        getAllButtons(glMainMenu);
        txtUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);

        imbSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PrinterSettingsActivity.class);
                startActivity(i);
            }
        });
    }

    public void getAllButtons(ViewGroup layout){

        for(int i =0; i< layout.getChildCount(); i++){
            View v =layout.getChildAt(i);
            if(v instanceof Button){
                Button b = (Button)v;
                if(this.userLogged.AndroidVersion==0)
                    return;
                String formatVersion = "v"+ this.userLogged.AndroidVersion;
                if(b.getTag().toString().equals(formatVersion)){
                    b.setVisibility(View.VISIBLE);
                }else {
                    b.setVisibility(View.GONE);
                }
            }
        }
    }

    public void openSelectedActivity(View view){
        Intent i = new Intent();
        switch (view.getId()){
            case R.id.buttonLocateMenu:
                i = new Intent(getApplicationContext(), LocateActivity.class);
                break;
            case R.id.buttonShipmentMenu:
                i = new Intent(getApplicationContext(), ShipmentActivity.class);
                i.putExtra("toCentral", false);
                break;
            case R.id.buttonSurgicalProcess:
                i = new Intent(getApplicationContext(), SelectSurgicalProcessActivity.class);
                break;
            case R.id.buttonSteriShipment:
                i = new Intent(getApplicationContext(), ShipmentActivity.class);
                i.putExtra("toCentral", true);
                break;
            default:
                break;
        }

        i.putExtra("userLogged", this.userLogged);
        startActivity(i);
    }
}