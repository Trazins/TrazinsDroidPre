package com.trazins.trazinsdroidpre.models.materialmodel;

import java.io.Serializable;
import java.util.List;

/**
 * Contenedor para deserializar una lista de MaterialOutputModel.
 */
public class MaterialOutputListModel implements Serializable {

    private List<MaterialOutputModel> MaterialList;

    public MaterialOutputListModel() {
    }

    public MaterialOutputListModel(List<MaterialOutputModel> materialList) {
        this.MaterialList = materialList;
    }

    public List<MaterialOutputModel> getMaterialList() {
        return MaterialList;
    }

    public void setMaterialList(List<MaterialOutputModel> materialList) {
        this.MaterialList = materialList;
    }
}
