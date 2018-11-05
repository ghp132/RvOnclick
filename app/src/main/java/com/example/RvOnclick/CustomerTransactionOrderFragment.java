package com.example.RvOnclick;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerTransactionOrderFragment extends Fragment implements OrderProductAdapter.OnItemClickListener {

    public static StDatabase stDatabase;
    protected String custCode;
    public Long orderId;
    public List<OrderProduct> productList;
    private OrderProductAdapter.OnItemClickListener listener;



    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }



    public CustomerTransactionOrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_transaction_order, container, false);

        custCode = getArguments().getString("custCode");
        Log.d("TransactionActivity", "onCreateView: ");


        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();

        try{
            try{
                orderId = Long.parseLong(getActivity().getIntent().getStringExtra("orderId"));
            } catch (NumberFormatException ee){
                orderId = Long.parseLong(String.valueOf(-1));
            }
        }
        catch (NullPointerException e){

            int temp = -1;
            orderId = Long.parseLong(String.valueOf(-1));
        }



        if (orderId != -1 && orderId != null){

            productList = stDatabase.stDao().getOrderProductsById(orderId);

            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.OrderProduct_recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            listener = this;
            OrderProductAdapter productAdapter = new OrderProductAdapter(listener, productList, getActivity());
            recyclerView.setAdapter(productAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //get the order id from the dialogfragment
        //orderId = Integer.parseInt(data.getStringExtra("orderId"));
    }


    @Override
    public void onItemClicked(View v, int position) {


        String prodCode = productList.get(position).getProductCode();
        Double qty = productList.get(position).getQty();
        Double rate = productList.get(position).getRate();

        // data passed to product rate info dialog fragment
        Bundle args = new Bundle();
        args.putString("prodCode", prodCode);
        args.putString("custCode", custCode);
        args.putString("orderId", String.valueOf(orderId));
        args.putDouble("qty",qty);
        args.putDouble("rate",rate);




        DialogFragment_ProductRateInfo df = new DialogFragment_ProductRateInfo();
        df.setArguments(args);
        df.setCancelable(false);
        df.setTargetFragment(this, 1);


        df.show(getFragmentManager(), "Dialog");
        setUserVisibleHint(false);
    }




}
