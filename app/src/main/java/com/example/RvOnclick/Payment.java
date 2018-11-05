package com.example.RvOnclick;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Payment {

    @PrimaryKey(autoGenerate = true)
    private Long paymentId;
    private String customerCode;
    private String invoiceNo;
    private Double paymentAmt;
    private String paymentDate;
    private Boolean isChequePayment;
    private String chequeNo;
    private String chequeDate;
    private String chequeBank;

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public Double getPaymentAmt() {
        return paymentAmt;
    }

    public void setPaymentAmt(Double paymentAmt) {
        this.paymentAmt = paymentAmt;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Boolean getChequePayment() {
        return isChequePayment;
    }

    public void setChequePayment(Boolean chequePayment) {
        isChequePayment = chequePayment;
    }

    public String getChequeNo() {
        return chequeNo;
    }

    public void setChequeNo(String chequeNo) {
        this.chequeNo = chequeNo;
    }

    public String getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(String chequeDate) {
        this.chequeDate = chequeDate;
    }

    public String getChequeBank() {
        return chequeBank;
    }

    public void setChequeBank(String chequeBank) {
        this.chequeBank = chequeBank;
    }
}
