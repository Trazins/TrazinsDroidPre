package com.trazins.trazinsdroidpre.surgicalprocessactivities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.trazins.trazinsdroidpre.R;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;
import com.trazins.trazinsdroidpre.utils.ErrorLogWriter;
import com.zebra.rfid.api3.TagData;

public class MaterialPostCounterActivity extends AppCompatActivity  implements RFIDHandler.ResponseHandlerInterface{

    public TextView statusTextViewRFID = null;
    private TextView textrfid;
    //private TextView testStatus;
    String activityName;

    public UserOutputModel userLogged;

    RFIDHandler rfidHandler;
    //final static String TAG = "RFID_SAMPLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_post_counter);
        //Usuario loggeado
        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        activityName = this.getClass().getSimpleName();

        statusTextViewRFID = findViewById(R.id.textViewRfidStatus);

        // RFID Handler
        rfidHandler = new RFIDHandler();
        try{
            rfidHandler.onCreate(this);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(), activityName);
        }

        //Recuperar la información de la caja
    }



    @Override
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

    @Override
    public void handleTagdata(TagData[] tagData) {
        final StringBuilder sb = new StringBuilder();
        for (int index = 0; index < tagData.length; index++) {
            sb.append(tagData[index].getTagID() + "\n");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textrfid.append(sb.toString());
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textrfid.setText("");
                }
            });
            rfidHandler.performInventory();
        } else
            rfidHandler.stopInventory();
    }
}