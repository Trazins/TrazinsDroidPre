package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;

public class SelectionActivity extends AppCompatActivity {

    private TextView txtUserName;
    private Button buttonLocate;
    private UserOutputModel userLogged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        txtUserName = (TextView)findViewById(R.id.textViewSelectionActivityUserName);
        buttonLocate = (Button)findViewById(R.id.buttonLocateMenu);

        //String userName = getIntent().getStringExtra("userName");
        this.userLogged = (UserOutputModel)getIntent().getSerializableExtra("userLogged");
        txtUserName.setText(getString(R.string.identified_user) + " " + userLogged.UserName);
    }

    public void openSelectedActivity(View view){
        Intent i = new Intent(getApplicationContext(),LocateActivity.class);
        i.putExtra("userLogged",this.userLogged);
        startActivity(i);
    }
}