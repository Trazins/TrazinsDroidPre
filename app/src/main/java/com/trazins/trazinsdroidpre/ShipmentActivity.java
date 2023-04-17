package com.trazins.trazinsdroidpre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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
import com.trazins.trazinsdroidpre.scanner.DataWedgeInterface;
import com.trazins.trazinsdroidpre.utils.ConnectionParameters;
import com.trazins.trazinsdroidpre.utils.CustomAdapter;
import com.trazins.trazinsdroidpre.utils.ErrorLogWriter;
import com.trazins.trazinsdroidpre.utils.SettingsHelper;
import com.trazins.trazinsdroidpre.utils.ThreadSleeper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShipmentActivity extends AppCompatActivity {

    //region Variables

    String activityName;

    //Lista que muestra los resultados por pantalla
    ListView ListViewMaterials;

    //Lista que gestiona los materiales internamente.
    List<MaterialOutputModel> lstMaterial= new ArrayList<>();

    //Variable para gestionar el funcionamiento de la clase que gestiona la conexión webservice
    boolean setShipment = false;

    //Ubicación de destino
    OriginOutputModel finalOrigin = new OriginOutputModel();

    //Carro asociado al envío final
    TrolleyOutputModel finalTrolley;

    //Registro de envío generado para insertar en bd
    ShipmentInputModel createShipment = new ShipmentInputModel();
    ShipmentOutputModel shipmentResult = new ShipmentOutputModel();

    //Material seleccionado en la lista
    MaterialOutputModel materialSelected = new MaterialOutputModel();

    //Conexión con la impresora para imprimir la etiqueta.
    Connection printerConnection;

    //Lectura obtenida en el scanner
    String readCode;

    String originDescription= "";

    //Usuario logeado
    UserOutputModel userLogged;

    //Sirve para crear una entrada o salida de material.
    boolean toCentral;

    //Controles
    BottomNavigationView btm;
    TextView textViewShipmentResult, textViewUserName, textViewElements, textViewTrolleyName,
            textViewShipmentTitle;
    Switch switchUrgent;

    IntentFilter filter = new IntentFilter();

    Handler handler;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment);
        handler = new Handler(Looper.getMainLooper());

        this.activityName= this.getClass().getSimpleName();

        ListViewMaterials = findViewById(R.id.listViewShipmentMaterials);
        ListViewMaterials.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                materialSelected = lstMaterial.get(position);

                //Habilitamos el selector de item en el listado.
                ListViewMaterials.setSelector(R.color.selection);
                ListViewMaterials.requestLayout();
            }
        });

        switchUrgent = findViewById(R.id.switchSUrgent);

        textViewShipmentResult = findViewById(R.id.textViewShipmentResult);
        textViewElements = findViewById(R.id.textViewShipmentMaterialCounter);
        textViewTrolleyName = findViewById(R.id.textViewTrolleyName);
        textViewShipmentTitle = findViewById(R.id.textViewShipmentTitle);

        //Usuario loggeado
        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        this.toCentral = (boolean)getIntent().getSerializableExtra("toCentral");
        textViewUserName = findViewById(R.id.textViewShipmentUserName);
        textViewUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);

        if(toCentral){
            textViewShipmentTitle.setText(R.string.shipment_title2);
            textViewShipmentResult.setText(R.string.shipment_empty2);
        }

        filter.addAction(DataWedgeInterface.ACTION_RESULT_DATAWEDGE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(DataWedgeInterface.ACTIVITY_INTENT_FILTER_ACTION);


        btm = findViewById(R.id.bottomShipmentNavigationMenu);
        btm.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.add_shipment){
                    //ubicar elementos
                    setLocate();
                }else if(item.getItemId()== R.id.printlabel){
                    //Imprimir y enviar
                    setLocateAndPrint();
                }else {
                    //Borrar elementos
                    removeSelectedMaterial();
                }
                return false;
            }
        });
    }

    //Es necesario crear un hilo nuevo para la conexión TCP
    private void setLocateAndPrint() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                setLocate();
                ThreadSleeper.sleep(2000);
                Looper.prepare();
                createPrinterConnection();
                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();
    }

    private void setLocate() {
        try{
            if(finalOrigin.OriginId == 0){
                Toast.makeText(getBaseContext(), R.string.locate_empty ,Toast.LENGTH_LONG).show();
                return;
            }
            setShipment = true;
            new ShipmentMyAsyncClass().execute();

        }catch (Exception e){
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            Toast.makeText(getBaseContext(),"Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void removeSelectedMaterial() {
        CustomAdapter adapter = new CustomAdapter(this, removeData(materialSelected));
        ListViewMaterials.setAdapter(adapter);
    }

    private List<MaterialOutputModel> removeData(MaterialOutputModel material){
        lstMaterial.remove(material);
        return lstMaterial;
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
        disconnect();
    }

    // Used EventBus to notify foreground activity of profile change
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataWedgeInterface.MessageEvent event) {
        //TextView txtActiveProfile = findViewById(R.id.textViewAutResult);
        //txtActiveProfile.setText(event.activeProfile);
    };

    //Clase que gestiona la conexión con el web service y los envíos.
    class ShipmentMyAsyncClass extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            String methodName;
            String parameterName;
            //Variable para almacenar el resultado de la petición
            Object resultModel = null;

            //Desplegar el servicio:
            //Usamos la librería Fireexit para la gestión de la serialización.
            FireExitClient client = new FireExitClient(
                    ConnectionParameters.SOAP_ADDRESS[ConnectionParameters.SET_URL_CONNECTION]);

            //Si recibimos la orden de ubicar o de leer etiquetas.
            if(setShipment){
                //Usamos esta variable indicar que vamos a insertar los registros.
                setShipment = false;
                methodName = "SetShipmentData";
                parameterName = "dataToInsert";

                //Mapeamos el registro a insertar
                createShipment = new ShipmentInputModel();
                createShipment.OriginId = finalOrigin.OriginId;
                createShipment.HosId = userLogged.HosId;
                createShipment.ToCentral = toCentral;
                createShipment.Urgent = switchUrgent.isChecked();
                createShipment.EntryUser = userLogged.Login;
                if(finalTrolley!= null)
                    createShipment.TrolleyCode = finalTrolley.TrolleyCode;
                for(MaterialOutputModel m : lstMaterial){
                    //Serializamos los materiales.
                    MaterialInputModel serializableMaterial = new MaterialInputModel();
                    serializableMaterial.MaterialCode = m.Id;
                    serializableMaterial.MaterialType = m.MaterialType;
                    serializableMaterial.MaterialDescription = m.MaterialDescription;
                    createShipment.MatList.add(serializableMaterial);
                }

                //Según el código hay que usar una clase de web service o otra;
                client.configure(new Configurator(
                        ConnectionParameters.NAME_SPACE, ConnectionParameters.CONTRACT_NAME, methodName));

                client.addParameter(parameterName, createShipment);
                resultModel = new ShipmentOutputModel();

            }else{
                //Determinamos que tipo de objeto es el código leido
                if(readCode.startsWith("O")){
                    methodName = "GetOrigin";
                    parameterName = "originCode";

                    OriginInputModel originInputModel = new OriginInputModel();
                    originInputModel.originCode = readCode;
                    originInputModel.HosId = userLogged.HosId;

                    //Según el código hay que usar una clase de web service o otra;
                    client.configure(new Configurator(
                            ConnectionParameters.NAME_SPACE, ConnectionParameters.CONTRACT_NAME, methodName));

                    client.addParameter(parameterName, originInputModel);
                    resultModel = new OriginOutputModel();
                }else{

                    if(readCode.startsWith("C")){
                        methodName = "GetTrolleyData";
                        parameterName= "trolleyCode";

                        TrolleyInputModel trolleyInputModel = new TrolleyInputModel();
                        trolleyInputModel.TrolleyCode = readCode;
                        trolleyInputModel.HosId = userLogged.HosId;

                        client.configure(new Configurator(
                                ConnectionParameters.NAME_SPACE,ConnectionParameters.CONTRACT_NAME, methodName));
                        client.addParameter(parameterName, trolleyInputModel);
                        resultModel = new TrolleyOutputModel();
                    }else{
                        methodName = "GetMaterialData";
                        parameterName = "materialCode";

                        MaterialInputModel materialInputModel = new MaterialInputModel();
                        materialInputModel.MaterialCode = readCode;

                        client.configure(new Configurator(
                                ConnectionParameters.NAME_SPACE, ConnectionParameters.CONTRACT_NAME, methodName));
                        client.addParameter(parameterName, materialInputModel);

                        resultModel = new MaterialOutputModel();
                    }
                }
            }

            try {
                //Realizamos la llamada al web service para obtener los datos
                resultModel = client.call(resultModel);
            } catch (Exception e) {
                e.printStackTrace();
                ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
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

        private void processData(Object modelResult) {
            String modelType = modelResult.getClass().getSimpleName();
            switch(modelType){
                case "OriginOutputModel":
                    //PDTE, comprobar que el hosid del usuario es el mismo que el del origen?
                    finalOrigin = (OriginOutputModel)modelResult;
                    textViewShipmentResult.setText(((OriginOutputModel) modelResult).OriginDescription);
                    break;

                case "MaterialOutputModel":
                    addMaterialToList((MaterialOutputModel)modelResult);
                    break;

                case "ShipmentOutputModel":
                    refreshUI((ShipmentOutputModel)modelResult);
                    break;

                case"TrolleyOutputModel":

                    if(((TrolleyOutputModel) modelResult).LocateId!= null){
                        Toast.makeText(getBaseContext(),R.string.located_trolley, Toast.LENGTH_LONG).show();
                    }else {

                        finalTrolley = (TrolleyOutputModel)modelResult;
                        textViewTrolleyName.setText(((TrolleyOutputModel)modelResult).TrolleyName);
                    }
                    break;

                default:
                    Toast.makeText(getBaseContext(), R.string.unidentified_code, Toast.LENGTH_LONG).show();
                    break;
            }

        }
    }

    private void refreshUI(ShipmentOutputModel modelResult) {
        try{

            if(modelResult.Result){
                shipmentResult = modelResult;
                originDescription = finalOrigin.OriginDescription;
                Toast.makeText(getBaseContext(), R.string.correct_shipment, Toast.LENGTH_LONG).show();

                //Imprimir la etiqueta
                if (finalTrolley != null) {
                    printLabel((ShipmentOutputModel) modelResult);
                }
                cleanControlsViews();
            }else{
                Toast.makeText(getBaseContext(), R.string.error_process, Toast.LENGTH_LONG).show();
            }

        }catch(Exception e){
            e.printStackTrace();
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void createPrinterConnection() {
        //Recuperamos los datos de la impresora guardados en el archivo de preferencias
        String portToParse = SettingsHelper.getPort(this);
        if(portToParse.equals("")){
            Toast.makeText(getBaseContext(), R.string.printing_error,Toast.LENGTH_LONG).show();
            return;
        }
        int port = Integer.parseInt(portToParse);
        if(SettingsHelper.getIp(this).equals("")){
            Toast.makeText(getBaseContext(), R.string.printing_error,Toast.LENGTH_LONG).show();
            return;
        }

        printerConnection = new TcpConnection(
                SettingsHelper.getIp(this), port);
        try {
            printerConnection.open();
        } catch (ConnectionException e) {
            e.printStackTrace();
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void printLabel(ShipmentOutputModel resultShipment)  {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                createShipmentLabel(resultShipment);
                Looper.loop();
                Looper.myLooper().quit();

            }
        }).start();
    }

    private void createShipmentLabel(ShipmentOutputModel resultShipment) {
        try{
            if(printerConnection == null){
                return;
            }
            if(printerConnection.isConnected()){
                byte[] label = createLabel(resultShipment);
                printerConnection.write(label);
            }
        }catch(ConnectionException e){
            e.printStackTrace();
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private byte[] createLabel(ShipmentOutputModel resultShipment) {
        String originName = finalOrigin.OriginDescription;
        String total = String.valueOf(createShipment.MatList.size());
        //Formato que hay que obtener idEs, origenId, (Pendiente añadir propiedad para guardar el dato en el insert)carroSeleccionado
        String barcode = resultShipment.ESId + "-" + resultShipment.OriginId + "-" + resultShipment.TrolleyCode;
        String label =  "^XA"+
                        "^LH0,0"+"\r\n"+
                        "^FO50,20" + "\r\n" + "^BCN,90,Y,N,N" + "\r\n" + "^FD" + barcode + "^FS" + "\r\n" +
                        "^FO50,170" + "\r\n" + "^A0,N,40,40" + "\r\n" + "^FD"+ originName +"^FS" + "\r\n" +
                        "^FO50,220" + "\r\n" + "^A0,N,40,40" + "\r\n" + "^FDCAN: " + total + "^FS" + "\r\n" +
                        "^XZ";
        return label.getBytes();
    }

    //Método para desconectar la impresora
    public void disconnect() {
        try {

            if (printerConnection != null) {
                printerConnection.close();
            }

        } catch (ConnectionException e) {
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    private void cleanControlsViews() {
        lstMaterial.clear();
        ListViewMaterials.setAdapter(null);
        textViewElements.setText(lstMaterial.size()+ " " + getText(R.string.materials_counter));
        textViewShipmentResult.setText("");
        textViewTrolleyName.setText("");
        finalOrigin = null;
        finalTrolley = null;
        switchUrgent.setChecked(false);
    }

    private void addMaterialToList(MaterialOutputModel modelResult) {
        try{
            CustomAdapter adapter = new CustomAdapter(this, GetData(modelResult));
            ListViewMaterials.setAdapter(adapter);
            //Ponemos el color del selector igual que el del fondo para que no parezca que selecciona el primer
            //elemento
            ListViewMaterials.setSelector(R.color.transparent);

        }catch(Exception e){
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
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
                    ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
                }
            }
        }
    };

    private void displayScanResult(Intent initiatingIntent, String howDataRecibed){
        //String decodedSource = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_source));
        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
        //String decodedLabelType = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type));
        readCode = decodedData;

        new ShipmentMyAsyncClass().execute();
    };

    private List<MaterialOutputModel> GetData(MaterialOutputModel material) {
        try {
            int materialImageType=0;
            switch (material.MaterialType){
                case "S":
                    materialImageType = R.drawable.ic_set_icon;
                    break;
                case "I":
                    materialImageType = R.drawable.ic_instrument_icon;
                    break;
                case "G":
                    materialImageType = R.drawable.ic_generic_icon;
                    break;
                case "L":
                    materialImageType = R.drawable.ic_loan_icon;
                    break;
                default:
                    materialImageType = 0;
                    break;
            }

            //Hay que insertar el primer elemento
            if(lstMaterial.size()==0){
                lstMaterial.add(
                        new MaterialOutputModel(material.Id, materialImageType,material.MaterialDescription,material.MaterialType));
            }else {
                if(existsInList(material.Id)){
                    Toast.makeText(getBaseContext(), R.string.material_exists_list, Toast.LENGTH_LONG).show();
                }else{
                    lstMaterial.add(
                            new MaterialOutputModel(material.Id, materialImageType,material.MaterialDescription, material.MaterialType));
                }
            }
            textViewElements.setText(lstMaterial.size() + " " + getText(R.string.materials_counter));


        }catch (Exception e){
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return lstMaterial;
    }

    //Comprobamos que el material no exista en la lista
    private boolean existsInList(String materialId){
        for(MaterialOutputModel m : lstMaterial){
            if(m.getId().equals(materialId)){
                return true;
            }
        }
        return false;
    }



}