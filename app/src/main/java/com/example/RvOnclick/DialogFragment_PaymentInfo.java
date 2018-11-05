package com.example.RvOnclick;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DialogFragment_PaymentInfo extends DialogFragment {
    Button btPaymentSave, btCancell;
    EditText etPaymentAmt,etChequeNo,etChequeDate, etChequeBank;
    CheckBox cbCheque;
    TextView tvPaymentErrorInfo;
    Double paymentAmt;
    String chequeNo, chequeDate, chequeBank;
    public static StDatabase stDatabase;
    public String custCode,invoiceNo;
    Payment payment = new Payment();
    Integer mYear,mMonth,mDay;


    public DialogFragment_PaymentInfo() {
        // Required empty public constructor
    }





/*
    @Override
    public void onAttach (Context context){
        super.onAttach(context);
        try{
            mListener = (OnProductSelectedListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + //" must implement OnProductSelectedListener");
        }
    }
*/




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        custCode = getActivity().getIntent().getStringExtra("custCode");
        invoiceNo = getActivity().getIntent().getStringExtra("invoiceNo");



        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_fragment__payment_info, container, false);
        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();


        btCancell = view.findViewById(R.id.bt_cancel);
        btPaymentSave = view.findViewById(R.id.bt_paymentSave);
        etChequeBank = view.findViewById(R.id.et_bank);
        etChequeDate = view.findViewById(R.id.et_chequeDate);
        etChequeNo = view.findViewById(R.id.et_chequeNumber);
        etPaymentAmt = view.findViewById(R.id.et_paymentAmt);
        cbCheque = view.findViewById(R.id.cb_cheque);
        tvPaymentErrorInfo = view.findViewById(R.id.tv_paymentErrorInfo);

        tvPaymentErrorInfo.setText(custCode + "/n/n" + invoiceNo);
        tvPaymentErrorInfo.setVisibility(View.VISIBLE);




        cbCheque.setSelected(false);
        cbCheque.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    etChequeBank.setVisibility(View.VISIBLE);
                    etChequeDate.setVisibility(View.VISIBLE);
                    etChequeNo.setVisibility(View.VISIBLE);
                } else {
                    etChequeBank.setVisibility(View.GONE);
                    etChequeDate.setVisibility(View.GONE);
                    etChequeNo.setVisibility(View.GONE);
                }
            }
        });

        btPaymentSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etPaymentAmt.getText().toString().equals("")||etPaymentAmt.getText()==null){paymentAmt=0.00;}
                else {paymentAmt = Double.parseDouble(etPaymentAmt.getText().toString());}

                chequeNo = etChequeNo.getText().toString();
                chequeDate = etChequeDate.getText().toString();
                chequeBank = etChequeBank.getText().toString();
                if (paymentAmt == 0||paymentAmt == null){

                    tvPaymentErrorInfo.setVisibility(View.VISIBLE);
                    tvPaymentErrorInfo.setText("Enter Amount!");
                } else if (cbCheque.isChecked()){
                    if (chequeBank.equals("")||chequeDate.equals("")||chequeNo.equals("")){

                        tvPaymentErrorInfo.setVisibility(View.VISIBLE);
                        tvPaymentErrorInfo.setText("Complete Cheque Details!");
                    } else {
                        payment.setPaymentAmt(paymentAmt);
                        payment.setChequeBank(chequeBank);
                        payment.setChequeDate(chequeDate);
                        payment.setChequeNo(chequeNo);
                        payment.setCustomerCode(custCode);
                        payment.setInvoiceNo(invoiceNo);
                        payment.setChequePayment(true);
                        stDatabase.stDao().makePayment(payment);
                        getDialog().dismiss();

                    }
                }else {
                    payment.setPaymentAmt(paymentAmt);
                    payment.setCustomerCode(custCode);
                    payment.setInvoiceNo(invoiceNo);
                    stDatabase.stDao().makePayment(payment);
                    getDialog().dismiss();
                }



            }
        });

        btCancell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        etChequeDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    final Calendar calendar = Calendar.getInstance();
                    mYear = calendar.get(Calendar.YEAR);
                    mMonth = calendar.get(Calendar.MONTH);
                    mDay = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity()
                            , new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            etChequeDate.setText(dayOfMonth + "-" + (month+1) + "-" + year);

                        }
                    },mYear,mMonth,mDay);
                    datePickerDialog.show();
                }
            }
        });

        return view;
    }
}
