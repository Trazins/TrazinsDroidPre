package com.trazins.trazinsdroidpre.utils;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.trazins.trazinsdroidpre.R;
import com.trazins.trazinsdroidpre.models.surgicalprocessmodel.SurgicalProcessOutputModel;

import java.text.SimpleDateFormat;
import java.util.List;

public class SPCustomAdapter extends BaseAdapter {

    Context context;
    List<SurgicalProcessOutputModel> lst;

    public SPCustomAdapter(Context context, List<SurgicalProcessOutputModel> lst) {
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
        TextView TextViewInterventionCode;
        TextView TextViewRecordNum;
        TextView TextViewOperatingRoom;
        TextView TextViewInterventionDate;
        TextView TextViewUrgent;
        Switch SwitchUrgent;

        SurgicalProcessOutputModel c = lst.get(position);

        String deviceModel = Build.MODEL;

        //Según el tipo de dispositivo usar un listviewitem u otro
        if(convertView == null)
            if(deviceModel.equals("TC20")){
                convertView = LayoutInflater.from(context).inflate(R.layout.listview_surgical_process_item_tc20, null);
                TextViewUrgent = convertView.findViewById(R.id.textViewLstUrgent);
                if(c.Urgent)
                    TextViewUrgent.setText(R.string.urgent_yes);
                else
                    TextViewUrgent.setText(R.string.no);
            }else{
                convertView = LayoutInflater.from(context).inflate(R.layout.listview_surgical_process_item, null);
                SwitchUrgent = convertView.findViewById(R.id.switchUrgent);
                SwitchUrgent.setChecked(c.Urgent);
            }

        TextViewInterventionCode = convertView.findViewById(R.id.textViewLstInterventionCode);
        TextViewRecordNum = convertView.findViewById(R.id.textViewLstRecordNum);
        TextViewOperatingRoom = convertView.findViewById(R.id.textViewLstOperatingRoom);
        TextViewInterventionDate = convertView.findViewById(R.id.textViewLstInterventionDate);

        TextViewInterventionCode.setText(c.InterventionCode);
        TextViewRecordNum.setText(c.RecordNumber);
        TextViewOperatingRoom.setText(c.OperationRoomName);
        TextViewInterventionDate.setText(c.InterventionDate);

        return convertView;
    }
}
