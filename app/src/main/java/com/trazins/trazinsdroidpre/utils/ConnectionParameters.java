package com.trazins.trazinsdroidpre.utils;

public class ConnectionParameters {

    //Selector de conexión
    public static final int SET_URL_CONNECTION = 0;

    //Listado de url de conexión para los web service
    public static final String [] SOAP_ADDRESS = new String[]{
            /*0*/ "http://188.165.209.37:8036/Android/TrazinsDroidService.svc",     //Desarrollo
            /*1*/ "http://141.94.195.33:8036/Android/TrazinsDroidService.svc",      //Pruebas
            /*2*/ "http://10.50.0.170:8006/Android/TrazinsDroidService.svc",        //Lucena
            /*3*/ "http://141.94.195.33:8026/Android/TrazinsDroidService.svc",      //Huesca
            /*4*/ "http://10.100.0.234:8030/Android/TrazinsDroidService.svc",       //Salamanca
            /*5*/ "http://DC0GDESIIS003:8000/Android/TrazinsDroidService.svc",      //Navarra DES
            /*6*/ "http://DC0GPREIIS003:8051/Android/TrazinsDroidService.svc",      //Navarra PRE
            /*7*/ "http://DC0GPROIIS003:8051/Android/TrazinsDroidService.svc",      //Navarra PRO
            /*8*/ "http://141.94.195.33:8066/Android/TrazinsDroidService.svc",      //Teruel
            /*9*/ "http://10.208.8.26:8040/Android/TrazinsDroidService.svc",        //Chihuahua
            /*10*/ "http://10.14.0.61:8020/Android/TrazinsDroidService.svc",        //Tauli
            /*11*/ "http://141.94.195.33:8079/Android/TrazinsDroidService.svc",     //Garraf
            /*12*/ "http://10.84.76.132:8197/Android/TrazinsDroidService.svc",      //Bellvitge
            /*13*/ "http://141.94.195.33:8081/Android/TrazinsDroidService.svc",     //Sector1
            /*14*/ "http://10.33.74.210:57711/TrazinsDroidService.svc",              //Local
    };

    //Datos de conexión del webservice
    public static final String NAME_SPACE = "http://tempuri.org/";
    public static final String CONTRACT_NAME = "ITrazinsDroidService";
}
