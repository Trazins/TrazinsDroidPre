package com.trazins.trazinsdroidpre.models.materialmodel;

public class MaterialOutputModel {

    public String Id;
    public String MaterialDescription;
    public String MaterialType;
    public String HosId;
    public String ChId;
    public int Image;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getMaterialDescription() {
        return MaterialDescription;
    }

    public void setMaterialDescription(String materialDescription) {
        this.MaterialDescription = materialDescription;
    }

    public String getMaterialType() {
        return MaterialType;
    }

    public void setMaterialType(String materialType) {
        this.MaterialType = materialType;
    }

    public String getHosId() {
        return HosId;
    }

    public void setHosId(String hosId) {
        this.HosId = hosId;
    }

    public String getChId() {
        return ChId;
    }

    public void setChId(String chId) {
        this.ChId = chId;
    }

    public int getImage() {
        return Image;
    }

    public void setImage(int image) {
        this.Image = image;
    }

    public MaterialOutputModel(String id, int image, String materialDescription, String materialType) {
        this.Id = id;
        this.MaterialDescription = materialDescription;
        this.Image = image;
        this.MaterialType = materialType;
    }

    public MaterialOutputModel() {
    }

    public MaterialOutputModel(String id, String materialDescription, String materialType, String hosId, String chId) {
        this.Id = id;
        this.MaterialDescription = materialDescription;
        this.MaterialType = materialType;
        this.HosId = hosId;
        this.ChId = chId;
    }


}
