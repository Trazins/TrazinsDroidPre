package com.trazins.trazinsdroidpre.models.trolleymodel;

import com.trazins.trazinsdroidpre.models.materialmodel.MaterialOutputModel;

import java.util.ArrayList;
import java.util.List;

public class TrolleyOutputModel extends MaterialOutputModel {
    public String TrolleyCode;
    public int LocateId;
    public List<TrolleyOutputModel> TrolleyContent = new ArrayList<TrolleyOutputModel>();
}
