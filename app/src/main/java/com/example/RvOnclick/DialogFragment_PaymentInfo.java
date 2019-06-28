package com.example.RvOnclick;


import android.app.DatePickerDialog;
import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.RvOnclick.CustomerTransactionProductFragment.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class DialogFragment_PaymentInfo extends DialogFragment {
    Button btPaymentSave, btCancell, btPaymentDelete;
    EditText etPaymentAmt, etChequeNo, etChequeDate, etChequeBank;
    CheckBox cbCheque;
    TextView tvPaymentErrorInfo;
    Double paymentAmt, outstanding;
    String chequeNo, chequeDate, chequeBank, info;
    public static StDatabase stDatabase;
    public String custCode, invoiceNo,company;
    Payment payment = new Payment();
    Payment paidAmt = new Payment();
    Integer mYear, mMonth, mDay;
    Long paymentId, oldPaymentid, orderId;
    Boolean previouslyPaid;
    ApplicationController ac = new ApplicationController();
    int smsPermissionGranted;
    String uniquePaymentId;


    public DialogFragment_PaymentInfo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        custCode = getActivity().getIntent().getStringExtra("custCode");
        invoiceNo = getActivity().getIntent().getStringExtra("invoiceNo");
        company = getActivity().getIntent().getStringExtra("company");
        orderId = getActivity().getIntent().getLongExtra("orderId", 0);
        String str = getActivity().getIntent().getStringExtra("outstanding");
        outstanding = Double.parseDouble(getActivity().getIntent().getStringExtra("outstanding"));
        getActivity().getIntent().putExtra("deletePayment",0);//will be set to 1 when delete is clicked
        //paidAmt = Double.valueOf(getActivity().getIntent().getStringExtra("paidAmt"));

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_fragment__payment_info, container, false);
        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();
        paidAmt = stDatabase.stDao().getUnsyncedPaymentByInvoiceNo(invoiceNo);

        previouslyPaid = false;
        if (paidAmt!=null){
            oldPaymentid = paidAmt.getPaymentId();
            previouslyPaid = true;
            paymentId = oldPaymentid;
        } // in case there is an unsynced payment for the same invoice


        btCancell = view.findViewById(R.id.bt_cancel);
        btPaymentSave = view.findViewById(R.id.bt_paymentSave);
        btPaymentDelete = view.findViewById(R.id.bt_paymentDelete);
        etChequeBank = view.findViewById(R.id.et_bank);
        etChequeDate = view.findViewById(R.id.et_chequeDate);
        etChequeNo = view.findViewById(R.id.et_chequeNumber);
        etPaymentAmt = view.findViewById(R.id.et_paymentAmt);
        cbCheque = view.findViewById(R.id.cb_cheque);
        tvPaymentErrorInfo = view.findViewById(R.id.tv_paymentErrorInfo);

        info = custCode + "\n" + invoiceNo;
        if (previouslyPaid){info = info + "\n" + paidAmt.getPaymentAmt();}
        if (getTargetRequestCode()==102){
            btPaymentDelete.setVisibility(View.VISIBLE);

        }
        tvPaymentErrorInfo.setText(info);
        tvPaymentErrorInfo.setVisibility(View.VISIBLE);

        if (getTargetRequestCode()==102){
            etPaymentAmt.setText(paidAmt.getPaymentAmt().toString());
        } else {
            etPaymentAmt.setText(outstanding.toString());
        }
        etPaymentAmt.selectAll();

        etPaymentAmt.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


        cbCheque.setSelected(false);
        cbCheque.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
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
                if (Double.parseDouble(etPaymentAmt.getText().toString()) > outstanding) {
                    tvPaymentErrorInfo.setVisibility(View.VISIBLE);
                    tvPaymentErrorInfo.setText("Amount should be less than or equal to" + outstanding);
                } else {
                    if (etPaymentAmt.getText().toString().equals("") || etPaymentAmt.getText() == null) {
                        paymentAmt = 0.00;
                    } else {
                        paymentAmt = Double.parseDouble(etPaymentAmt.getText().toString());
                    }

                    chequeNo = etChequeNo.getText().toString();
                    chequeDate = etChequeDate.getText().toString();
                    chequeBank = etChequeBank.getText().toString();

                    if (paymentAmt == 0 || paymentAmt == null) { // if fields are empty or zero
                        //don't do anything

                        tvPaymentErrorInfo.setVisibility(View.VISIBLE);
                        tvPaymentErrorInfo.setText("Enter Amount!");
                    } else if (cbCheque.isChecked()) {
                        //in case of cheque payment
                        if (chequeBank.equals("") || chequeDate.equals("") || chequeNo.equals("")) {

                            tvPaymentErrorInfo.setVisibility(View.VISIBLE);
                            tvPaymentErrorInfo.setText("Complete Cheque Details!");
                        } else {
                            payment.setPaymentAmt(paymentAmt);
                            payment.setChequeBank(chequeBank);
                            payment.setChequeDate(chequeDate);
                            payment.setChequeNo(chequeNo);
                            payment.setCustomerCode(custCode);
                            payment.setInvoiceNo(invoiceNo);
                            if (orderId != 0) {
                                payment.setOrderId(orderId);
                            }
                            payment.setCompany(company);
                            payment.setChequePayment(true);
                            payment.setPaymentStatus(-1);
                            updatePaidAmtInInvoice();


                            if (previouslyPaid){
                                payment.setPaymentId(oldPaymentid);
                                stDatabase.stDao().updatePayment(payment);
                                paymentId = oldPaymentid;

                            } else {
                                paymentId = stDatabase.stDao().makePayment(payment);
                            }

                            //creating and updating unique payment id
                            stDatabase.stDao().updateAppPaymentId(CreateUniqueAppPaymentId(paymentId), paymentId);

                            getTargetFragment().onActivityResult(getTargetRequestCode(), 1, getActivity().getIntent());

                            //checkForSmsPermission();
                            getDialog().dismiss();

                        }
                    } else {
                        payment.setPaymentAmt(paymentAmt);
                        payment.setCustomerCode(custCode);
                        payment.setInvoiceNo(invoiceNo);
                        payment.setPaymentStatus(-1);
                        payment.setCompany(company);
                        payment.setChequePayment(false);
                        updatePaidAmtInInvoice();

                        if (previouslyPaid){
                            payment.setPaymentId(oldPaymentid);
                            stDatabase.stDao().updatePayment(payment);
                            paymentId = oldPaymentid;
                        } else {
                            paymentId = stDatabase.stDao().makePayment(payment);
                        }

                        //creating and updating unique payment id

                        stDatabase.stDao().updateAppPaymentId(CreateUniqueAppPaymentId(paymentId), paymentId);

                        getTargetFragment().onActivityResult(getTargetRequestCode(), 1, getActivity().getIntent());


                            checkForSmsPermission();

                        getDialog().dismiss();
                    }
                }

                /*if (stDatabase.stDao().getAllUserConfig().get(0).getSendSms()==1){
                    smsPermissionGranted = ac.checkForSmsPermission(getActivity(),1);
                    if (smsPermissionGranted==0){
                        //Toast.makeText(getActivity(), "SMS Permission Denied", Toast.LENGTH_SHORT).show();
                    } else {
                        sendSms();


                    }
                }*/


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
                if (hasFocus) {
                    final Calendar calendar = Calendar.getInstance();
                    mYear = calendar.get(Calendar.YEAR);
                    mMonth = calendar.get(Calendar.MONTH);
                    mDay = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity()
                            , new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            etChequeDate.setText(dayOfMonth + "-" + (month + 1) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                }
            }
        });

        btPaymentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stDatabase.stDao().deletePaymentByPaymentId(paymentId);
                getTargetFragment().onActivityResult(getTargetRequestCode(), 1, getActivity().getIntent());
                dismiss();
            }
        });

        return view;
    }

    public String CreateUniqueAppPaymentId(Long paymentId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String ts = simpleDateFormat.format(new Date());
        String uniqueAppPaymentId = ts + paymentId + "P";
        return uniqueAppPaymentId;

    }

    private void createSmsContent() {
        SmsManager smsManager = SmsManager.getDefault();
        String mobileNo = stDatabase.stDao().getMobileNoByCustomer(custCode);
        getActivity().getIntent().putExtra("mobileNo",mobileNo);

        String smsContent = "Received Rs." + paymentAmt.toString() + " against bill no.:"
                + invoiceNo + ". Subject to verification.";
        getActivity().getIntent().putExtra("smsContent",smsContent);
        //smsManager.sendTextMessage(mobileNo,null,smsContent,null,null);
        Log.d(TAG, "createSmsContent: Prepared");
        //Toast.makeText(getActivity(), "SMS Content Prepared", Toast.LENGTH_SHORT).show();
    }

    private void checkForSmsPermission(){
        if (stDatabase.stDao().getAllUserConfig().get(0).getSendSms()==1){
            createSmsContent();
            smsPermissionGranted = ac.checkForSmsPermission(getActivity(),1);
            if (smsPermissionGranted==0){
                //Toast.makeText(getActivity(), "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            } else {
                String mobileNo = getActivity().getIntent().getStringExtra("mobileNo");
                String smsContent = getActivity().getIntent().getStringExtra("smsContent");
                if (mobileNo!=null) {
                    ac.sendSms(mobileNo, smsContent, getActivity());
                }
            }
        }

    }

    private void updatePaidAmtInInvoice() {
        Invoice invoice = stDatabase.stDao().getInvoiceByInvoiceNo(invoiceNo);
        invoice.setPaidAmount(paymentAmt);
        stDatabase.stDao().updateInvoice(invoice);
        //Todo: clear paidAmt after syncing

    }
}
