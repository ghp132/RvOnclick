package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class ProductsList {
    @PrimaryKey(autoGenerate = true)
    private Long productsListId;
    private String docType;
    private String company;
    private String warehouse;
    private boolean editable;
    private String timeStamp;

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public Long getProductsListId() {
        return productsListId;
    }

    public void setProductsListId(Long productsListId) {
        this.productsListId = productsListId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
