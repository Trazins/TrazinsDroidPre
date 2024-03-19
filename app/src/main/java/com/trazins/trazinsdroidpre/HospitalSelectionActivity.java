package com.trazins.trazinsdroidpre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.threepin.fireexit_wcf.Configurator;
import com.threepin.fireexit_wcf.FireExitClient;
import com.trazins.trazinsdroidpre.models.materialmodel.MaterialInputModel;
import com.trazins.trazinsdroidpre.models.materialmodel.MaterialOutputModel;
import com.trazins.trazinsdroidpre.models.originmodel.OriginInputModel;
import com.trazins.trazinsdroidpre.models.originmodel.OriginOutputModel;
import com.trazins.trazinsdroidpre.models.shipmentmodel.ShipmentInputModel;
import com.trazins.trazinsdroidpre.models.shipmentmodel.ShipmentOutputModel;
import com.trazins.trazinsdroidpre.models.trolleymodel.TrolleyInputModel;
import com.trazins.trazinsdroidpre.models.trolleymodel.TrolleyOutputModel;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.utils.ConnectionParameters;
import com.trazins.trazinsdroidpre.utils.CustomAdapter;
import com.trazins.trazinsdroidpre.utils.ErrorLogWriter;
import com.trazins.trazinsdroidpre.utils.HospitalCustomAdapter;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;

import java.util.ArrayList;
import java.util.List;

public class HospitalSelectionActivity extends AppCompatActivity {

    ListView ListViewHospitals;
    List<UserOutputModel> lstHosUser = new ArrayList<UserOutputModel>();

    BottomNavigationView bnv;
    TextView textViewUserLogged;

    UserOutputModel selectedhospitalUser;
    UserOutputModel userLogged;
    UserOutputModel listaDeHospitales;

    //Variable para gestionar el funcionamiento de la clase que gestiona la conexión webservice
    boolean setShipment = false;

    ShipmentInputModel createShipment = new ShipmentInputModel();
    OriginOutputModel finalOrigin = new OriginOutputModel();
    boolean toCentral;
    Switch switchUrgent;
    //Carro asociado al envío final
    TrolleyOutputModel finalTrolley;
    //Lista que gestiona los materiales internamente.
    List<MaterialOutputModel> lstMaterial= new ArrayList<>();
    String readCode;
    String activityName;
    Handler handler;
    TextView textViewShipmentResult, textViewUserName, textViewElements, textViewTrolleyName;


    //Lista que muestra los resultados por pantalla
    ListView ListViewMaterials;
    ShipmentOutputModel shipmentResult = new ShipmentOutputModel();
    String originDescription= "";

    //Conexión con la impresora para imprimir la etiqueta.
    Connection printerConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_selection);

        ListViewHospitals = findViewById(R.id.listViewHospitals);
        ListViewHospitals.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedhospitalUser = lstHosUser.get(position);

                ListViewHospitals.setSelector(R.color.selection);
                ListViewHospitals.requestLayout();
            }
        });

        //Usuario loggeado
        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        this.listaDeHospitales = userLogged;
        textViewUserLogged = findViewById(R.id.textViewHospitalUserName);

        textViewUserLogged.setText(getString(R.string.user) + " " + userLogged.UsersList.get(0).UserName);

        bnv = findViewById(R.id.bottomNavigationMenuHospitals);

        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.select_user_hospital){
                    //Coger el elemento seleccionado y devolver los datos al flujo principal
                    if(selectedhospitalUser==null){
                        Toast.makeText(getApplicationContext(), R.string.hospital_not_selected, Toast.LENGTH_LONG).show();
                        return false;
                    }

                    closeScreenWithResult(RESULT_OK);

                }else if(item.getItemId()==R.id.go_back_hospital){
                    closeScreenWithResult(RESULT_CANCELED);
                }else{
                    closeScreenWithResult(RESULT_CANCELED);
                }
                return false;
            }
        });

        //Cargar la lista con la propiedad de lista del modelo enviado
        this.lstHosUser = userLogged.UsersList;
        HospitalCustomAdapter hospitalCustomAdapter = new HospitalCustomAdapter( this,
                convertList(lstHosUser));
        ListViewHospitals.setAdapter(hospitalCustomAdapter);
    }

    private List<UserOutputModel> convertList(List<UserOutputModel> lstHosUser) {
        return lstHosUser;
    }

    private void closeScreenWithResult(int Result) {
        Intent i = getIntent();
        i.putExtra("selectedHospitalUser", selectedhospitalUser);
        i.putExtra("listaDeHospitales", listaDeHospitales);
        setResult(Result,i);
        finish();
    }
}