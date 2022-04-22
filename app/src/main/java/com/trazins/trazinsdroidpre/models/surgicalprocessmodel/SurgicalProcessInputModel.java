package com.trazins.trazinsdroidpre.models.surgicalprocessmodel;

import com.trazins.trazinsdroidpre.models.materialmodel.MaterialInputModel;
import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialInputModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SurgicalProcessInputModel implements Serializable {
    public Integer HisId;
    public int OperationRoomId;
    public String InterventionCode;
    public String RecordNumber;
    public String InterventionDate;
    public String EntryUser;
    public String OperationRoomName;
    public List<SP_MaterialInputModel> MaterialList = new ArrayList<SP_MaterialInputModel>();
}
