package com.trazins.trazinsdroidpre.utils;

public class ConnectionParameters {

    //Selector de conexión
    public static final int SET_URL_CONNECTION = 3;

    //Listado de url de coenxión para los web service
    public static final String [] SOAP_ADDRESS = new String[]{
            /*0*/ "http://188.165.209.37:8036/Android/TrazinsDroidService.svc", //Pruebas
            /*1*/ "http://10.50.0.170:8006/Android/TrazinsDroidService.svc",
            /*2*/ "http://188.165.209.37:8026/Android/TrazinsDroidService.svc", //Huesca
            /*3*/ "http://10.100.0.234:8030/Android/TrazinsDroidService.svc" //Salamanca
    };

    //Datos de conexión del webservice
    public static final String NAME_SPACE = "http://tempuri.org/";
    public static final String CONTRACT_NAME = "ITrazinsDroidService";
}
