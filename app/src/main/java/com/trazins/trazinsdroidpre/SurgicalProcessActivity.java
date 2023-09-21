package com.trazins.trazinsdroidpre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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
import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialInputModel;
import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialOutputModel;
import com.trazins.trazinsdroidpre.models.sp_setmodel.SP_SetOutputModel;
import com.trazins.trazinsdroidpre.utils.CustomAdapter;
import com.trazins.trazinsdroidpre.surgicalprocessactivities.MaterialPostCounterActivity;
import com.trazins.trazinsdroidpre.models.materialmodel.MaterialInputModel;
import com.trazins.trazinsdroidpre.models.materialmodel.MaterialOutputModel;
import com.trazins.trazinsdroidpre.models.surgicalprocessmodel.SurgicalProcessInputModel;
import com.trazins.trazinsdroidpre.models.surgicalprocessmodel.SurgicalProcessOutputModel;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.scanner.DataWedgeInterface;
import com.trazins.trazinsdroidpre.utils.ConnectionParameters;
import com.trazins.trazinsdroidpre.utils.ErrorLogWriter;
import com.trazins.trazinsdroidpre.utils.SPMaterialCustomAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

//No puede estar en un subpaquete porque sino el datawedge no lo reconoce
public class SurgicalProcessActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 77;

    SP_MaterialOutputModel materialSelected = new SP_MaterialOutputModel();
    List<SP_MaterialOutputModel> lstMaterial;

    //Usuario logeado
    UserOutputModel userLogged;
    SurgicalProcessOutputModel surgicalProcess;

    SP_SetOutputModel recoveredSet = new SP_SetOutputModel();

    String activityName;

    boolean result = false;

    //La usamos para actualizar el registro en lugar de crear uno nuevo.
    boolean isUpdate = false;

    //Lectura obtenida en el scanner
    String readCode;

    boolean createNewSurgicalProcess = false;
    IntentFilter filter = new IntentFilter();

    Handler handler;

    ListView ListViewMaterials;
    TextView textViewUserName, textViewElements;
    BottomNavigationView bnv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgical_process);

        handler = new Handler(Looper.getMainLooper());

        this.activityName= this.getClass().getSimpleName();
        lstMaterial = new ArrayList<SP_MaterialOutputModel>();

        ListViewMaterials = findViewById(R.id.listViewSPMaterials);
        ListViewMaterials.setClickable(true);
        ListViewMaterials.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                materialSelected = lstMaterial.get(position);

                //Habilitamos el selector de item en el listado.
                ListViewMaterials.setSelector(R.color.selection);
                ListViewMaterials.requestLayout();
            }
        });

        ListViewMaterials.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                materialSelected = lstMaterial.get(position);

                materialSelected.ChemicalControl = !materialSelected.ChemicalControl;
                ListViewMaterials.setSelector(R.color.selection);
                changeChemicalControlValue();
                ListViewMaterials.requestLayout();
                return false;
            }
        });

        //Usuario loggeado
        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        this.surgicalProcess = (SurgicalProcessOutputModel)getIntent().getSerializableExtra("surgicalProcess");
        this.isUpdate = getIntent().getBooleanExtra("isUpdate", false);

        textViewUserName = findViewById(R.id.textViewSPUserName);
        textViewUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);
        textViewElements = findViewById(R.id.textViewSPMaterialCounter);

        filter.addAction(DataWedgeInterface.ACTION_RESULT_DATAWEDGE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(DataWedgeInterface.ACTIVITY_INTENT_FILTER_ACTION);

        bnv = findViewById(R.id.bottomSPNavigationMenu);
        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.add_surgical_process){
                    //Añadir registro en bd
                    newSurgicalProcess();

                    //Revisar si da error que no salga de la pantalla

                    //Volver a la pantalla de procesos quirúrgicos y borrar los datos del proceso
                    closeScreenWithResult();

                }else if(item.getItemId()==R.id.start_counter){
                    if(materialSelected.MaterialType == null){
                        return false;
                    }
                    //Abrir pantalla de recuento
                    if(materialSelected.getMaterialType().equals("S")){
                        openMPCActivity();
                    }else{
                        Toast.makeText(getApplicationContext(), R.string.set_not_selected, Toast.LENGTH_LONG).show();
                    }

                }else{
                    removeSelected();
                }
                return false;
            }
        });

        //Si es una modificación hay que cargar los materiales.
        if(isUpdate){

            for(SP_MaterialOutputModel m : this.surgicalProcess.MaterialOutputModelList){
                addMaterialToList(m);
            }
        }
    }

    private void closeScreenWithResult(){
        Intent i = getIntent();
        setResult(RESULT_OK,i);
        finish();
    }

    private void openMPCActivity(){
        SP_SetOutputModel selectedSet = new SP_SetOutputModel();
        selectedSet.HisId = this.surgicalProcess.HisId;
        selectedSet.MaterialDescription = this.materialSelected.getMaterialDescription();
        selectedSet.Id = this.materialSelected.Id;
        selectedSet.MaterialType = this.materialSelected.MaterialType;

        Intent i = new Intent(getApplicationContext(), MaterialPostCounterActivity.class);
        i.putExtra("userLogged", this.userLogged);
        i.putExtra("selectedSet", selectedSet);
        //Enviar datos de la caja
        startActivityForResult(i, REQUEST_CODE);
    }

    //Para poder procesar la información recibida de la pantalla de recuento
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK){
                this.recoveredSet = (SP_SetOutputModel)data.getSerializableExtra("recoveredSet");
                for(SP_MaterialOutputModel m : lstMaterial){
                    if(m.getId().equals(recoveredSet.Id)){

                        m.PreCount= recoveredSet.PreCount;
                        m.PostCount = recoveredSet.PostCount;
                        m.TheoreticalCounter = recoveredSet.TheoreticalCounter;
                        m.Remarks = recoveredSet.Remarks;

                    }
                }
            }
            if (resultCode == RESULT_CANCELED) {
                // Write your code if there's no result
            }
        }
    } //onActivityResult

    private void newSurgicalProcess(){
        try{
            createNewSurgicalProcess = true;

            new SurgicalProcessMyAsyncClass().execute().toString();

        }catch(Exception e){
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            Toast.makeText(getBaseContext(), e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void changeChemicalControlValue(){
        SPMaterialCustomAdapter adapter = new SPMaterialCustomAdapter(this, lstMaterial);
        ListViewMaterials.setAdapter(adapter);
    }

    private void removeSelected(){
        SPMaterialCustomAdapter adapter = new SPMaterialCustomAdapter(this, removeData(materialSelected));
        ListViewMaterials.setAdapter(adapter);
    }

    private List<SP_MaterialOutputModel> removeData(SP_MaterialOutputModel material){
        try{
            lstMaterial.remove(material);
            return lstMaterial;
        }catch(Exception e){
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            Toast.makeText(getBaseContext(), e.getMessage(),Toast.LENGTH_LONG).show();
            return null;
        }
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
    class SurgicalProcessMyAsyncClass extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            String methodName;
            String parameterName;
            String parameterName1;

            //Variable para almacenar el resultado de la petición
            Object resultModel = null;

            //Desplegar el servicio:
            //Usamos la librería Fireexit para la gestión de la serialización.
            FireExitClient client = new FireExitClient(
                    ConnectionParameters.SOAP_ADDRESS[ConnectionParameters.SET_URL_CONNECTION]);

            //Si recibimos la orden de ubicar o de leer etiquetas.
            if(createNewSurgicalProcess) {
                //Usamos esta variable indicar que vamos a insertar los registros.
                createNewSurgicalProcess = false;
                methodName = "SetSurgicalProcess";
                parameterName = "dataToInsert";
                parameterName1 = "isUpdate";

                //Probar cambio, añadida descripcion al material
                SurgicalProcessInputModel surgicalProcessInputModel = new SurgicalProcessInputModel();

                surgicalProcessInputModel.HisId = surgicalProcess.HisId;
                surgicalProcessInputModel.InterventionCode = surgicalProcess.InterventionCode;
                surgicalProcessInputModel.RecordNumber = surgicalProcess.RecordNumber;
                surgicalProcessInputModel.InterventionDate = surgicalProcess.InterventionDate;
                surgicalProcessInputModel.OperationRoomId = surgicalProcess.OperationRoomId;
                surgicalProcessInputModel.HosId = userLogged.HosId;
                surgicalProcessInputModel.EntryUser = userLogged.Login;
                surgicalProcessInputModel.Urgent = surgicalProcess.Urgent;

                for (SP_MaterialOutputModel m : lstMaterial) {
                    //Serializamos los materiales.
                    SP_MaterialInputModel serializableMaterial = new SP_MaterialInputModel();
                    serializableMaterial.MaterialCode = m.Id;
                    serializableMaterial.MaterialType = m.MaterialType;
                    serializableMaterial.MaterialDescription = m.MaterialDescription;
                    serializableMaterial.PostCount = m.PostCount;
                    serializableMaterial.PreCount = m.PreCount;
                    serializableMaterial.TheoreticalCounter = m.TheoreticalCounter;
                    serializableMaterial.Remarks = m.Remarks;
                    serializableMaterial.ChemicalControl = m.ChemicalControl;
                    surgicalProcessInputModel.MaterialList.add(serializableMaterial);
                }

                //Según el código hay que usar una clase de web service o otra;
                client.configure(new Configurator(
                        ConnectionParameters.NAME_SPACE, ConnectionParameters.CONTRACT_NAME, methodName));

                client.addParameter(parameterName, surgicalProcessInputModel);
                client.addParameter(parameterName1, isUpdate);
                resultModel = new SurgicalProcessOutputModel();

            }
            else{

                methodName = "GetMaterialData";
                parameterName = "materialCode";

                SP_MaterialInputModel materialInputModel = new SP_MaterialInputModel();
                materialInputModel.MaterialCode = readCode;

                client.configure(new Configurator(
                        ConnectionParameters.NAME_SPACE, ConnectionParameters.CONTRACT_NAME, methodName));
                client.addParameter(parameterName, materialInputModel);

                resultModel = new SP_MaterialOutputModel();
            }

            try {
                //Realizamos la llamada al web service para obtener los datos
                resultModel = client.call(resultModel);
            } catch (Exception e) {
                ErrorLogWriter.writeToLogErrorFile(e.getMessage(), getApplicationContext(),activityName);
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

    private void processData(Object modelResult) {
        String modelType = modelResult.getClass().getSimpleName();
        switch(modelType){
            case "SP_MaterialOutputModel":
                addMaterialToList((SP_MaterialOutputModel)modelResult);
                break;
            case "SurgicalProcessOutputModel":
                if(((SurgicalProcessOutputModel)modelResult).Result){
                    this.result = true;
                    Toast.makeText(getBaseContext(),R.string.correct_surgical_process, Toast.LENGTH_LONG).show();
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

    private void cleanControlsViews() {
        lstMaterial.clear();
        ListViewMaterials.setAdapter(null);
        textViewElements.setText(lstMaterial.size()+ " " + getText(R.string.materials_counter));
    }

    private void addMaterialToList(SP_MaterialOutputModel modelResult) {
        try{
            SPMaterialCustomAdapter adapter = new SPMaterialCustomAdapter(this, GetData(modelResult));
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

        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));

        readCode = decodedData;

        new SurgicalProcessMyAsyncClass().execute();
    };

    private List<SP_MaterialOutputModel> GetData(SP_MaterialOutputModel material) {
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
                        new SP_MaterialOutputModel(material.Id, materialImageType,
                                material.MaterialDescription,material.MaterialType, material.ChemicalControl));
            }else {
                if(existsInList(material.Id)){
                    Toast.makeText(getBaseContext(), R.string.material_exists_list, Toast.LENGTH_LONG).show();
                }else{
                    lstMaterial.add(
                            new SP_MaterialOutputModel(material.Id, materialImageType,material.MaterialDescription,
                                    material.MaterialType, material.ChemicalControl));
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
        for(SP_MaterialOutputModel m : lstMaterial){
            if(m.getId().equals(materialId)){
                return true;
            }
        }
        return false;
    }
}