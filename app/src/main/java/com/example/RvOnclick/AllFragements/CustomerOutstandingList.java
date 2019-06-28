package com.example.RvOnclick.AllFragements;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.RvOnclick.ApplicationController;
import com.example.RvOnclick.DialogFragment_PaymentInfo;
import com.example.RvOnclick.Invoice;
import com.example.RvOnclick.InvoiceAdapter;
import com.example.RvOnclick.Order;
import com.example.RvOnclick.R;
import com.example.RvOnclick.StDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomerOutstandingList.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CustomerOutstandingList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomerOutstandingList extends Fragment implements InvoiceAdapter.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static StDatabase stDatabase;
    private static ApplicationController ac = new ApplicationController();
    protected String custCode;
    public List<Invoice> invoiceList;
    private InvoiceAdapter.OnItemClickListener listener;
    RecyclerView recyclerView;
    InvoiceAdapter invoiceAdapter;
    boolean dbCreated = false;
    boolean refresh = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public CustomerOutstandingList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CustomerOutstandingList.
     */
    // TODO: Rename and change types and number of parameters
    public static CustomerOutstandingList newInstance(String param1, String param2) {
        CustomerOutstandingList fragment = new CustomerOutstandingList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_outstanding_list, container, false);


        custCode = getActivity().getIntent().getStringExtra("custCode");



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

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClicked(View view, int position) {
        Long orderId = invoiceList.get(position).getOrderId();
        Order order = stDatabase.stDao().getOrderByOrderId(invoiceList.get(position).getOrderId());
        List<Long> orderIdList = new ArrayList<>();
        if (orderId != null) {
            //invoices created on ErpNext will not contain orderId
            orderIdList = ac.splitProductListByCompany(order, stDatabase);
        } else {
            orderIdList.add(orderId);
        }
        //refreshRecyclerView();
        if (orderIdList.size() > 1) {
            refreshRecyclerView();
            Toast.makeText(getActivity(), "The order has been split", Toast.LENGTH_SHORT).show();
        } else {

            Double outstandingAmount = invoiceList.get(position).getOutstanding();
            if (outstandingAmount == 0) {
                Toast.makeText(getActivity().getApplicationContext(), "No outstanding amount!", Toast.LENGTH_SHORT).show();
            } else {
                String invoiceNo = invoiceList.get(position).getInvoiceNumber();
                Invoice invoice = stDatabase.stDao().getInvoiceByInvoiceNo(invoiceNo);
                String company = invoice.getCompany();

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
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void refreshRecyclerView() {
        invoiceList.clear();
        invoiceList.addAll(stDatabase.stDao().getOutstandingInvoicesByCustomerCode(custCode));
        invoiceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshRecyclerView();
    }
}
