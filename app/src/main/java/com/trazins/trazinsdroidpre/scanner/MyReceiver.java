package com.trazins.trazinsdroidpre.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.trazins.trazinsdroidpre.MainActivity;
import com.trazins.trazinsdroidpre.R;

import org.greenrobot.eventbus.EventBus;

public class MyReceiver extends BroadcastReceiver {

    //Pendiente optimizar código
    public String decodedData;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(DataWedgeInterface.ACTION_RESULT_DATAWEDGE))
        {
            if (intent.hasExtra(DataWedgeInterface.EXTRA_RESULT_GET_ACTIVE_PROFILE))
            {
                //  6.2 API to GetActiveProfile
                String activeProfile = intent.getStringExtra(DataWedgeInterface.EXTRA_RESULT_GET_ACTIVE_PROFILE);
                //Esta clase se añade en el archivo gradle en la sección implemnetation.
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

    private void displayScanResult(Intent initiatingIntent, String via_broadcast) {
        decodedData = initiatingIntent.getStringExtra(DataWedgeInterface.DATAWEDGE_INTENT_KEY_DATA);
        /*MainActivity.textViewAutResult.setText(decodedData);
        MainActivity.readCode= decodedData;*/

    }


}
