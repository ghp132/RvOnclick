package com.example.RvOnclick;


import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerTransactionProductFragment extends Fragment implements ProductAdapter.OnItemClickListener {


    private TextView mTextView;
    private RecyclerView mRecyclerview;
    public static StDatabase stDatabase;
    protected String custCode;
    //private DialogFragment_ProductRateInfo.OnProductSelectedListener mListener;
    public int orderId;


    public List<Product> productList;


    private ProductAdapter.OnItemClickListener listener;
    //public CustomerAdapter customerAdapter = new CustomerAdapter(listener,customerList, getActivity());


    public CustomerTransactionProductFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_transaction_product, container, false);


        custCode = getArguments().getString("custCode");
        orderId = -1;

        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();


        productList = stDatabase.stDao().getProduct();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.product_recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        listener = this;
        ProductAdapter productAdapter = new ProductAdapter(listener, productList, getActivity());
        recyclerView.setAdapter(productAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        orderId = Integer.parseInt(data.getStringExtra("orderId"));
    }


    @Override
    public void onItemClicked(View v, int position) {


        String prodCode = productList.get(position).getProductCode();
        //Toast.makeText(getActivity(), custCode, Toast.LENGTH_SHORT).show();

        Bundle args = new Bundle();
        args.putString("prodCode", prodCode);
        args.putString("custCode", custCode);
        args.putString("orderId", String.valueOf(orderId));


        DialogFragment_ProductRateInfo df = new DialogFragment_ProductRateInfo();
        df.setArguments(args);
        df.setCancelable(false);
        df.setTargetFragment(this, 1);


        df.show(getFragmentManager(), "Dialog");

    }




   /* private void prepareCustomerData(){
        Customer customer = new Customer("customercode1", "customername1","territory1");
        customerList.add(customer);

        customer = new Customer("customercode2","customername2","territory2");
        customerList.add(customer);

        customer = new Customer ("customercode3","customername3","territory1");
        customerList.add(customer);

        customer = new Customer("customercode4","customername4","territory2");
        customerList.add(customer);
    }*/

}
