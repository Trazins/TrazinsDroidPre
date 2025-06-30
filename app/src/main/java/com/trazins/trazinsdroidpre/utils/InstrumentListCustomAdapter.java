package com.trazins.trazinsdroidpre.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trazins.trazinsdroidpre.R;
import com.trazins.trazinsdroidpre.models.sp_intrumentmodel.SP_InstrumentOutputModel;

import java.util.List;

public class InstrumentListCustomAdapter extends BaseAdapter {
    Context context;
    List<SP_InstrumentOutputModel> lst;

    public InstrumentListCustomAdapter(Context context, List<SP_InstrumentOutputModel> lst) {
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

        TextView TextViewDescription;
        TextView TextViewId;
        TextView TextViewDM;

        SP_InstrumentOutputModel c = lst.get(position);
        if(convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_instruments_items, null);

        TextViewDescription = convertView.findViewById(R.id.textViewInstrumentDescription);
        TextViewId = convertView.findViewById(R.id.textViewInstrumentId);
        TextViewDM = convertView.findViewById(R.id.textViewInstrumentDM);

        TextViewDescription.setText(c.MaterialDescription);
        TextViewId.setText(c.InstrumentCode);
        TextViewDM.setText(c.InstrumentDM);

        return convertView;
    }
}
