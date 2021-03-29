package com.trazins.trazinsdroidpre;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationMenu;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    ListView ListViewContacto;
    List<Contacto> lst;
    View bottomNavigationMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        ListViewContacto = findViewById(R.id.listViewMaterials);

        CustomAdapter adapter = new CustomAdapter(this, GetData());
        ListViewContacto.setAdapter(adapter);

        bottomNavigationMenu = findViewById(R.id.bottomNavigationMenu);

        ListViewContacto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contacto c = lst.get(position);
                Toast.makeText(getBaseContext(),c.nombre,Toast.LENGTH_LONG ).show();
            }
        });
    }

    private List<Contacto> GetData() {
        lst = new ArrayList<>();

        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja2", "trauma2"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));
        lst.add(new Contacto(1,R.drawable.ic_launcher_background, "caja1", "trauma"));

        return lst;

    }
}