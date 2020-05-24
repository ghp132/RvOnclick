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

    public String getProductCompany() {
        return productCompany;
    }

    public void setProductCompany(String productCompany) {
        this.productCompany = productCompany;
    }

    private String productCompany;
    private Boolean productDisabled;
    private String productGroup, defaultSalesUom, stockUom;
    private double productRate, defSalesUomConversion;
    private double stock, cgst, sgst, igst, cess;
    private Double currentOrderQty;
    private double currentOrderFreeQty;

    public String getDefaultSalesUom() {
        return defaultSalesUom;
    }

    public void setDefaultSalesUom(String defaultSalesUom) {
        this.defaultSalesUom = defaultSalesUom;
    }

    public String getStockUom() {
        return stockUom;
    }

    public void setStockUom(String stockUom) {
        this.stockUom = stockUom;
    }

    public double getDefSalesUomConversion() {
        return defSalesUomConversion;
    }

    public void setDefSalesUomConversion(double defUomSalesConversion) {
        this.defSalesUomConversion = defSalesUomConversion;
    }

    public double getCgst() {
        return cgst;
    }

    public void setCgst(double cgst) {
        this.cgst = cgst;
    }

    public double getSgst() {
        return sgst;
    }

    public void setSgst(double sgst) {
        this.sgst = sgst;
    }

    public double getIgst() {
        return igst;
    }

    public void setIgst(double igst) {
        this.igst = igst;
    }

    public double getCess() {
        return cess;
    }

    public void setCess(double cess) {
        this.cess = cess;
    }

    public double getCurrentOrderFreeQty() {
        return currentOrderFreeQty;
    }

    public void setCurrentOrderFreeQty(double currentOrderFreeQty) {
        this.currentOrderFreeQty = currentOrderFreeQty;
    }

    public Double getCurrentOrderQty() {
        return currentOrderQty;
    }

    public void setCurrentOrderQty(Double currentOrderQty) {
        this.currentOrderQty = currentOrderQty;
    }

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


    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }
}
