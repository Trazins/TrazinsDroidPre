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

import com.trazins.trazinsdroidpre.utils.SettingsHelper;
import com.trazins.trazinsdroidpre.utils.ThreadSleeper;
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

    //Impresora Zebra conectada a la red
    private ZebraPrinter printer;

    //Conexión a la impresora
    private Connection printerConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_settings);

        editTextIp = findViewById(R.id.editTextIpAddress);
        editTextPortNumber = findViewById(R.id.editTextPort);
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

    //region Métodos principales

    //Método para conectar y enviar etiqueta de prueba
    private void doConnectionTest() {

        //Creamos la conexión y si todo va bien envíamos la etiqueta
        printer = connect();
    }

    //Método que devuelve la impresora con todos los datos de configuración
    private ZebraPrinter connect() {
        setStatus("Connecting...", Color.YELLOW);
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
            setStatus(getText(R.string.invalid_data).toString(), Color.RED );
            return null;
        }

        //Conectamos con la impresora y obtenemos la información
        try{
            printerConnection.open();
            setStatus(getText(R.string.connection_ok).toString(),Color.GREEN);
        }catch (ConnectionException e){
            e.printStackTrace();
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
                setStatus(getText(R.string.unknown_printer_language).toString(), Color.RED);
                printer = null;
                ThreadSleeper.sleep(1000);
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                e.printStackTrace();
                setStatus(getText(R.string.unknown_printer_language).toString(), Color.RED);
                printer = null;
                ThreadSleeper.sleep(1000);
                disconnect();
            }
        }
        return printer;
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

    //Método para desconectar la impresora
    private void disconnect() {
        try{
            setStatus(getText(R.string.disconnecting).toString(),Color.RED);
            if(printerConnection != null){
                printerConnection.close();
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
            setStatus(getText(R.string.connection_error).toString(),Color.RED);
        }
        finally {
            enableButtonTest(true);
        }
    }
    //endregion
}