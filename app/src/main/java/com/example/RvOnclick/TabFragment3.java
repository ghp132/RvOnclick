package com.example.RvOnclick;


import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment3 extends Fragment implements Rv1Adapter.OnItemClickListener, Rv1Adapter.OnItemLongClickListener {
    List<Payment> paymentList;
    RecyclerView recyclerView;
    Rv1Adapter adapter;
    Rv1Adapter.OnItemClickListener listener;
    Rv1Adapter.OnItemLongClickListener longClickListener;
    ApplicationController ac = new ApplicationController();
    StDatabase stDatabase;
    List<Rv1Item> rv1ItemList = new ArrayList<>();
    int clickedPosition, paymentStatus;
    private String TAG = "TabFragment3";


    public TabFragment3() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_fragment3, container, false);

        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();
        paymentList = stDatabase.stDao().getAllPayments();
        List<Rv1Item> itemList = createItemListForRV(paymentList);
        rv1ItemList.addAll(itemList);

        recyclerView = view.findViewById(R.id.rv1_recyclerView);
        listener = this;
        longClickListener = this;
        adapter = new Rv1Adapter(listener, longClickListener, rv1ItemList, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        return view;
    }

    private List<Rv1Item> createItemListForRV(List<Payment> paymnents) {
        List<Rv1Item> itemList = new ArrayList<>();

        for (Payment payment : paymnents) {
            Rv1Item item = new Rv1Item();
            String cCode = payment.getCustomerCode();
            Customer c = stDatabase.stDao().getCustomerbyCustomerCode(cCode);
            String customerName = stDatabase.stDao().getCustomerbyCustomerCode(payment.getCustomerCode()).getCustomer_name();
            item.setHeading(customerName);
            item.setInfo2(payment.getInvoiceNo());
            item.setInfo4(payment.getPaymentAmt().toString());
            item.setIntId(payment.getPaymentId().intValue());
            item.setInfo3(String.valueOf(payment.getPaymentStatus()));

            itemList.add(item);

        }
        return itemList;
    }

    @Override
    public void onItemClicked(View view, int position) {
        clickedPosition = position;
        final Payment payment = stDatabase.stDao()
                .getPaymentByPaymentId(Long.valueOf(rv1ItemList.get(position).getIntId()));
        Double paidAmt = payment.getPaymentAmt();

        Double outStanding = stDatabase.stDao().getInvoiceByInvoiceNo(payment.getInvoiceNo()).getOutstanding();
        String custCode = payment.getCustomerCode();
        String invoiceNo = rv1ItemList.get(position).getInfo2();
        String company = payment.getCompany();
        getActivity().getIntent().putExtra("invoiceNo", invoiceNo);
        getActivity().getIntent().putExtra("company", company);
        getActivity().getIntent().putExtra("outstanding", outStanding.toString());
        getActivity().getIntent().putExtra("custCode", custCode);
        getActivity().getIntent().putExtra("paymentId", payment.getPaymentId().toString());
        getActivity().getIntent().putExtra("paidAmt", paidAmt);
        paymentStatus = payment.getPaymentStatus();

        if (paymentStatus!=-1){
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(getActivity());
            }
            builder.setTitle("Delete entry")
                    .setMessage("Only Delete action is available. Do you want to delete this record?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            stDatabase.stDao().deletePayment(payment);
                            updateAdapterFromDB();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            DialogFragment_PaymentInfo df = new DialogFragment_PaymentInfo();
            df.setCancelable(false);
            df.setTargetFragment(this, 102);
            df.show(getFragmentManager(), "PaymentInfo");
        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102) {
            updateAdapterFromDB();
        }

    }

    private void updateAdapterFromDB(){
        rv1ItemList.clear();
        rv1ItemList.addAll(createItemListForRV(stDatabase.stDao().getAllPayments()));
        adapter.notifyDataSetChanged();

    }


    @Override
    public void onItemLongClicked(View view, int position) {

    }
}
