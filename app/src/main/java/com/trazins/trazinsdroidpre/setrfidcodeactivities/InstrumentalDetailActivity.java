package com.trazins.trazinsdroidpre.setrfidcodeactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.threepin.fireexit_wcf.Configurator;
import com.threepin.fireexit_wcf.FireExitClient;
import com.trazins.trazinsdroidpre.R;
import com.trazins.trazinsdroidpre.SetRFIDCodeActivity;
import com.trazins.trazinsdroidpre.models.sp_intrumentmodel.SP_InstrumentInputModel;
import com.trazins.trazinsdroidpre.models.sp_intrumentmodel.SP_InstrumentOutputModel;
import com.trazins.trazinsdroidpre.models.sp_setmodel.SP_SetInputModel;
import com.trazins.trazinsdroidpre.models.sp_setmodel.SP_SetOutputModel;
import com.trazins.trazinsdroidpre.surgicalprocessactivities.RFIDHandler;
import com.trazins.trazinsdroidpre.utils.ConnectionParameters;
import com.trazins.trazinsdroidpre.utils.ErrorLogWriter;
import com.zebra.rfid.api3.TagData;

public class InstrumentalDetailActivity extends AppCompatActivity implements RFIDHandler.ResponseHandlerInterface{

    public TextView statusTextViewRFID = null;
    private TextView textrfid;
    EditText editTextDM;

    String RFIDCode;

    String activityName;

    String setId = "";
    SP_InstrumentOutputModel instrument = new SP_InstrumentOutputModel();

    boolean Result;

    RFIDHandler rfidHandler;
    Handler handler;

    BottomNavigationView bnv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrumental_detail);

        activityName = this.getClass().getSimpleName();

        this.instrument = (SP_InstrumentOutputModel)getIntent().getSerializableExtra("instrument");
        this.setId = (String)getIntent().getSerializableExtra("setId");
        TextView textViewSetName = findViewById(R.id.textViewSetNameDetail);
        textViewSetName.setText(instrument.SetName);

        TextView textViewInstrumentalDescription = findViewById(R.id.textViewInstrumentDescriptionDetail);
        textViewInstrumentalDescription.setText(instrument.MaterialDescription);

        editTextDM = findViewById(R.id.editTextInstrumentDMDetail);
        editTextDM.setText(instrument.InstrumentDM);

        statusTextViewRFID = findViewById(R.id.textViewRfidStatusDetail);
        textrfid = findViewById(R.id.textViewRfidStatusDetail);

        bnv = findViewById(R.id.bottomInstrumentDetailActivity);
        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.save) {

                    setRFIDCode();
                }else{
                    finish();
                }

                return false;
            }
        });

        handler = new Handler(Looper.getMainLooper());

        rfidHandler = new RFIDHandler();
        try{
            rfidHandler.onCreate(this);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(), activityName);
        }
    }

    private void setRFIDCode(){
        new SetRFIDCodeAsyncClass().execute();
    }

    class SetRFIDCodeAsyncClass extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            String methodName;
            String parameterName;
            //Variable para almacenar el resultado de la petición
            SP_InstrumentOutputModel resultModel = null;

            SP_InstrumentInputModel instrumentInputModel = new SP_InstrumentInputModel();
            instrumentInputModel.InstrumentDM = instrument.InstrumentDM;
            instrumentInputModel.InstrumentId = instrument.Id;
            instrumentInputModel.SetId = setId;

            //Desplegar el servicio:
            //Usamos la librería Fireexit para la gestión de la serialización.
            FireExitClient client = new FireExitClient(
                    ConnectionParameters.SOAP_ADDRESS[ConnectionParameters.SET_URL_CONNECTION]);

            methodName = "SetRFIDCode";
            parameterName = "instrument";
            client.configure(new Configurator(
                    ConnectionParameters.NAME_SPACE, ConnectionParameters.CONTRACT_NAME, methodName));

            client.addParameter(parameterName, instrumentInputModel);
            resultModel = new SP_InstrumentOutputModel();

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
        instrument.Result = ((SP_InstrumentOutputModel)result).Result;

        if(instrument.Result){
            Intent i = new Intent(getApplicationContext(), SetRFIDCodeActivity.class);
            setResult(RESULT_OK,i);
            finish();
        }

    }
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
            /*sb.append(tagData[index].getTagID());*/
            RFIDCode = tagData[index].getTagID();
            addDistinctTags(RFIDCode);
        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textrfid.append(sb.toString());
                editTextDM.setText(RFIDCode);
            }
        });
    }

    private void addDistinctTags(String tagId) {
        instrument.InstrumentDM = tagId;
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*textrfid.setText("");*/
                }
            });
            rfidHandler.performInventory();
        } else
            rfidHandler.stopInventory();
    }
}