package com.trazins.trazinsdroidpre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.threepin.fireexit_wcf.Configurator;
import com.threepin.fireexit_wcf.FireExitClient;
import com.trazins.trazinsdroidpre.models.sp_intrumentmodel.SP_InstrumentInputModel;
import com.trazins.trazinsdroidpre.models.sp_intrumentmodel.SP_InstrumentOutputModel;
import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialOutputModel;
import com.trazins.trazinsdroidpre.models.sp_setmodel.SP_SetInputModel;
import com.trazins.trazinsdroidpre.models.sp_setmodel.SP_SetOutputModel;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.scanner.DataWedgeInterface;
import com.trazins.trazinsdroidpre.setrfidcodeactivities.InstrumentalDetailActivity;
import com.trazins.trazinsdroidpre.utils.ConnectionParameters;
import com.trazins.trazinsdroidpre.utils.ErrorLogWriter;
import com.trazins.trazinsdroidpre.utils.InstrumentListCustomAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class SetRFIDCodeActivity extends AppCompatActivity {

    private ListView listViewInstruments;
    private static final String TAG = "SetRFIDCodeActivity";
    private static final int REQUEST_CODE = 77;
    IntentFilter filter = new IntentFilter();
    String activityName;
    Handler handler;
    String readCode;
    private List<SP_InstrumentOutputModel> InstrumentList = new ArrayList<>();

    private SP_InstrumentOutputModel selectedInstrument;

    public UserOutputModel userLogged;
    TextView textViewRfidSetNameResult,textViewRfidMaterialCounter;
    BottomNavigationView bnv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_rfidcode);

        filter.addAction(DataWedgeInterface.ACTION_RESULT_DATAWEDGE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(DataWedgeInterface.ACTIVITY_INTENT_FILTER_ACTION);

        handler = new Handler(Looper.getMainLooper());

        this.activityName= this.getClass().getSimpleName();

        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");

        listViewInstruments = findViewById(R.id.listViewRfidMaterials);
        listViewInstruments.setClickable(true);
        listViewInstruments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedInstrument = InstrumentList.get(i);
                //Habilitamos el selector de item en el listado.
                listViewInstruments.setSelector(R.color.selection);
                listViewInstruments.requestLayout();
            }
        });

        TextView textViewUserName = findViewById(R.id.textViewRfidUserName);
        textViewUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);
        textViewRfidSetNameResult = findViewById(R.id.textViewRfidSetNameResult);
        textViewRfidMaterialCounter = findViewById(R.id.textViewRfidMaterialCounter);
        bnv = findViewById(R.id.bottomRfidNavigationMenu);
        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.set_rfid_code) {
                    Intent i = new Intent(getApplicationContext(), InstrumentalDetailActivity.class);
                    i.putExtra("instrument", selectedInstrument);
                    startActivityForResult(i, REQUEST_CODE);

                }else{
                    finish();
                }

                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK){
                new GetDataMyAsyncClass().execute();
            }
            if (resultCode == RESULT_CANCELED) {
                // Write your code if there's no result
            }
        }
    }

    private List<SP_InstrumentOutputModel> convertList(List<SP_InstrumentOutputModel> instrumentList) {
        return  instrumentList;
    }

    private BroadcastReceiver myBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(DataWedgeInterface.ACTION_RESULT_DATAWEDGE))
            {
                if (intent.hasExtra(DataWedgeInterface.EXTRA_RESULT_GET_ACTIVE_PROFILE))
                {
                    String activeProfile = intent.getStringExtra(DataWedgeInterface.EXTRA_RESULT_GET_ACTIVE_PROFILE);
                    EventBus.getDefault().post(new DataWedgeInterface.MessageEvent(activeProfile));
                }
            }

            if (action.equals(DataWedgeInterface.ACTIVITY_INTENT_FILTER_ACTION)) {
                //Recibimos el barcode leido
                try {
                    displayScanResult(intent, "via Broadcast");
                }catch (Exception e){
                    Log.e(TAG, "Error en myBroadCastReceiver", e);
                    ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
                }
            }
        }
    };

    private void displayScanResult(Intent initiatingIntent, String howDataRecibed){
        String decodedData = initiatingIntent.getStringExtra(
                getResources().getString(R.string.datawedge_intent_key_data));

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Código leído: " + decodedData + " [" + howDataRecibed + "]");
        }

        readCode = decodedData;
        new GetDataMyAsyncClass().execute();
    }

    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        registerReceiver(myBroadCastReceiver, filter);

        // Retrieve current active profile using GetActiveProfile: http://techdocs.zebra.com/datawedge/latest/guide/api/getactiveprofile/
        DataWedgeInterface.sendDataWedgeIntentWithExtra(getApplicationContext(),
                DataWedgeInterface.ACTION_DATAWEDGE, DataWedgeInterface. EXTRA_GET_ACTIVE_PROFILE,
                DataWedgeInterface.EXTRA_EMPTY);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(myBroadCastReceiver);
    }

    // Used EventBus to notify foreground activity of profile change
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataWedgeInterface.MessageEvent event) {
        //TextView txtActiveProfile = findViewById(R.id.textViewAutResult);
        //txtActiveProfile.setText(event.activeProfile);
    };

    class GetDataMyAsyncClass extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            String methodName;
            String parameterName;
            //Variable para almacenar el resultado de la petición
            SP_InstrumentOutputModel resultModel = null;

            SP_InstrumentInputModel sp_InstrumentInputModel = new SP_InstrumentInputModel();
            sp_InstrumentInputModel.SetId = readCode;

            //Desplegar el servicio:
            //Usamos la librería Fireexit para la gestión de la serialización.
            FireExitClient client = new FireExitClient(
                    ConnectionParameters.SOAP_ADDRESS[ConnectionParameters.SET_URL_CONNECTION]);

            methodName = "GetSetData";
            parameterName = "SetId";
            client.configure(new Configurator(
                    ConnectionParameters.NAME_SPACE, ConnectionParameters.CONTRACT_NAME, methodName));

            client.addParameter(parameterName, sp_InstrumentInputModel);
            resultModel = new SP_InstrumentOutputModel();

            try {
                //Realizamos la llamada al web service para obtener los datos
                resultModel = client.call(resultModel);
            } catch (Exception e) {
                ErrorLogWriter
                        .writeToLogErrorFile(e.getMessage(),getApplicationContext(), activityName);
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

        this.InstrumentList = ((SP_InstrumentOutputModel)result).SetContent;

        String counter = String.valueOf(((SP_InstrumentOutputModel)result).SetContent.size());
        textViewRfidMaterialCounter.setText(counter + " " + getString(R.string.materials_counter));
        String setName = ((SP_InstrumentOutputModel)result).SetContent.get(0).SetName;
        textViewRfidSetNameResult.setText(setName);
        InstrumentListCustomAdapter adapter = new InstrumentListCustomAdapter(this,
                convertList(InstrumentList));
        listViewInstruments.setAdapter(adapter);
    }
}