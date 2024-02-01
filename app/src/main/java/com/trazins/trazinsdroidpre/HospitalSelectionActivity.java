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
    TextView textViewShipmentResult, textViewUserName, textViewElements, textViewTrolleyName,
            textViewShipmentTitle;

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

    //Clase que gestiona la conexión con el web service y los envíos.
    class HospitalShipmentMyAsyncClass extends AsyncTask {
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