package com.trazins.trazinsdroidpre.surgicalprocessactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.threepin.fireexit_wcf.Configurator;
import com.threepin.fireexit_wcf.FireExitClient;
import com.trazins.trazinsdroidpre.R;
import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialInputModel;
import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialOutputModel;
import com.trazins.trazinsdroidpre.models.storagemodel.StorageInputModel;
import com.trazins.trazinsdroidpre.models.storagemodel.StorageOutputModel;
import com.trazins.trazinsdroidpre.models.surgicalprocessmodel.SurgicalProcessInputModel;
import com.trazins.trazinsdroidpre.models.surgicalprocessmodel.SurgicalProcessOutputModel;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.utils.ConnectionParameters;
import com.trazins.trazinsdroidpre.utils.ErrorLogWriter;
import com.trazins.trazinsdroidpre.utils.SPCustomAdapter;

import java.io.Serializable;
import java.nio.channels.AsynchronousChannelGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SelectSurgicalProcessActivity extends AppCompatActivity {

    ListView SPListView;
    List<SurgicalProcessOutputModel> SPList = new ArrayList<SurgicalProcessOutputModel>();
    Handler handler;

    String activityName;

    //Usuario logeado
    UserOutputModel userLogged;

    //Proceso quiúrgico seleccionado
    SurgicalProcessOutputModel selectedsurgicalProcess;
    private boolean getMaterialList = false;

    private TextView textViewUserName, txtHospital;
    private BottomNavigationView bnv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_surgical_process);

        handler = new Handler(Looper.getMainLooper());

        //Usuario loggeado
        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        textViewUserName = findViewById(R.id.textViewShipmentUserName);
        textViewUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);
        bnv = findViewById(R.id.bottomSurgicalSelectNavigationMenu);
        txtHospital = findViewById(R.id.textViewHospital);
        txtHospital.setText("Hospital:" + " " + userLogged.HospitalName);
        this.activityName= this.getClass().getSimpleName();

        //Hacer responsive la listview

        SPListView = findViewById(R.id.listViewSelectSurgicalProcess);
        SPListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedsurgicalProcess = SPList.get(position);

                SPListView.setSelector(R.color.selection);
                SPListView.requestLayout();
            }
        });

        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.new_SP){
                    //Abrir la pantalla sin datos
                    selectedsurgicalProcess = null;
                    openNewSurgicalProcessPreviousDataActivity();
                }else if(item.getItemId()==R.id.edit_SP){
                    //Abrir la pantalla con datos
                    openSurgicalProcessPreviousDataActivity();
                }else{
                    onBackPressed();
                }
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            new SurgicalProcessAsyncClass().execute();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SPList.clear();
    }

    private void openNewSurgicalProcessPreviousDataActivity(){
        Intent i = new Intent(getApplicationContext(), SurgicalProcessPreviousDataActivity.class);
        i.putExtra("userLogged", this.userLogged);
        i.putExtra("surgicalProcess", this.selectedsurgicalProcess);
        startActivity(i);
    }
    private void openSurgicalProcessPreviousDataActivity() {
        getMaterialList = true;

        if(selectedsurgicalProcess== null)
            return;

        new SurgicalProcessAsyncClass().execute();

        /*//Probar
        Bundle bun = new Bundle();
        bun.putSerializable("materialsListbun", (Serializable) this.selectedsurgicalProcess.MaterialOutputModelList);
        Intent i = new Intent(getApplicationContext(), SurgicalProcessPreviousDataActivity.class);
        i.putExtra("userLogged", this.userLogged);
        i.putExtra("surgicalProcess", this.selectedsurgicalProcess);
        i.putExtra("materialList", bun);
        startActivity(i);*/

    }

    class SurgicalProcessAsyncClass extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            String methodName = "GetSurgicalProcess";
            String parameterName = "surgicalProcess";
            String parameterName1 = "getMaterialList";

            SurgicalProcessInputModel surgicalProcessInputModel = new SurgicalProcessInputModel();
            //Con el usuario obtenemos los datos del hospital
            surgicalProcessInputModel.HosId = userLogged.HosId;
            if(getMaterialList)
                surgicalProcessInputModel.HisId = selectedsurgicalProcess.HisId;

            //Desplegar el servicio:
            //Usamos la librería Fireexit para la gestión de la serialización.
            FireExitClient client = new FireExitClient(
                    ConnectionParameters.SOAP_ADDRESS[ConnectionParameters.SET_URL_CONNECTION]);

            client.configure(new Configurator(
                    ConnectionParameters.NAME_SPACE, ConnectionParameters.CONTRACT_NAME, methodName));

            client.addParameter(parameterName, surgicalProcessInputModel);
            client.addParameter(parameterName1, getMaterialList);

            SurgicalProcessOutputModel resultModel = new SurgicalProcessOutputModel();
            try {
                //Realizamos la llamada al web service para obtener los datos
                resultModel = client.call(resultModel);
            } catch (Exception e) {
                e.printStackTrace();
                ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
                getMaterialList = false;
            }
            return resultModel;
        }

        //Aqui es donde se realizan las acciones sobre la UI
        @Override
        protected void onPostExecute(Object modelResult) {
            super.onPostExecute(modelResult);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(modelResult!= null){

                        if(getMaterialList){
                            selectedsurgicalProcess = (SurgicalProcessOutputModel)modelResult;
                            openNewSurgicalProcessPreviousDataActivity();
                        }
                        else{
                            processData((SurgicalProcessOutputModel)modelResult);
                        }
                        getMaterialList = false;
                    }
                }
            });
        }

        private void processData(SurgicalProcessOutputModel modelResult) {
            //Recibimos un listado de surgicalprocess
            if(modelResult!= null)
                addSurgicalProcessToList(modelResult);
        }
    }

    private void addSurgicalProcessToList(SurgicalProcessOutputModel modelResult) {
        SurgicalProcessOutputModel spm = (SurgicalProcessOutputModel)modelResult;
        List<SurgicalProcessOutputModel> listResult = spm.SurgicalProcessList;

        if(listResult.size()>0){
            SPCustomAdapter spAdapter = null;
            for (SurgicalProcessOutputModel spmItem : listResult) {
                spAdapter = new SPCustomAdapter(this, GetData(spmItem));

            }
            SPListView.setAdapter(spAdapter);
            SPListView.setSelector(R.color.transparent);
        }
    }

    private List<SurgicalProcessOutputModel> GetData(SurgicalProcessOutputModel spmItem) {
        try {

            SPList.add(new SurgicalProcessOutputModel(spmItem.HisId,
                    spmItem.OperationRoomName, spmItem.InterventionCode, spmItem.RecordNumber,
                    spmItem.InterventionDate, spmItem.Urgent));
            return SPList;

        }catch (Exception e){
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),this,activityName);
            e.printStackTrace();
            return  null;
        }

    }


}