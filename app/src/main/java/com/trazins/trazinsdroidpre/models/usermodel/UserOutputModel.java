package com.trazins.trazinsdroidpre.models.usermodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserOutputModel implements Serializable {
    public String SignatureCode;
    public String UserName;
    public String Login;
    public String HosId;
    public String HospitalName;
    public List<UserOutputModel> UsersList = new ArrayList<UserOutputModel>();
}
