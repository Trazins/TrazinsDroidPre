package com.trazins.trazinsdroidpre.models.surgicalprocessmodel;

import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialOutputModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SurgicalProcessOutputModel implements Serializable {
    public Integer HisId;
    public int OperationRoomId;
    public String InterventionCode;
    public String RecordNumber;
//    public String DeliveryNote;
    public String InterventionDate;
    public String HosId;
    public String OperationRoomName;

    public boolean Urgent;
    public List<SurgicalProcessOutputModel> SurgicalProcessList = new ArrayList<SurgicalProcessOutputModel>();
    public List<SP_MaterialOutputModel> MaterialOutputModelList = new ArrayList<SP_MaterialOutputModel>();
    public boolean Result;
    public boolean RealTime;

    public SurgicalProcessOutputModel(){}

    public SurgicalProcessOutputModel(
            Integer hisId, String operationRoomName, String interventionCode, String recordNumber,
            String interventionDate, Boolean urgent ){
        this.HisId = hisId;
        this.OperationRoomName = operationRoomName;
        this.InterventionCode = interventionCode;
        this.RecordNumber = recordNumber;
        this.InterventionDate = interventionDate;
        this.Urgent = urgent;
    }
}
