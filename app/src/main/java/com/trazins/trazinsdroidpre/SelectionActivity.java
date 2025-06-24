package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private TextView txtUserName, txtHospital;

    private Button btnLocateMenu, btnShipmentMenu, btnSPMenu, btnSteriShipMenu;

    private androidx.gridlayout.widget.GridLayout glMainMenu;

    //Version 1:
    private ArrayList<Button>version1Menu = new ArrayList<>();
    private ArrayList<Button>version2Menu = new ArrayList<>();

    private ImageButton imbSettings;
    private UserOutputModel userLogged;
    private UserOutputModel listaDeHospitales;
    List<UserOutputModel> lstHosUser = new ArrayList<UserOutputModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        txtUserName = findViewById(R.id.textViewSelectionActivityUserName);
        txtHospital = findViewById(R.id.textViewHospital);
        imbSettings = findViewById(R.id.buttonSettings);
        btnLocateMenu = findViewById(R.id.buttonLocateMenu);
        btnShipmentMenu = findViewById(R.id.buttonShipmentMenu);
        btnSPMenu = findViewById(R.id.buttonSurgicalProcess);
        btnSteriShipMenu = findViewById(R.id.buttonSteriShipment);
        glMainMenu = findViewById(R.id.gridLayoutSelectionMenu);

        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        this.listaDeHospitales = (UserOutputModel)getIntent().getSerializableExtra("listaDeHospitales");
        this.lstHosUser = userLogged.UsersList;

        getAllButtons(glMainMenu);
        txtUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);
        txtHospital.setText("Hospital:" + " " + userLogged.HospitalName);

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
            case R.id.buttonHospitalShipmentMenu:
                i = new Intent(getApplicationContext(), HospitalShipment.class);
                i.putExtra("toCentral", true);
                break;
            case R.id.buttonSetRFIDCodeMenu:
                i = new Intent(getApplicationContext(), SetRFIDCodeActivity.class);
                break;
            default:
                break;
        }

        i.putExtra("userLogged", this.userLogged);
        i.putExtra("listaDeHospitales", this.listaDeHospitales);
        startActivity(i);
    }
}