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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerTransactionOrderFragment extends Fragment implements OrderProductAdapter.OnItemClickListener {

    public static StDatabase stDatabase;
    public Long orderId;
    public double orderTotalValue;
    public List<OrderProduct> productList = new ArrayList<>();
    protected String custCode;
    RecyclerView recyclerView;
    private OrderProductAdapter.OnItemClickListener listener;
    OrderProductAdapter productAdapter;
    TextView tvOrderInfo;
    private int orderStatus;
    ApplicationController ac = new ApplicationController();
    boolean shouldBeRefreshed;
    boolean dbCreated=false;


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
        tvOrderInfo = view.findViewById(R.id.tv_customerOrderInfo);

        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();
        dbCreated=true;
        orderTotalValue = stDatabase.stDao().getOrderTotalValueByOrderId(orderId);
        tvOrderInfo.setText(String.valueOf(orderTotalValue));

        try {
            try {

                orderId = Long.parseLong(getActivity().getIntent().getStringExtra("orderId"));
                //Toast.makeText(getActivity().getApplicationContext(),orderId.toString(),Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException ee) {
                orderId = Long.parseLong(String.valueOf(-1));
            }
        } catch (NullPointerException e) {

            int temp = -1;
            //Toast.makeText(getActivity().getApplicationContext(),orderId.toString(),Toast.LENGTH_SHORT).show();
            orderId = Long.parseLong(String.valueOf(-1));
        }


        if (orderId != -1 && orderId != null) {
            orderTotalValue = stDatabase.stDao().getOrderTotalValueByOrderId(orderId);
            productList = stDatabase.stDao().getOrderProductsById(orderId);
            orderStatus = stDatabase.stDao().getOrderStatusByOrderId(orderId);
            recyclerView = (RecyclerView) view.findViewById(R.id.OrderProduct_recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            listener = this;
            productAdapter = new OrderProductAdapter(listener, productList, getActivity());
            recyclerView.setAdapter(productAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }

        if (shouldBeRefreshed){
            refreshFragment();
            shouldBeRefreshed=false;
        }

        return view;


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        productList.clear();
        productList.addAll(stDatabase.stDao().getOrderProductsById(orderId));
        productAdapter.notifyDataSetChanged();
        /*productList = stDatabase.stDao().getOrderProductsById(orderId);
        OrderProductAdapter productAdapter = new OrderProductAdapter(listener, productList, getActivity());
        recyclerView.setAdapter(productAdapter);*/
        orderTotalValue = stDatabase.stDao().getOrderTotalValueByOrderId(orderId);
        tvOrderInfo.setText(String.valueOf(orderTotalValue));

    }


    @Override
    public void onItemClicked(View v, int position) {

        if (orderStatus == -1) {
            String prodCode = productList.get(position).getProductCode();
            OrderProduct orderProduct = productList.get(position);
            double freeQty = 0;
            long parentId = orderProduct.getParentId();
            long childId = orderProduct.getChildId();
            if (childId!=0) {
                for (OrderProduct prod : productList) {
                    if(prod.getOrderProductId()==orderProduct.getChildId()){
                        freeQty=prod.getQty();
                    }
                }
            }
            Double qty = productList.get(position).getQty();
            Double rate = productList.get(position).getRate();

            if (parentId!=0){
                for (OrderProduct prod : productList) {
                    if(prod.getOrderProductId()==orderProduct.getParentId()){
                        qty=prod.getQty();
                        freeQty = orderProduct.getQty();
                    }
                }
            }

            // data passed to product rate info dialog fragment
            Bundle args = new Bundle();
            args.putString("prodCode", prodCode);
            args.putString("custCode", custCode);
            args.putString("orderId", String.valueOf(orderId));
            args.putDouble("qty", qty);
            args.putDouble("freeQty",freeQty);
            args.putDouble("rate", rate);


            DialogFragment_ProductRateInfo df = new DialogFragment_ProductRateInfo();
            df.setArguments(args);
            df.setCancelable(false);
            df.setTargetFragment(this, 2);

            df.show(getFragmentManager(), "Dialog");
            setUserVisibleHint(false);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "This order cannot be editted.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onLongClick(View view, final int position) {
        // Toast.makeText(getActivity(),"clicked at position" + position,Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getActivity());
        }
        builder.setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        /*if (stDatabase.stDao().countOrderProduct(orderId) > 1) {
                            stDatabase.stDao().deleteOrderProduct(productList.get(position));
                            updateProductAdapterfromDB();
                        } else if (stDatabase.stDao().countOrderProduct(orderId) == 1) {
                            stDatabase.stDao().deleteOrderProduct(productList.get(position));
                            stDatabase.stDao().deleteOrderByOrderId(orderId);
                            orderId = Long.valueOf(-1);
                            getActivity().getIntent().putExtra("orderId", orderId);
                            productList.clear();
                            productAdapter.notifyDataSetChanged();
                            getActivity().finish();
                        }*/

                        ac.deleteOrderProduct(productList.get(position).getOrderProductId(),stDatabase);
                        int countOfOrderProduct = stDatabase.stDao().countOrderProduct(orderId);
                        updateProductAdapterfromDB();
                        if (countOfOrderProduct==0){
                            stDatabase.stDao().deleteOrderByOrderId(orderId);
                            orderId = Long.valueOf(-1);
                            getActivity().getIntent().putExtra("orderId", orderId);
                            productList.clear();
                            productAdapter.notifyDataSetChanged();
                            getActivity().finish();
                        }

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
          //      .allowMainThreadQueries().build();
        if (isVisibleToUser) {

            if (dbCreated){
                refreshFragment();
            }
            shouldBeRefreshed=true;
            //stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(),StDatabase.class,"StDb").allowMainThreadQueries().build();
            /*getFragmentManager().beginTransaction().detach(this).attach(this).commit();
            orderTotalValue = stDatabase.stDao().getOrderTotalValueByOrderId(orderId);
            tvOrderInfo.setText(String.valueOf(Math.round(orderTotalValue)));*/
        }
    }

    private void updateProductAdapterfromDB() {
        productList.clear();
        productList.addAll(stDatabase.stDao().getOrderProductsById(orderId));
        productAdapter.notifyDataSetChanged();
    }


    private void refreshFragment(){
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        orderTotalValue = stDatabase.stDao().getOrderTotalValueByOrderId(orderId);
        tvOrderInfo.setText(String.valueOf(Math.round(orderTotalValue)));
    }
}