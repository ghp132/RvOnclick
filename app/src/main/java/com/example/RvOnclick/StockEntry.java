package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class StockEntry {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String stockEntryNumber, company, sourceWarehouse, targetWarehouse, uniqueValue;
    private Long productsListId;
    private int docStatus;

    public String getUniqueValue() {
        return uniqueValue;
    }

    public void setUniqueValue(String uniqueValue) {
        this.uniqueValue = uniqueValue;
    }

    public int getDocStatus() {
        return docStatus;
    }

    public void setDocStatus(int docStatus) {
        this.docStatus = docStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStockEntryNumber() {
        return stockEntryNumber;
    }

    public void setStockEntryNumber(String stockEntryNumber) {
        this.stockEntryNumber = stockEntryNumber;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSourceWarehouse() {
        return sourceWarehouse;
    }

    public void setSourceWarehouse(String sourceWarehouse) {
        this.sourceWarehouse = sourceWarehouse;
    }

    public String getTargetWarehouse() {
        return targetWarehouse;
    }

    public void setTargetWarehouse(String targetWarehouse) {
        this.targetWarehouse = targetWarehouse;
    }

    public Long getProductsListId() {
        return productsListId;
    }

    public void setProductsListId(Long productsListId) {
        this.productsListId = productsListId;
    }
}
