package com.example.RvOnclick;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.RvOnclick.ApplicationController.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerTransactionHistoryFragment extends Fragment implements InvoiceAdapter.OnItemClickListener {

    public static StDatabase stDatabase;
    protected String custCode;
    public List<Invoice> invoiceList;
    private InvoiceAdapter.OnItemClickListener listener;
    RecyclerView recyclerView;
    InvoiceAdapter invoiceAdapter;
    boolean dbCreated = false;
    boolean refresh = false;

    public CustomerTransactionHistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_transaction_history, container, false);

        custCode = getArguments().getString("custCode");


        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();
        dbCreated = true;
        invoiceList = stDatabase.stDao().getOutstandingInvoicesByCustomerCode(custCode);

        if (!invoiceList.isEmpty()) {
            //return view;
            //} else {
            Collections.sort(invoiceList, new Comparator<Invoice>() {
                @Override
                public int compare(Invoice o1, Invoice o2) {
                    return o1.getInvoiceDate().compareToIgnoreCase(o2.getInvoiceDate());
                }
            });
        }
        recyclerView = view.findViewById(R.id.invoice_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listener = this;
        invoiceAdapter = new InvoiceAdapter(listener, invoiceList, getActivity());
        recyclerView.setAdapter(invoiceAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        if (refresh) {
            refreshRecyclerView();
        }
        return view;
        //}
    }

    @Override
    public void onItemClicked(View v, int postion) {
        Double outstandingAmount = invoiceList.get(postion).getOutstanding();
        if (outstandingAmount == 0) {
            Toast.makeText(getActivity().getApplicationContext(), "No outstanding amount!", Toast.LENGTH_SHORT).show();
        } else {
            String invoiceNo = invoiceList.get(postion).getInvoiceNumber();
            String company = invoiceList.get(postion).getCompany();

            getActivity().getIntent().putExtra("invoiceNo", invoiceNo);
            getActivity().getIntent().putExtra("company", company);
            getActivity().getIntent().putExtra("outstanding", outstandingAmount.toString());
            //getActivity().getIntent().putExtra("custCode",custCode);

            DialogFragment_PaymentInfo df = new DialogFragment_PaymentInfo();
            df.setCancelable(false);
            df.setTargetFragment(this, 1);
            df.show(getFragmentManager(), "Payment_Info");

        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: overridden");
        if (isVisibleToUser) {
            if (dbCreated) {
                refreshRecyclerView();
                refresh = true;
                Log.d(TAG, "setUserVisibleHint: true");
                showNewPaymentFragment();


            } else {
                refresh = true;
            }

        }
    }

    private void refreshRecyclerView() {
        invoiceList = stDatabase.stDao().getOutstandingInvoicesByCustomerCode(custCode);
        invoiceAdapter.notifyDataSetChanged();
    }


    private void showNewPaymentFragment() {
        Intent m2aIntent = new Intent(getActivity(), Main2Activity.class);
        m2aIntent.putExtra("custCode", custCode);
        m2aIntent.putExtra("fragment", com.example.RvOnclick.Utils.CUSTOMER_OUTSTANDING_LIST_FRAGMENT);
        getActivity().startActivity(m2aIntent);
    }

}
