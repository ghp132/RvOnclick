package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class TblSettings {

    @PrimaryKey
    @NonNull
    private String defn;
    private String svalues;

    public String getSvalues() {
        return svalues;
    }

    public void setSvalues(String svalues) {
        this.svalues = svalues;
    }

    public String getDefn() {
        return defn;
    }

    public void setDefn(String defn) {
        this.defn = defn;
    }


}
