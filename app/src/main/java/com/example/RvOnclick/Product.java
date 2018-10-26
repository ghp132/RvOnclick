package com.example.RvOnclick;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Product {
    @PrimaryKey
    @NonNull
    private String productCode;

    @ColumnInfo
    private String productName;
    private String productBrand;
    private Boolean productDisabled;
    private String productGroup;
    private double productRate;

    @NonNull
    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(@NonNull String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public Boolean getProductDisabled() {
        return productDisabled;
    }

    public void setProductDisabled(Boolean productDisabled) {
        this.productDisabled = productDisabled;
    }

    public String getProductGroup() {
        return productGroup;
    }

    public void setProductGroup(String productGroup) {
        this.productGroup = productGroup;
    }

    public double getProductRate() {
        return productRate;
    }

    public void setProductRate(double productRate) {
        this.productRate = productRate;
    }
}
