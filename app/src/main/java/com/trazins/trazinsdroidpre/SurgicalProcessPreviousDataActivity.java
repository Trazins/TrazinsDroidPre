package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.threepin.fireexit_wcf.Configurator;
import com.threepin.fireexit_wcf.FireExitClient;
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

    }
    class OperationRoomAsyncClass extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            String methodName;
            String parameterName;

            Object resultModel = null;

            FireExitClient client = new FireExitClient(
                    "http://188.165.209.37:8009/Android/TrazinsDroidService.svc");
            methodName = "GetOperationRoomList";
            parameterName = "user";

            client.configure(new Configurator(
                    "http://tempuri.org/","ITrazinsDroidService", methodName));
            resultModel = new OperationRoomOutputModel();
            try {
                resultModel= client.call(resultModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}