package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Order {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Long orderId;
    private Long splitFrom;
    private String customerCode, companyName;
    private int orderStatus;
    //order statuses
    // -1 sync unattempted
    // 0 sync attempted - result unknown
    // 1 draft
    // 2 submitted
    private String appOrderId;
    private String orderNumber;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getSplitFrom() {
        return splitFrom;
    }

    public void setSplitFrom(Long splitFrom) {
        this.splitFrom = splitFrom;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getAppOrderId() {
        return appOrderId;
    }

    public void setAppOrderId(String appOrderId) {
        this.appOrderId = appOrderId;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    @NonNull
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(@NonNull Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }


}


