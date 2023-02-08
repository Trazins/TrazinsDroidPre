package com.trazins.trazinsdroidpre.models.operationroom;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class OperationRoomOutputModel {
    public int OpId;
    public String OpName;
    public String HosId;
    public List<OperationRoomOutputModel> OpList = new ArrayList<OperationRoomOutputModel>();

    @NonNull
    @Override
    public String toString() {
        return this.OpName;
    }
}
