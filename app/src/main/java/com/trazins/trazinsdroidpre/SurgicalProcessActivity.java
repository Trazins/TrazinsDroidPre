package com.trazins.trazinsdroidpre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trazins.trazinsdroidpre.models.materialmodel.MaterialOutputModel;

import java.util.ArrayList;
import java.util.List;

public class SurgicalProcessActivity extends AppCompatActivity {

    MaterialOutputModel materialSelected = new MaterialOutputModel();
    List<MaterialOutputModel> lstmaterials = new ArrayList<MaterialOutputModel>();

    ListView ListViewMaterials;
    BottomNavigationView bnv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surgical_process);

        ListViewMaterials = findViewById(R.id.listViewPCSMaterials);

        bnv = findViewById(R.id.bottomPostCounterNavigationMenu);
        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.add_post_counter){
                    //añadir registro en bd
                }else if(item.getItemId()==R.id.start_counter){
                    //Abrir nueva activity
                }else{
                    removeSelectedSet();
                }
                return false;
            }
        });
    }

    private void removeSelectedSet(){
        CustomAdapter adapter = new CustomAdapter(this, removeData(materialSelected));
        ListViewMaterials.setAdapter(adapter);
    }

    private List<MaterialOutputModel> removeData(MaterialOutputModel material){
        try{
            lstmaterials.remove(material);
            return lstmaterials;
        }catch(Exception e){
            Toast.makeText(getBaseContext(), e.getMessage(),Toast.LENGTH_LONG).show();
            return null;
        }
    }
}