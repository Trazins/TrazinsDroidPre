package com.trazins.trazinsdroidpre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.utils.HospitalCustomAdapter;

import java.util.ArrayList;
import java.util.List;

public class HospitalSelectionActivity extends AppCompatActivity {

    ListView ListViewHospitals;
    List<UserOutputModel> lstHosUser = new ArrayList<UserOutputModel>();

    BottomNavigationView bnv;
    TextView textViewUserLogged;

    UserOutputModel selectedhospitalUser;
    UserOutputModel userLogged;

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
        setResult(Result,i);
        finish();
    }
}