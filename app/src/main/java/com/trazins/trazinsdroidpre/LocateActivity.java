package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.threepin.fireexit_wcf.Configurator;
import com.threepin.fireexit_wcf.FireExitClient;
import com.trazins.trazinsdroidpre.models.usermodel.UserInputModel;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.scanner.DataWedgeInterface;
import com.trazins.trazinsdroidpre.scanner.MyReceiver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class LocateActivity extends AppCompatActivity {

    ListView ListViewContacto;
    List<Contacto> lst;
    View bottomNavigationMenu;
    TextView textViewResult;

    MyReceiver Receiver = new MyReceiver();
    IntentFilter filter = new IntentFilter();

    Handler handler;
    String readCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);

        handler = new Handler(Looper.getMainLooper());

        ListViewContacto = findViewById(R.id.listViewMaterials);
        textViewResult = findViewById(R.id.textViewLocationResult);

        filter.addAction(DataWedgeInterface.ACTION_RESULT_DATAWEDGE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(DataWedgeInterface.ACTIVITY_INTENT_FILTER_ACTION);

        CustomAdapter adapter = new CustomAdapter(this, GetData());
        ListViewContacto.setAdapter(adapter);

        bottomNavigationMenu = findViewById(R.id.bottomNavigationMenu);

        ListViewContacto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contacto c = lst.get(position);
                Toast.makeText(getBaseContext(),c.nombre,Toast.LENGTH_LONG ).show();
            }
        });
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
        TextView txtActiveProfile = findViewById(R.id.textViewAutResult);
        //txtActiveProfile.setText(event.activeProfile);
    };

    //Clase que gestiona la conexión con el web service
    class LocateMyAsyncClass extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            //Usamos los mismos nombres en las clases para que la serialización se realice correctamente
            UserInputModel userInputModelData = new UserInputModel();
            userInputModelData.SignatureCode = readCode;

            //Desplegar el servicio:
            //Usamos la librería Fireexit para la gestión de la serialización.
            //Pendiente crear identificación lectura según material
            FireExitClient client = new FireExitClient(
                    "http://188.165.209.37:8009/Android/TrazinsDroidService.svc");
            client.configure(new Configurator(
                    "http://tempuri.org/", "ITrazinsDroidService", "GetUser"));

            client.addParameter("signature", userInputModelData);

            UserOutputModel userOutputModelLogged = new UserOutputModel();

            try {
                //Realizamos la llamada al web service para obtener los datos
                userOutputModelLogged = client.call(userOutputModelLogged);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return userOutputModelLogged;
        }

        @Override
        protected void onPostExecute(Object userLogged) {
            super.onPostExecute(userLogged);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setInformationMessage((UserOutputModel) userLogged);
                }
            });
        }

        private void setInformationMessage(UserOutputModel userLogged) {
            if(userLogged!=null){
                textViewResult.setText(((UserOutputModel) userLogged).UserName);

                /*Intent switchActivity = new Intent(getApplicationContext(), SelectionActivity.class);
                switchActivity.putExtra("userName",((UserOutputModel) userLogged).UserName);
                startActivity(switchActivity);*/
            }
            else{
                textViewResult.setText(R.string.Incorrect_user);
            }
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
        new LocateMyAsyncClass().execute();
    };


    private List<Contacto> GetData() {
        lst = new ArrayList<>();

        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja2", "trauma2"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));

        return lst;

    }
}