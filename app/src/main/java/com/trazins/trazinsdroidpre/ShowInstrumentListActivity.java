package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.trazins.trazinsdroidpre.models.sp_intrumentmodel.SP_InstrumentOutputModel;
import com.trazins.trazinsdroidpre.utils.InstrumentListCustomAdapter;

import java.util.ArrayList;
import java.util.List;

public class ShowInstrumentListActivity extends AppCompatActivity {

    private List<SP_InstrumentOutputModel> InstrumentList = new ArrayList<>();
    private ListView listViewInstruments;
    private TextView textViewToLocate;
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
    }

    private List<SP_InstrumentOutputModel> convertList(List<SP_InstrumentOutputModel> instrumentList) {
        return  instrumentList;
    }
}