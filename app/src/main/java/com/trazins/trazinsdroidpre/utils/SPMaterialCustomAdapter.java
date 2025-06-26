package com.trazins.trazinsdroidpre.utils;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.trazins.trazinsdroidpre.R;
import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialOutputModel;

import java.util.List;

public class SPMaterialCustomAdapter extends BaseAdapter {

    Context context;
    List<SP_MaterialOutputModel> lst;

    public SPMaterialCustomAdapter(Context context, List<SP_MaterialOutputModel> lst) {
        this.context = context;
        this.lst = lst;
    }

    @Override
    public int getCount()  {
        return lst.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageViewMaterial;
        TextView textViewName;
        TextView textViewDes;
        TextView textViewChemicalControl;
        Switch switchChemicalControl;

        SP_MaterialOutputModel c = lst.get(position);
        String deviceModel = Build.MODEL;

        if (convertView == null) {
            if (deviceModel.equals("TC20")) {
                convertView = LayoutInflater.from(context).inflate(R.layout.listview_sp_materials_item_tc20, parent, false);
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.listview_sp_materials_item, parent, false);
            }
        }

        imageViewMaterial = convertView.findViewById(R.id.imageViewMaterialType);
        textViewName = convertView.findViewById(R.id.textViewDescription);
        textViewDes = convertView.findViewById(R.id.textViewId);

        imageViewMaterial.setImageResource(c.Image);
        textViewName.setText(c.MaterialDescription);
        textViewDes.setText(c.Id);

        if (deviceModel.equals("TC20")) {
            textViewChemicalControl = convertView.findViewById(R.id.textViewLstChemicalControl);
            if (textViewChemicalControl != null) {
                textViewChemicalControl.setText(c.ChemicalControl ? R.string.urgent_yes : R.string.no);
            }
        } /*else {
            /*switchChemicalControl = convertView.findViewById(R.id.switchMaterialListItem);
            if (switchChemicalControl != null) {
                switchChemicalControl.setOnCheckedChangeListener(null); // Evita callback al reciclar la vista
                switchChemicalControl.setChecked(c.ChemicalControl);
                switchChemicalControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    c.ChemicalControl = isChecked;
                });
            }
        }*/

        return convertView;
    }
}