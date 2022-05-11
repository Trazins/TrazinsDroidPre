package com.trazins.trazinsdroidpre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trazins.trazinsdroidpre.models.sp_intrumentmodel.SP_InstrumentOutputModel;
import com.trazins.trazinsdroidpre.utils.InstrumentListCustomAdapter;

import java.util.ArrayList;
import java.util.List;

public class ShowInstrumentListActivity extends AppCompatActivity {

    private List<SP_InstrumentOutputModel> InstrumentList = new ArrayList<>();
    private ListView listViewInstruments;
    private TextView textViewToLocate;
    private BottomNavigationView bnv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_instrument_list);
        this.InstrumentList = (List<SP_InstrumentOutputModel>)getIntent().getSerializableExtra("instrumentList");
        listViewInstruments = findViewById(R.id.listViewInstrumentList);
        InstrumentListCustomAdapter adapter = new InstrumentListCustomAdapter(this,
                convertList(InstrumentList));
        listViewInstruments.setAdapter(adapter);
        textViewToLocate = findViewById(R.id.textViewToLocateTitle);
        textViewToLocate.setText(getResources().getText(R.string.to_locate_title ) + " " + InstrumentList.size());
        bnv = findViewById(R.id.bottomInstrumentListActivity);

        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                finish();
                return false;
            }
        });
    }

    private List<SP_InstrumentOutputModel> convertList(List<SP_InstrumentOutputModel> instrumentList) {
        return  instrumentList;
    }
}