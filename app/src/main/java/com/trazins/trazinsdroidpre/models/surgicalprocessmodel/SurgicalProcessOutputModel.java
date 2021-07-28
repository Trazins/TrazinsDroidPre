package com.trazins.trazinsdroidpre.models.surgicalprocessmodel;

import android.text.Editable;

import java.io.Serializable;
import java.util.Date;

public class SurgicalProcessOutputModel implements Serializable {
    public int HisId;
    public int OperationRoomId;
    public String InterventionCode;
    public String RecordNumber;
    public String InterventionDate;
    public String EntryUser;
    public boolean Result;
}
