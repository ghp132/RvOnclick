package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Order {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Long orderId;
    private String customerCode;
    private boolean orderStatus;


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

    public boolean isOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(boolean orderStatus) {
        this.orderStatus = orderStatus;
    }
}


