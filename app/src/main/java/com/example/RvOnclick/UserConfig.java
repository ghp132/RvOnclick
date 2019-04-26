package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class UserConfig {
    @PrimaryKey
    @NonNull
    private String userId; //email id
    private String fullName;
    private int SendSms, allowRateChange;
    private String employeeId;
    private String password;
    private String loginUrl, salesPerson;

    public String getSalesPerson() {
        return salesPerson;
    }

    public void setSalesPerson(String salesPerson) {
        this.salesPerson = salesPerson;
    }

    public int getAllowRateChange() {
        return allowRateChange;
    }

    public void setAllowRateChange(int allowRateChange) {
        this.allowRateChange = allowRateChange;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }



    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getSendSms() {
        return SendSms;
    }

    public void setSendSms(int sendSms) {
        SendSms = sendSms;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}
