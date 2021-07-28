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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.scanner.DataWedgeInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ShipmentActivity extends AppCompatActivity {

    //Lista que muestra los resultados por pantalla
    ListView ListViewMaterials;

    //Lista que gestiona los materiales internamente.
    List<MaterialOutputModel> lstMaterial= new ArrayList<>();

    //Variable para gestionar el funcionamiento de la clase que gestiona la conexión webservice
    boolean setShipment = false;

    //Ubicación de destino
    OriginOutputModel finalShipment = new OriginOutputModel();

    //Material seleccionado en la lista
    MaterialOutputModel materialSelected = new MaterialOutputModel();

    //Lectura obtenida en el scanner
    String readCode;

    //Usuario logeado
    UserOutputModel userLogged;

    //Controles
    BottomNavigationView btm;
    TextView textViewShipmentResult, textViewUserName, textViewElements, textViewShipmentDetails;

    IntentFilter filter = new IntentFilter();

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment);
        handler = new Handler(Looper.getMainLooper());

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

        textViewShipmentResult = findViewById(R.id.textViewShipmentResult);
        textViewElements = findViewById(R.id.textViewShipmentMaterialCounter);
        textViewShipmentDetails = findViewById(R.id.textViewShipmentDetails);

        //Usuario loggeado
        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        textViewUserName = findViewById(R.id.textViewShipmentUserName);
        textViewUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);

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
                }else {
                    //Borrar elementos
                    removeSelectedMaterial();
                }
                return false;
            }
        });
    }

    private void setLocate() {
        try{
            if(finalShipment.OriginId == 0){
                Toast.makeText(getBaseContext(), R.string.locate_empty ,Toast.LENGTH_LONG).show();
                return;
            }
            setShipment = true;
            new ShipmentMyAsyncClass().execute();

        }catch (Exception e){
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
    }

    // Used EventBus to notify foreground activity of profile change
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataWedgeInterface.MessageEvent event) {
        //TextView txtActiveProfile = findViewById(R.id.textViewAutResult);
        //txtActiveProfile.setText(event.activeProfile);
    };

    //Clase que gestiona la conexión con el web service
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
                    "http://188.165.209.37:8009/Android/TrazinsDroidService.svc");

            //Si recibimos la orden de ubicar o de leer etiquetas.
            if(setShipment){
                //Usamos esta variable indicar que vamos a insertar los registros.
                setShipment = false;
                methodName = "SetShipmentData";
                parameterName = "dataToInsert";

                ShipmentInputModel shipmentInputModel = new ShipmentInputModel();
                shipmentInputModel.OriginId = finalShipment.OriginId;
                shipmentInputModel.EntryUser = userLogged.Login;
                for(MaterialOutputModel m : lstMaterial){
                    //Serializamos los materiales.
                    MaterialInputModel serializableMaterial = new MaterialInputModel();
                    serializableMaterial.MaterialCode = m.Id;
                    serializableMaterial.MaterialType = m.MaterialType;
                    serializableMaterial.MaterialDescription = m.MaterialDescription;
                    shipmentInputModel.MatList.add(serializableMaterial);
                }

                //Según el código hay que usar una clase de web service o otra;
                client.configure(new Configurator(
                        "http://tempuri.org/", "ITrazinsDroidService", methodName));

                client.addParameter(parameterName, shipmentInputModel);
                resultModel = new ShipmentOutputModel();

            }else{
                //Determinamos que tipo de objeto es el código leido
                if(readCode.substring(0,1).equals("O")){
                    methodName = "GetOrigin";
                    parameterName = "originCode";

                    OriginInputModel originInputModel = new OriginInputModel();
                    originInputModel.originCode = readCode;

                    //Según el código hay que usar una clase de web service o otra;
                    client.configure(new Configurator(
                            "http://tempuri.org/", "ITrazinsDroidService", methodName));

                    client.addParameter(parameterName, originInputModel);
                    resultModel = new OriginOutputModel();
                }else{
                    //Modelo Carro pdte desarrollo
                    if(readCode.substring(0,1).equals("C")){
                        //resultModel = new TrolleyOutpuModel();
                    }else{
                        methodName = "GetMaterialData";
                        parameterName = "materialCode";

                        MaterialInputModel materialInputModel = new MaterialInputModel();
                        materialInputModel.MaterialCode = readCode;

                        client.configure(new Configurator(
                                "http://tempuri.org/", "ITrazinsDroidService", methodName));
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
                    finalShipment = (OriginOutputModel)modelResult;
                    textViewShipmentResult.setText(((OriginOutputModel) modelResult).OriginDescription);

                    break;
                case "MaterialOutputModel":
                    addMaterialToList((MaterialOutputModel)modelResult);
                    break;
                case "ShipmentOutputModel":
                    if(((ShipmentOutputModel)modelResult).Result){
                        Toast.makeText(getBaseContext(),R.string.correct_shipment, Toast.LENGTH_LONG).show();
                        //Limpiar controles
                        cleanControlsViews();
                    }else{
                        Toast.makeText(getBaseContext(), R.string.error_process, Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    Toast.makeText(getBaseContext(), R.string.unidentified_code, Toast.LENGTH_LONG).show();
                    break;
            }

        }
    }

    private void cleanControlsViews() {
        lstMaterial.clear();
        ListViewMaterials.setAdapter(null);
        textViewElements.setText(lstMaterial.size()+ " " + getText(R.string.materials_counter));
        textViewShipmentResult.setText("");
        finalShipment = null;
    }

    private void addMaterialToList(MaterialOutputModel modelResult) {
        try{
            CustomAdapter adapter = new CustomAdapter(this, GetData(modelResult));
            ListViewMaterials.setAdapter(adapter);
            //Ponemos el color del selector igual que el del fondo para que no parezca que selecciona el primer
            //elemento
            ListViewMaterials.setSelector(R.color.transparent);

        }catch(Exception e){
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
                case "C":
                    materialImageType = R.drawable.ic_set_icon;
                    break;
                case "A":
                    materialImageType = R.drawable.ic_instrument_icon;
                    break;
                case "G":
                    materialImageType = R.drawable.ic_generic_icon;
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