package com.trazins.trazinsdroidpre.models.trolleymodel;

import android.content.Intent;

import com.trazins.trazinsdroidpre.models.materialmodel.MaterialOutputModel;

import java.util.ArrayList;
import java.util.List;

public class TrolleyOutputModel extends MaterialOutputModel {
    public String TrolleyCode;
    public Integer LocateId;
    public String TrolleyName;
    public String HosId;
    public List<TrolleyOutputModel> TrolleyContent = new ArrayList<TrolleyOutputModel>();
}
