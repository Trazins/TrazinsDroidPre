package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;

import java.util.ArrayList;
import java.util.List;

public class HospitalSelectionActivity extends AppCompatActivity {

    ListView ListViewHospitals;
    List<UserOutputModel> lstHosUser = new ArrayList<UserOutputModel>();
    UserOutputModel selectedhospitalUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_selection);

        ListViewHospitals = findViewById(R.id.listViewHospitals);
        ListViewHospitals.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedhospitalUser = lstHosUser.get(position);

                ListViewHospitals.setSelector(R.color.selection);
                ListViewHospitals.requestLayout();
            }
        });

    }
}