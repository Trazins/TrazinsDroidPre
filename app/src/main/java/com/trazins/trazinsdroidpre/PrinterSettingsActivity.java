package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trazins.trazinsdroidpre.utils.ErrorLogWriter;
import com.trazins.trazinsdroidpre.utils.SettingsHelper;
import com.trazins.trazinsdroidpre.utils.ThreadSleeper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

public class PrinterSettingsActivity extends AppCompatActivity {

    EditText editTextIp, editTextPortNumber;
    TextView textViewTestInfo;
    Button buttonTest;

    String activityName;
    //Impresora Zebra conectada a la red
    private ZebraPrinter printer;

    //Conexión a la impresora
    private Connection printerConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_settings);

        this.activityName= this.getClass().getSimpleName();

        editTextIp = findViewById(R.id.editTextIpAddress);
        editTextIp.setText(SettingsHelper.getIp(this));
        editTextPortNumber = findViewById(R.id.editTextPort);
        editTextPortNumber.setText(SettingsHelper.getPort(this));
        textViewTestInfo = findViewById(R.id.textViewInfoTest);
        buttonTest = findViewById(R.id.buttonTest);

        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Deshabilitar el botón de test para evitar multiples pulsaciones
                        enableButtonTest(false);
                        Looper.prepare();

                        //Iniciamos la prueba de conexión
                        doConnectionTest();

                        //Detenemos la ejecución del método
                        Looper.loop();
                        Looper.myLooper().quit();

                    }
                }).start();

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (printerConnection != null && printerConnection.isConnected()) {
            disconnect();
        }
    }

    //region Métodos principales

    //Método para conectar y enviar etiqueta de prueba
    private void doConnectionTest() {

        //Creamos la conexión y si todo va bien envíamos la etiqueta
        printer = connect();
        if(printer!= null){
            sendTestLabel();
        }else {
            disconnect();
        }
    }

    //Método que devuelve la impresora con todos los datos de configuración
    private ZebraPrinter connect() {
        setStatus(getText(R.string.connecting).toString(), Color.YELLOW);
        ThreadSleeper.sleep(1000);
        printerConnection = null;

        try {
            //Obtenemos los datos que ha registrado el usuario
            int port = Integer.parseInt(getPortNumber());
            printerConnection = new TcpConnection(getIpAddress(), port);

            //Los guardamos en el archivo de preferencias de la aplicación
            SettingsHelper.saveIp(getIpAddress(),this);
            SettingsHelper.savePort(getPortNumber(),this);
        }catch (NumberFormatException e){
            e.printStackTrace();
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            setStatus(getText(R.string.invalid_data).toString(), Color.RED );
            return null;
        }

        //Conectamos con la impresora y obtenemos la información
        try{
            printerConnection.open();
            setStatus(getText(R.string.connection_ok).toString(),Color.GREEN);
            ThreadSleeper.sleep(1000);
        }catch (ConnectionException e){
            e.printStackTrace();
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            setStatus(getText(R.string.connection_error).toString(),Color.RED);
            ThreadSleeper.sleep(1000);
            disconnect();
        }

        ZebraPrinter printer = null;
        if(printerConnection.isConnected()){
            try{
                printer = ZebraPrinterFactory.getInstance(printerConnection);
                setStatus(getText(R.string.determining_printer_language).toString(), Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                setStatus(getText(R.string.printer_language).toString() + pl, Color.BLUE);
            } catch (ConnectionException e) {
                e.printStackTrace();
                ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
                setStatus(getText(R.string.unknown_printer_language).toString(), Color.RED);
                printer = null;
                ThreadSleeper.sleep(1000);
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                e.printStackTrace();
                ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
                setStatus(getText(R.string.unknown_printer_language).toString(), Color.RED);
                printer = null;
                ThreadSleeper.sleep(1000);
                disconnect();
            }
        }
        return printer;
    }

    private void sendTestLabel() {
        try{
            byte[] configLabel = getConfigLabel();
            printerConnection.write(configLabel);
            setStatus(getText(R.string.sending_data).toString(), Color.BLUE);
            ThreadSleeper.sleep(1500);

        }catch (ConnectionException e) {
            e.printStackTrace();
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        finally{
            disconnect();
        }
    }

    private byte[] getConfigLabel() {
        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();
        byte[] configLabel = null;
        if(printerLanguage== PrinterLanguage.ZPL){
            configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ".getBytes();
        }else{
            String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            configLabel = cpclConfigLabel.getBytes();
        }

        return configLabel;
    }

    //Método para desconectar la impresora
    private void disconnect() {
        try{
            setStatus(getText(R.string.disconnecting).toString(),Color.RED);
            if(printerConnection != null){
                printerConnection.close();
            }
            ThreadSleeper.sleep(1000);
            setStatus(getText(R.string.not_connected).toString(),Color.RED);
        } catch (ConnectionException e) {
            e.printStackTrace();
            ErrorLogWriter.writeToLogErrorFile(e.getMessage(),getApplicationContext(),activityName);
            setStatus(getText(R.string.connection_error).toString(),Color.RED);
        }
        finally {
            enableButtonTest(true);
        }
    }

    //endregion

    //region Métodos de apoyo

    //Habilita y deshabilita el botón de test
    private void enableButtonTest(final boolean enabled) {
        //Como hemos creado un hilo nuevo de ejecución hay que actualizar el principal
        runOnUiThread(new Runnable() {
            @Override
            public void run() {buttonTest.setEnabled(enabled);}
        });
    }

    //Actualiza el textView con el estado del proceso del test
    private void setStatus(final String messagge, final int color) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewTestInfo.setText(messagge);
                textViewTestInfo.setBackgroundColor(color);
            }
        });
    }

    //Devuelve el puerto del editText correspondiente
    private String getPortNumber() { return editTextPortNumber.getText().toString();}

    //Devuelve la ip del editText correspondiente
    private String getIpAddress(){return  editTextIp.getText().toString();}


    //endregion
}