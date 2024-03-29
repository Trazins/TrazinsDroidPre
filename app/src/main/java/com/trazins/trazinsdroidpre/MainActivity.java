package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.threepin.fireexit_wcf.Configurator;
import com.threepin.fireexit_wcf.FireExitClient;
import com.trazins.trazinsdroidpre.models.usermodel.UserInputModel;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.scanner.DataWedgeInterface;
import com.trazins.trazinsdroidpre.utils.ConnectionParameters;
import com.trazins.trazinsdroidpre.utils.ErrorLogWriter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String PROFILE1 = "TrazinsMultiActivity_Profile1";
    private static final String PROFILE2 = "TrazinsMultiActivity_Profile2";
    private static final String PROFILE3 = "TrazinsMultiActivity_Profile3";
    private static final String PROFILE4 = "TrazinsMultiActivity_Profile4";
    private static final String PROFILE5 = "TrazinsMultiActivity_Profile5";

    private static final int REQUEST_CODE = 77;

    private String activityName;

    TextView editTextAutResult;
    Button buttonAutResult;
    ImageButton buttonShowErrorLog;
    ImageView imageViewAutResult;
    Handler handler;

    UserOutputModel selectedHospitalUser = new UserOutputModel();
    UserOutputModel listaDeHospitales = new UserOutputModel();

    IntentFilter filter = new IntentFilter();

    //Variable que almacena el código leído por el lector.
    String readCode = "";

    private Boolean isConnected;
    public Boolean getConnected() {
        return isNetworkConnected(getApplicationContext());
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler(Looper.getMainLooper());

        this.activityName = this.getClass().getSimpleName();

        editTextAutResult = findViewById(R.id.editAutResult);
        buttonAutResult = findViewById(R.id.buttonAutResult);
        imageViewAutResult= findViewById(R.id.imageViewUser);
        buttonShowErrorLog = findViewById(R.id.buttonShowErrorLog);

        //setConnected(isNetworkConnected(this));
        if(!getConnected()){
            Toast.makeText(this, R.string.wifi_not_conected, Toast.LENGTH_LONG).show();
        }

        buttonAutResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextAutResult.setVisibility(View.VISIBLE);
            }
        });

        buttonShowErrorLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),ShowErrorLogActivity.class);
                startActivity(i);
            }
        });

        editTextAutResult.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (getConnected()) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        readCode = editTextAutResult.getText().toString();
                        new MyAsyncClass().execute();
                        editTextAutResult.setText("");
                        editTextAutResult.setVisibility(View.INVISIBLE);
                        return true;
                    }
                }
                return false;
            }
        });

        filter.addAction(DataWedgeInterface.ACTION_RESULT_DATAWEDGE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(DataWedgeInterface.ACTIVITY_INTENT_FILTER_ACTION);

        createProfileForeachActivity();
    }

    private void createProfileForeachActivity() {
        // Create Profile 1 for MainActivity:
        String Code128Value = "true";
        String EAN13Value = "false";
        CreateProfile(PROFILE1, Code128Value, EAN13Value);

        // Create Profile 2 for LocateActivity:
        CreateProfile(PROFILE2, Code128Value, EAN13Value);

        //Create Profile 3 for ShipmentActivity
        CreateProfile(PROFILE3, Code128Value, EAN13Value);

        //Create Profile 4 for SurgicalProcessActivity
        CreateProfile(PROFILE4, Code128Value, EAN13Value);

        //Create Profile 5 for HospitalShipmentActivity
        CreateProfile(PROFILE5, Code128Value, EAN13Value);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
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
    private void sendMessage(){
        Toast.makeText(getBaseContext(),"Aqui", Toast.LENGTH_LONG).show();
    }

    //Clase que gestiona la conexión con el web service
    class MyAsyncClass extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {

            //Usamos los mismos nombres en las clases para que la serialización se realice correctamente
            UserInputModel userInputModelData = new UserInputModel();
            userInputModelData.SignatureCode = readCode;

            //Desplegar el servicio:
            //Usamos la librería Fireexit para la gestión de la serialización.
            FireExitClient client = new FireExitClient(
                    ConnectionParameters.SOAP_ADDRESS[ConnectionParameters.SET_URL_CONNECTION]);
            client.configure(new Configurator(
                    ConnectionParameters.NAME_SPACE, ConnectionParameters.CONTRACT_NAME, "GetUser"));

            client.addParameter("signature", userInputModelData);

            UserOutputModel userOutputModelLogged = new UserOutputModel();

            try {
                //Realizamos la llamada al web service para obtener los datos
                userOutputModelLogged = client.call(userOutputModelLogged);
            } catch (Exception e) {
                e.printStackTrace();
                ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(), activityName);
                //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return userOutputModelLogged;
        }

        @Override
        protected void onPostExecute(Object userLogged) {
            super.onPostExecute(userLogged);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    List<UserOutputModel>userlist = ((UserOutputModel)userLogged).UsersList;
                    if(userlist.size()==1){
                        setInformationMessage(userlist.get(0), true);
                    }else if(userlist.size()>1){
                        //Mostramos selector de hospital
                        setInformationMessage(userlist.get(0), false);
                        Intent i = new Intent(getApplicationContext(), HospitalSelectionActivity.class);
                        i.putExtra("userLogged", ((UserOutputModel) userLogged));
                        startActivityForResult(i,REQUEST_CODE);
                    }else{
                        //Toast.makeText(getBaseContext(),userLogged.toString(),Toast.LENGTH_LONG).show();
                        setInformationMessage((UserOutputModel) userLogged, false);
                    }
                }
            });
        }
    }

    private void setInformationMessage(UserOutputModel userLogged, boolean openSelectionActivity) {
        if(userLogged.Login!=null){
            //textViewAutResult.setText(((UserOutputModel) userLogged).UserName);
            //Por si primero no leen bien el código que les aparezca correcto.
            buttonAutResult.setTextColor(getResources().getColor(R.color.green));
            buttonAutResult.setText(R.string.correct_user);
            if(openSelectionActivity){
                Intent switchActivity = new Intent(getApplicationContext(), SelectionActivity.class);
                switchActivity.putExtra("userLogged",((UserOutputModel) userLogged));
                startActivity(switchActivity);
            }
        }
        else{
            //textViewAutResult.setText(R.string.incorrect_user);
            buttonAutResult.setTextColor(getResources().getColor(R.color.red));
            buttonAutResult.setText(R.string.incorrect_user);
        }
    }

    private void setInformationMessage(UserOutputModel userLogged, UserOutputModel listaDeHospitales, boolean openSelectionActivity) {
        if(userLogged.Login!=null){
            //textViewAutResult.setText(((UserOutputModel) userLogged).UserName);
            //Por si primero no leen bien el código que les aparezca correcto.
            buttonAutResult.setTextColor(getResources().getColor(R.color.green));
            buttonAutResult.setText(R.string.correct_user);
            if(openSelectionActivity){
                Intent switchActivity = new Intent(getApplicationContext(), SelectionActivity.class);
                switchActivity.putExtra("userLogged",((UserOutputModel) userLogged));
                switchActivity.putExtra("listaDeHospitales",((UserOutputModel) listaDeHospitales));
                startActivity(switchActivity);
            }
        }
        else{
            //textViewAutResult.setText(R.string.incorrect_user);
            buttonAutResult.setTextColor(getResources().getColor(R.color.red));
            buttonAutResult.setText(R.string.incorrect_user);
        }
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
                    ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(), activityName);
                }
            }
        }
    };

    private void displayScanResult(Intent initiatingIntent, String howDataRecibed){

        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));

        readCode = decodedData;
        if(getConnected())
            new MyAsyncClass().execute();
    };

    //Crea los archivos para la aplicación Datawedge de Zebra
    private void CreateProfile (String profileName, String code128Value, String ean13Value){

        // Configure profile to apply to this app
        Bundle bMain = new Bundle();
        bMain.putString("PROFILE_NAME", profileName);
        bMain.putString("PROFILE_ENABLED", "true");
        bMain.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");  // Create profile if it does not exist

        // Configure barcode input plugin
        Bundle bConfigBarcode = new Bundle();
        bConfigBarcode.putString("PLUGIN_NAME", "BARCODE");
        bConfigBarcode.putString("RESET_CONFIG", "true"); //  This is the default

        // PARAM_LIST bundle properties
        Bundle bParamsBarcode = new Bundle();
        bParamsBarcode.putString("scanner_selection", "auto");
        bParamsBarcode.putString("scanner_input_enabled", "true");
        bParamsBarcode.putString("decoder_code128", code128Value);
        bParamsBarcode.putString("decoder_ean13", ean13Value);

        // Bundle "bParamsBarcode" within bundle "bConfigBarcode"
        bConfigBarcode.putBundle("PARAM_LIST", bParamsBarcode);

        // Associate appropriate activity to profile
        String activityName;
        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", getPackageName());
        if (profileName.equals(PROFILE1))
        {
            activityName = MainActivity.class.getSimpleName();
        }
        else if(profileName.equals(PROFILE2)){
            activityName = LocateActivity.class.getSimpleName();
        }
        else if(profileName.equals(PROFILE3)){
            activityName = ShipmentActivity.class.getSimpleName();
        }
        else if(profileName.equals(PROFILE4)){
            activityName = SurgicalProcessActivity.class.getSimpleName();
        }else{
            activityName = HospitalShipment.class.getSimpleName();
        }

        String activityPackageName = getPackageName() + "." + activityName;
        appConfig.putStringArray("ACTIVITY_LIST", new String[] {activityPackageName});
        bMain.putParcelableArray("APP_LIST", new Bundle[]{appConfig});

        // Configure intent output for captured data to be sent to this app
        Bundle bConfigIntent = new Bundle();
        bConfigIntent.putString("PLUGIN_NAME", "INTENT");
        bConfigIntent.putString("RESET_CONFIG", "true");

        // Set params for intent output
        Bundle bParamsIntent = new Bundle();
        bParamsIntent.putString("intent_output_enabled", "true");
        bParamsIntent.putString("intent_action", "com.zebra.dwmultiactivity.ACTION");
        bParamsIntent.putString("intent_delivery", "2");

        // Bundle "bParamsIntent" within bundle "bConfigIntent"
        bConfigIntent.putBundle("PARAM_LIST", bParamsIntent);

        // Place both "bConfigBarcode" and "bConfigIntent" bundles into arraylist bundle
        ArrayList<Bundle> bundlePluginConfig = new ArrayList<>();
        bundlePluginConfig.add(bConfigBarcode);
        bundlePluginConfig.add(bConfigIntent);

        // Place bundle arraylist into "bMain" bundle
        bMain.putParcelableArrayList("PLUGIN_CONFIG", bundlePluginConfig);

        // Apply configs using SET_CONFIG: http://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
        DataWedgeInterface.sendDataWedgeIntentWithExtra(getApplicationContext(),
                DataWedgeInterface.ACTION_DATAWEDGE, DataWedgeInterface.EXTRA_SET_CONFIG, bMain);

        //Toast.makeText(getApplicationContext(), "Created profiles.  Check DataWedge app UI.", Toast.LENGTH_LONG).show();
    }

    //Para poder procesar la información recibida de la pantalla de recuento
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK){
                this.selectedHospitalUser = (UserOutputModel) data.getSerializableExtra("selectedHospitalUser");
                this.listaDeHospitales = (UserOutputModel) data.getSerializableExtra("listaDeHospitales");
                setInformationMessage(selectedHospitalUser, listaDeHospitales, true);
            }
            if (resultCode == RESULT_CANCELED) {
                // Write your code if there's no result
            }
        }
    } //onActivityResult
}