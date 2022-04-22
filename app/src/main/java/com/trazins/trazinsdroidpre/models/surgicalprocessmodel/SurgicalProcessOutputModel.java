package com.trazins.trazinsdroidpre.models.surgicalprocessmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SurgicalProcessOutputModel implements Serializable {
    public Integer HisId;
    public int OperationRoomId;
    public String InterventionCode;
    public String RecordNumber;
    public String InterventionDate;
    public String EntryUser;
    public String OperationRoomName;
    public List<SurgicalProcessOutputModel> SurgicalProcessList = new ArrayList<SurgicalProcessOutputModel>();
    public boolean Result;

    public SurgicalProcessOutputModel(){}

    public SurgicalProcessOutputModel(
            String operationRoomName, String interventionCode, String recordNumber, String interventionDate ){
        this.OperationRoomName = operationRoomName;
        this.InterventionCode = interventionCode;
        this.RecordNumber = recordNumber;
        this.InterventionDate = interventionDate;
    }
}
