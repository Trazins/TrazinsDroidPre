package com.trazins.trazinsdroidpre.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.CopyOnWriteArrayList;

//Classe que gestiona la información guardada en el archivo de preferencias de la aplicación
public class SettingsHelper {

    //Nombre del archivo donde se almacenará la información
    private static final String PREFS_NAME = "TrazinsDroidPreferences";

    //Identificadores de los valores de ip y puerto
    private static final String tcpIpAddressKey = "PRINTER_IP_ADDRESS";
    private static final String tcpPortNumberKey = "PRINTER_PORT_NUMBER";

    //Método que devuelve la ip desde el archivo de preferencias
    public static String getIp(Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);
        return settings.getString(tcpIpAddressKey,"");
    }

    //Método que devuelve el puerto de conexión desde el archivo de preferencias
    public static String getPort(Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);
        return  settings.getString(tcpPortNumberKey,"");
    }

    //Método para guardar la ip en el archivo de preferencias
    public static void saveIp(String ipAddress, Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(tcpIpAddressKey,ipAddress);
        editor.commit();
    }

    //Método para guardar el puerto en el archivo de preferencias
    public static void savePort(String port, Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(tcpPortNumberKey,port);
        editor.commit();
    }

}
