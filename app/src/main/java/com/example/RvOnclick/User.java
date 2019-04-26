package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class User {
    @PrimaryKey
    @NonNull
    private String emailId;
    private String fullName;
    private int sendSms;
    private int canEditRate;
    private String salesPerson;

    @NonNull
    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(@NonNull String emailId) {
        this.emailId = emailId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getSendSms() {
        return sendSms;
    }

    public void setSendSms(int sendSms) {
        this.sendSms = sendSms;
    }

    public int getCanEditRate() {
        return canEditRate;
    }

    public void setCanEditRate(int canEditRate) {
        this.canEditRate = canEditRate;
    }

    public String getSalesPerson() {
        return salesPerson;
    }

    public void setSalesPerson(String salesPerson) {
        this.salesPerson = salesPerson;
    }
}
