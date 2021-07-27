package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.threepin.fireexit_wcf.Configurator;
import com.threepin.fireexit_wcf.FireExitClient;
import com.trazins.trazinsdroidpre.models.operationroom.OperationRoomInputModel;
import com.trazins.trazinsdroidpre.models.operationroom.OperationRoomOutputModel;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;

import java.nio.channels.AsynchronousChannelGroup;
import java.util.ArrayList;
import java.util.List;

public class SurgicalProcessPreviousDataActivity extends AppCompatActivity {

    private EditText editTextRecordNumber, editTextInterventionCode;
    private TextView textViewUserName;
    private Spinner spinnerOperationRoom;
    List<OperationRoomOutputModel> OperationRoomList;
    //Usuario logeado
    UserOutputModel userLogged;

    Handler handler;
    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgical_process_precious_data);

        editTextInterventionCode = findViewById(R.id.editTextInterventionCode);
        editTextRecordNumber = findViewById(R.id.editTextRecordNumber);
        spinnerOperationRoom = findViewById(R.id.spinnerOperationRoom);

        //Usuario loggeado
        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        textViewUserName = findViewById(R.id.textViewSPUserName);
        textViewUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);

        OperationRoomList = new ArrayList<OperationRoomOutputModel>();

        new OperationRoomAsyncClass().execute();

        spinnerOperationRoom.setAdapter(adapter);

    }

    class OperationRoomAsyncClass extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            String methodName;
            String parameterName;

            Object resultModel = null;

            OperationRoomInputModel operationRoomInputModel = new OperationRoomInputModel();
            operationRoomInputModel.UserLogged = userLogged.Login;

            FireExitClient client = new FireExitClient(
                    "http://188.165.209.37:8009/Android/TrazinsDroidService.svc");
            methodName = "GetOperationRoomList";
            parameterName = "userlogged";

            client.configure(new Configurator(
                    "http://tempuri.org/","ITrazinsDroidService", methodName));
            client.addParameter(parameterName,operationRoomInputModel );
            resultModel = new OperationRoomOutputModel();
            try {
                resultModel= (List<OperationRoomOutputModel>)client.call(resultModel);
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
                        Toast.makeText(getBaseContext(),R.string.error_operating_room,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    private void processData(Object modelResult){
        //Si no es nulo cargamos el arrayadapter
        try {
            OperationRoomList = (List<OperationRoomOutputModel>) modelResult;
            adapter = new ArrayAdapter(
                            this, R.layout.support_simple_spinner_dropdown_item, OperationRoomList);


        }catch(Exception e){
            Toast.makeText(getBaseContext(), e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
}