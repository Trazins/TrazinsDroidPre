package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.threepin.fireexit_wcf.Configurator;
import com.threepin.fireexit_wcf.FireExitClient;

public class MainActivity extends AppCompatActivity {

    TextView textViewAutResult;
    Button buttonAction;
    Handler handler;
    //Variable que almacena el código leído por el lector.
    String readCode = "";
    //Variable que nos indica el resultado de la autenticación.
    Boolean result= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler(Looper.getMainLooper());
        textViewAutResult = findViewById(R.id.textViewAutResult);
        buttonAction = findViewById(R.id.buttonAction);
        buttonAction.setOnClickListener(view -> {
            new MyAsyncClass().execute();
            if(result){
                Intent switchActivity = new Intent(getApplicationContext(), SecondActivity.class);
                startActivity(switchActivity);
            }

        });
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
                textViewAutResult.setText(((UserOutputModel) userLogged).UserName);
                result = true;
            }
            else{
                textViewAutResult.setText(R.string.Incorrect_user);
            }
        }
    }


}