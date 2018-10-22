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
public class TabFragment1 extends Fragment implements CustomerAdapter.OnItemClickListener {


    private TextView mTextView;
    private RecyclerView mRecyclerview;
    public static StDatabase stDatabase;




    public List<Customer> customerList;


    private CustomerAdapter.OnItemClickListener listener;
    //public CustomerAdapter customerAdapter = new CustomerAdapter(listener,customerList, getActivity());


    public TabFragment1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_fragment1, container, false);


        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
        .allowMainThreadQueries().build();


        customerList = stDatabase.stDao().getCustomer();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        listener = this;
        CustomerAdapter customerAdapter = new CustomerAdapter(listener,customerList, getActivity());
        recyclerView.setAdapter(customerAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return view;
    }

    @Override
    public void onItemClicked(View v, int position){

        String custCode  = customerList.get(position).getCustomer_id();
        //Toast.makeText(getActivity(), custCode, Toast.LENGTH_SHORT).show();
        /*Bundle args = new Bundle();
        args.putString("custCode", custCode);

        DialogFragment_ProductRateInfo df = new DialogFragment_ProductRateInfo();
        df.setArguments(args);
        df.show(getFragmentManager(),"Dialog");*/

        Intent intent = new Intent(getActivity(), CustomerTransactionActivity.class);
        intent.putExtra("custCode", custCode);
        getActivity().startActivity(intent);
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
