package com.trazins.trazinsdroidpre.models.sp_intrumentmodel;

import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialOutputModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SP_InstrumentOutputModel extends SP_MaterialOutputModel implements Serializable {
    public String InstrumentCode;

    public String InstrumentId;
    public String InstrumentDM;
    public String SetName;

    public boolean Result;

    public List<SP_InstrumentOutputModel> SetContent = new ArrayList<SP_InstrumentOutputModel>();
}
