package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShowErrorLogActivity extends AppCompatActivity {

    EditText editTextUser, editTextPass;
    Button buttonLogin, buttonShowLog, buttonExit;
    TextView textViewErrorLog;

    private static final String ERROR_LOG_FILE = "ErrorLog.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_error_log);

        editTextUser = findViewById(R.id.editTextInsertUser);
        editTextPass = findViewById(R.id.editTextInsertPass);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonShowLog = findViewById(R.id.buttonShowErrorLog);
        buttonExit = findViewById(R.id.buttonExit);
        textViewErrorLog = findViewById(R.id.textViewLog);
        textViewErrorLog.setMovementMethod(new ScrollingMovementMethod());

        editTextPass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction()==KeyEvent.ACTION_DOWN && keyCode== KeyEvent.KEYCODE_ENTER){
                    activateLogButtons();
                    return true;
                }
                return false;
            }
        });

        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateLogButtons();
            }
        });

        buttonShowLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getErrorLogFromFile();
            }
        });
    }

    private void getErrorLogFromFile() {
        try{

            FileInputStream fistr = getApplicationContext().openFileInput(ERROR_LOG_FILE);

            InputStreamReader streamReader = new InputStreamReader(fistr, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader bfreader = new BufferedReader(streamReader)){
                String line = bfreader.readLine();
                while(line!= null){
                    stringBuilder.append(line).append('\n');
                    line = bfreader.readLine();
                }
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            } finally {
                String contents = stringBuilder.toString();
                textViewErrorLog.setText(contents);
            }

        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void activateLogButtons() {

        if(editTextUser.getText().toString().equals("admintr") &&
                editTextPass.getText().toString().equals("tradmin")){

            buttonShowLog.setEnabled(true);
        }else {
            Toast.makeText(getApplicationContext(),R.string.incorrect_user, Toast.LENGTH_LONG).show();
        }
    }
}