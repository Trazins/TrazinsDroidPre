package com.trazins.trazinsdroidpre.surgicalprocessactivities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.ArraySet;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.threepin.fireexit_wcf.Configurator;
import com.threepin.fireexit_wcf.FireExitClient;
import com.trazins.trazinsdroidpre.R;
import com.trazins.trazinsdroidpre.ShowInstrumentListActivity;
import com.trazins.trazinsdroidpre.SurgicalProcessActivity;
import com.trazins.trazinsdroidpre.models.materialmodel.MaterialInputModel;
import com.trazins.trazinsdroidpre.models.materialmodel.MaterialOutputModel;
import com.trazins.trazinsdroidpre.models.sp_intrumentmodel.SP_InstrumentOutputModel;
import com.trazins.trazinsdroidpre.models.sp_setmodel.SP_SetInputModel;
import com.trazins.trazinsdroidpre.models.sp_setmodel.SP_SetOutputModel;
import com.trazins.trazinsdroidpre.models.surgicalprocessmodel.SurgicalProcessInputModel;
import com.trazins.trazinsdroidpre.models.surgicalprocessmodel.SurgicalProcessOutputModel;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.utils.ConnectionParameters;
import com.trazins.trazinsdroidpre.utils.ErrorLogWriter;
import com.zebra.rfid.api3.TagData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialPostCounterActivity extends AppCompatActivity  implements RFIDHandler.ResponseHandlerInterface{

    public TextView statusTextViewRFID = null;
    private TextView textrfid;
    private TextView textViewSetName, textViewTheoreticalCounter;
    private TextView textViewPreCountRes, textViewPostCountRes, textViewPreCountTitle,
            textViewPostCountTitle;
    private EditText editTextRemarks;

    private Button buttonStartPreCounter, buttonValidatePreCounter,
            buttonValidatePostCounter, buttonStartPostCounter;
    BottomNavigationView bnv;

    String activityName;

    public UserOutputModel userLogged;
    public SP_SetOutputModel selectedSet;

    //Variable para obligar a que pulsen el botón de iniciar los recuentos.
    private boolean startCounter;

    //Variables para controlar que recuento se ha realizado
    private boolean preCounter;
    private boolean postCounter;

    private List<SP_InstrumentOutputModel> InstrumentList = new ArrayList<>();

    //Lista para almacenar los resultados sin duplicadades de códigos.
    private Set<SP_InstrumentOutputModel> TotalCounterList = new ArraySet<>(InstrumentList);

    RFIDHandler rfidHandler;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_post_counter);

        handler = new Handler(Looper.getMainLooper());
        //Usuario loggeado
        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        this.selectedSet =(SP_SetOutputModel)getIntent().getSerializableExtra("selectedSet");

        textViewSetName = findViewById(R.id.textViewMPCSetName);
        textViewTheoreticalCounter = findViewById(R.id.textViewMPCTheoricalCounter);
        statusTextViewRFID = findViewById(R.id.textViewRfidStatus);
        textrfid = findViewById(R.id.textViewRfidStatus);
        textViewPreCountRes = findViewById(R.id.textViewMPCPreMaterialCounter);
        textViewPreCountTitle = findViewById(R.id.textViewMPCPreMaterialCountTitle);
        textViewPostCountRes = findViewById(R.id.textViewMPCPostMaterialCounter);
        textViewPostCountTitle = findViewById(R.id.textViewMPCPostMaterialCountTitle);
        editTextRemarks = findViewById(R.id.editTextMPCRemarks);

        buttonStartPreCounter = findViewById(R.id.buttonStartPreCounter);
        buttonValidatePreCounter = findViewById(R.id.buttonValidatePreCounter);
        buttonStartPostCounter = findViewById(R.id.buttonStartPostCounter);
        buttonValidatePostCounter = findViewById(R.id.buttonValidatePostCounter);

        bnv = findViewById(R.id.bottomMPCNavigationMenu);

        activityName = this.getClass().getSimpleName();

        buttonStartPreCounter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startCounter = true;
                preCounter = true;
                textViewPreCountTitle.setBackgroundColor(getResources().getColor(R.color.green));
                textViewPreCountRes.setBackgroundColor(getResources().getColor(R.color.green));
                TotalCounterList.clear();
                textViewPreCountRes.setText(getResources().getText(R.string.materials_counter));
            }
        });

        buttonValidatePreCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCounter = false;
                preCounter = false;
                textViewPreCountTitle.setBackgroundColor(getResources().getColor(R.color.transparent));
                textViewPreCountRes.setBackgroundColor(getResources().getColor(R.color.transparent));

                setCounterResult(true);
            }
        });

        buttonStartPostCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedSet.PreCount==null || selectedSet.PreCount.equals("")){
                    Toast.makeText(getApplicationContext(), "no hay pre", Toast.LENGTH_LONG).show();
                    return;
                }

                startCounter = true;
                postCounter = true;
                textViewPostCountTitle.setBackgroundColor(getResources().getColor(R.color.green));
                textViewPostCountRes.setBackgroundColor(getResources().getColor(R.color.green));
                TotalCounterList.clear();
                textViewPostCountRes.setText(getResources().getText(R.string.materials_counter));
            }
        });

        buttonValidatePostCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCounter = false;
                postCounter = false;
                textViewPostCountTitle.setBackgroundColor(getResources().getColor(R.color.transparent));
                textViewPostCountRes.setBackgroundColor(getResources().getColor(R.color.transparent));

                setCounterResult(false);
            }
        });

        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.validate_counter) {
                    selectedSet.Remarks = editTextRemarks.getText().toString();
                    selectedSet.TheoreticalCounter = InstrumentList.size();
                    Intent i = new Intent(getApplicationContext(), SurgicalProcessActivity.class);
                    i.putExtra("recoveredSet", selectedSet);
                    setResult(RESULT_OK,i);
                }else{
                    finish();
                }

                return false;
            }
        });

        rfidHandler = new RFIDHandler();
        try{
            rfidHandler.onCreate(this);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(), activityName);
        }

        //Recuperar la información de la caja
        new SPGetDataMyAsyncClass().execute();
    }

    //Gestionamos los resultados del escaneo y de los recuentos
    private void setCounterResult(boolean counterType ) {
        List<SP_InstrumentOutputModel> newList = new ArrayList<>(InstrumentList);
        newList.removeAll(TotalCounterList);
        if(newList.size()==0){
            //Todo correcto, mostrar mensaje de estado??
            if(counterType)
                selectedSet.PreCount = TotalCounterList.size();
            else
                selectedSet.PostCount = TotalCounterList.size();
        }else{
            //Mostrar aviso y listado de articulos pendientes
            showAlertDialog(newList, counterType);
        }
    }

    private void showAlertDialog(List<SP_InstrumentOutputModel> newList, boolean counterType) {
        AlertDialog.Builder ad_builder = new AlertDialog.Builder(MaterialPostCounterActivity.this);
        ad_builder.setMessage(getResources().getText(R.string.alert_dialog_message))
                .setCancelable(false)
                .setPositiveButton(getResources().getText(R.string.show_list), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Abrir activity con el listado de instrumentos
                        Intent i = new Intent(getApplicationContext(), ShowInstrumentListActivity.class);
                        i.putExtra( "instrumentList", (Serializable) newList);
                        startActivity(i);
                    }
                })
                .setNegativeButton(getResources().getText(R.string.go_back), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setNeutralButton(getResources().getText(R.string.alert_dialog_validate_counter), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(counterType)
                            selectedSet.PreCount = TotalCounterList.size();
                        else
                            selectedSet.PostCount = TotalCounterList.size();
                    }
                });
        AlertDialog dialog = ad_builder.create();
        dialog.setTitle(getResources().getText(R.string.alert_dialog_title));
        dialog.show();
    }

    class SPGetDataMyAsyncClass extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            String methodName;
            String parameterName;
            //Variable para almacenar el resultado de la petición
            SP_SetOutputModel resultModel = null;

            SP_SetInputModel sp_setInputModel = new SP_SetInputModel();
            sp_setInputModel.MaterialCode = selectedSet.Id;

            //Desplegar el servicio:
            //Usamos la librería Fireexit para la gestión de la serialización.
            FireExitClient client = new FireExitClient(
                    ConnectionParameters.SOAP_ADDRESS[ConnectionParameters.SET_URL_CONNECTION]);

            methodName = "GetSurgicalProcessSetData";
            parameterName = "surgicalData";
            client.configure(new Configurator(
                    ConnectionParameters.NAME_SPACE, ConnectionParameters.CONTRACT_NAME, methodName));

                client.addParameter(parameterName, sp_setInputModel);
                resultModel = new SP_SetOutputModel();

            try {
                //Realizamos la llamada al web service para obtener los datos
                resultModel = client.call(resultModel);
            } catch (Exception e) {
                ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
                e.printStackTrace();
            }
            return resultModel;
        }

        @Override
        protected void onPostExecute(Object modelResult) {
            super.onPostExecute(modelResult);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(modelResult!= null){
                        processData(modelResult);
                    }else{
                        Toast.makeText(getBaseContext(), getText(R.string.unidentified_code), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    //Recuento de los intrumentos asociados a la caja desde Trazins
    private void processData(Object result){

        this.InstrumentList = ((SP_SetOutputModel)result).InstrumentList;
        String counter = String.valueOf(((SP_SetOutputModel)result).InstrumentList.size());
        textViewTheoreticalCounter.setText(counter);
        textViewSetName.setText(selectedSet.MaterialDescription);
    }

    @Override
    protected void onPause() {
        super.onPause();
        rfidHandler.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        String status = rfidHandler.onResume();
        statusTextViewRFID.setText(status);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandler.onDestroy();
    }

    //Revisar que datos tiene tgadata
    @Override
    public void handleTagdata(TagData[] tagData) {
        final StringBuilder sb = new StringBuilder();
        String tagId;

        for (int index = 0; index < tagData.length; index++) {
            /*sb.append(tagData[index].getTagID() + "\n");*/
            tagId = tagData[index].getTagID();
            addDistinctTags(tagId);
        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!startCounter){
                    Toast.makeText(getBaseContext(), R.string.not_pressed_counter_button, Toast.LENGTH_LONG).show();
                    return;
                }

                if(preCounter){
                    textViewPreCountRes.setText(TotalCounterList.size() + " " +
                            getText(R.string.materials_counter));
                    textViewPreCountRes.setTextColor(getResources().getColor(R.color.black));

                }
                if(postCounter){
                    textViewPostCountRes.setText(TotalCounterList.size() + " " +
                            getText(R.string.materials_counter));
                    textViewPostCountRes.setTextColor(getResources().getColor(R.color.black));

                }

                textrfid.append(sb.toString());
            }
        });
    }

    private void addDistinctTags(String tagId) {
        for(SP_InstrumentOutputModel m : InstrumentList){
            if(tagId!= "" && m.InstrumentCode != null){
                if(m.InstrumentCode.equals(tagId)){
                    TotalCounterList.add(m);
                }
            }
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   textrfid.setText("");
                }
            });
            rfidHandler.performInventory();
        } else
            rfidHandler.stopInventory();
    }
}