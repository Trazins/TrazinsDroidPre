package com.trazins.trazinsdroidpre.utils;

public class ConnectionParameters {

    //Selector de conexión
    public static final int SET_URL_CONNECTION = 0;

    //Listado de url de coenxión para los web service
    public static final String [] SOAP_ADDRESS = new String[]{
            "http://188.165.209.37:8009/Android/TrazinsDroidService.svc",
            "http://10.50.0.170:8006/Android/TrazinsDroidService.svc"
    };

    //Datos de conexión del webservice
    public static final String NAME_SPACE = "http://tempuri.org/";
    public static final String CONTRACT_NAME = "ITrazinsDroidService";
}
