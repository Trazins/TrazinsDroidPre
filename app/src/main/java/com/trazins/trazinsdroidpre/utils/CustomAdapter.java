package com.trazins.trazinsdroidpre.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trazins.trazinsdroidpre.R;
import com.trazins.trazinsdroidpre.models.materialmodel.MaterialOutputModel;

import java.util.List;

public class CustomAdapter extends BaseAdapter {
    Context context;
    List<MaterialOutputModel> lst;

    public CustomAdapter(Context context, List<MaterialOutputModel> lst) {
        this.context = context;
        this.lst = lst;
    }

    @Override
    public int getCount() {
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
        ImageView ImageViewContacto;
        TextView TextViewNombre;
        TextView TextViewDes;

        MaterialOutputModel c = lst.get(position);
        if(convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_materials_item, null);
        ImageViewContacto = convertView.findViewById(R.id.imageViewMaterialType);
        TextViewNombre = convertView.findViewById(R.id.textViewDescription);
        TextViewDes = convertView.findViewById(R.id.textViewId);

        ImageViewContacto.setImageResource(c.Image);
        TextViewNombre.setText(c.MaterialDescription);
        TextViewDes.setText(c.Id);

        return convertView;
    }
}
