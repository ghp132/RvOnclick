package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Warehouse {
    @PrimaryKey
    @NonNull
    private String name;
    private String warehouseName;
    private String company;
    private String parentWarehouse;
    private boolean isGroup;

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getParentWarehouse() {
        return parentWarehouse;
    }

    public void setParentWarehouse(String parentWarehouse) {
        this.parentWarehouse = parentWarehouse;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }
}
