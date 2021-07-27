package com.trazins.trazinsdroidpre.models.operationroom;

import androidx.annotation.NonNull;

public class OperationRoomOutputModel {
    public int OpId;
    public String OpName;

    @NonNull
    @Override
    public String toString() {
        return this.OpName;
    }
}
