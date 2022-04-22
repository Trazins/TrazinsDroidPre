package com.trazins.trazinsdroidpre.models.sp_setmodel;

import com.trazins.trazinsdroidpre.models.sp_materialmodel.SP_MaterialOutputModel;
import com.trazins.trazinsdroidpre.models.sp_intrumentmodel.SP_InstrumentOutputModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SP_SetOutputModel extends SP_MaterialOutputModel implements Serializable {

    public List<SP_InstrumentOutputModel> InstrumentList= new ArrayList<SP_InstrumentOutputModel>();

}
