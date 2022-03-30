package com.trazins.trazinsdroidpre.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ErrorLogWriter {
    public static void writeToLogErrorFile(String message, Context context){

        try{
            File path = context.getApplicationContext().getFilesDir();
            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            String errorMessage = String.format(
                    "Date: " + currentDate + " Error: " + message);

            FileOutputStream writer = new FileOutputStream(new File(path, "ErrorLog.txt"));
            writer.write(errorMessage.getBytes());
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
