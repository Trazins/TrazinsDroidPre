package com.trazins.trazinsdroidpre.models.shipmentmodel;

import com.trazins.trazinsdroidpre.models.materialmodel.MaterialInputModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShipmentInputModel implements Serializable {
    public int OriginId;
    public String HosId;
    public List<MaterialInputModel> MatList = new ArrayList<MaterialInputModel>();
    public boolean ToCentral = false;
    public boolean Urgent = false;
    public String TrolleyCode;
    public String EntryUser;
}
