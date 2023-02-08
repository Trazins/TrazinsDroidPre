package com.trazins.trazinsdroidpre.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.trazins.trazinsdroidpre.R;
import com.trazins.trazinsdroidpre.models.usermodel.UserOutputModel;

import java.util.List;

public class HospitalCustomAdapter extends BaseAdapter {

    Context context;
    List<UserOutputModel> lst;

    public HospitalCustomAdapter(Context context, List<UserOutputModel> lst){
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
        TextView TextViewHospitalName;

        UserOutputModel u = lst.get(position);

        if(convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_hospitals_items, null);

        TextViewHospitalName = convertView.findViewById(R.id.textViewHosName);

        TextViewHospitalName.setText(u.HospitalName);

        return convertView;
    }


}
