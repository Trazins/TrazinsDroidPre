package com.trazins.trazinsdroidpre.models.surgicalprocessmodel;

import com.trazins.trazinsdroidpre.models.materialmodel.MaterialInputModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SurgicalProcessInputModel implements Serializable {
    public int HisId;
    public int OperationRoomId;
    public String InterventionCode;
    public String RecordNumber;
    public String InterventionDate;
    public String EntryUser;
    public String OperationRoomName;
    public List<MaterialInputModel> MaterialList = new ArrayList<MaterialInputModel>();
}
