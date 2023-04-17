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
        ImageView ImageViewMaterial;
        TextView TextViewName;
        TextView TextViewDes;
        TextView TextViewChemicalControl;
        Switch SwitchChemicalControl;

        SP_MaterialOutputModel c = lst.get(position);

        String deviceModel = Build.MODEL;

        if(convertView == null)
            if(deviceModel.equals("TC20")){
                convertView = LayoutInflater.from(context).inflate(R.layout.listview_sp_materials_item_tc20, null);
                TextViewChemicalControl = convertView.findViewById(R.id.textViewLstChemicalControl);
                if(c.ChemicalControl)
                    TextViewChemicalControl.setText(R.string.urgent_yes);
                else
                    TextViewChemicalControl.setText(R.string.no);
            }else{
                convertView = LayoutInflater.from(context).inflate(R.layout.listview_sp_materials_item, null);
                SwitchChemicalControl = convertView.findViewById(R.id.switchMaterialListItem);
                SwitchChemicalControl.setChecked(c.ChemicalControl);
            }

        ImageViewMaterial = convertView.findViewById(R.id.imageViewMaterialType);
        TextViewName = convertView.findViewById(R.id.textViewDescription);
        TextViewDes = convertView.findViewById(R.id.textViewId);

        ImageViewMaterial.setImageResource(c.Image);
        TextViewName.setText(c.MaterialDescription);
        TextViewDes.setText(c.Id);

        return convertView;

    }
}