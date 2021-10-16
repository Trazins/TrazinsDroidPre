package com.trazins.trazinsdroidpre.utils;

//Clase que detiene el hilo para refrescar la pantalla
public class ThreadSleeper {
    private  ThreadSleeper(){
    }

    public static void sleep(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}