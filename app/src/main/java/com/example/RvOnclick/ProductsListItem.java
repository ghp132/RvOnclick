package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class ProductsListItem {
    @PrimaryKey(autoGenerate = true)
    private Long productsListItemId;
    private String productCode, productName, warehouse, companyName, baseUom, uom;
    private double uomConversion;
    private Long parentId, childId, productsListId;
    private double rate, discountPercentage, qty, cgst, sgst, igst, cess;

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

    public Long getProductsListId() {
        return productsListId;
    }

    public void setProductsListId(Long productsListId) {
        this.productsListId = productsListId;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public Long getProductsListItemId() {
        return productsListItemId;
    }

    public void setProductsListItemId(Long productsListItemId) {
        this.productsListItemId = productsListItemId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getBaseUom() {
        return baseUom;
    }

    public void setBaseUom(String baseUom) {
        this.baseUom = baseUom;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public double getUomConversion() {
        return uomConversion;
    }

    public void setUomConversion(double uomConversion) {
        this.uomConversion = uomConversion;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }
}
