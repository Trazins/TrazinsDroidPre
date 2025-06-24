package com.trazins.trazinsdroidpre.models.sp_intrumentmodel;

import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialOutputModel;

import java.util.ArrayList;
import java.util.List;

public class SP_InstrumentOutputModel extends SP_MaterialOutputModel {
    public String InstrumentCode;

    public String InstrumentDM;
    public String SetName;

    public List<SP_InstrumentOutputModel> SetContent = new ArrayList<SP_InstrumentOutputModel>();
}
