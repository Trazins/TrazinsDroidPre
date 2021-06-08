package com.trazins.trazinsdroidpre.models.storagemodel;

import com.trazins.trazinsdroidpre.models.materialmodel.MaterialOutputModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StorageInputModel implements Serializable {
    public String LocationId;
    public List<MaterialOutputModel> MatList = new ArrayList<MaterialOutputModel>();
    public String EntryUser;
}
