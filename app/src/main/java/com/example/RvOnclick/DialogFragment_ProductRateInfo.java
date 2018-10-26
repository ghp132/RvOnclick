package com.example.RvOnclick;


import android.app.Dialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DialogFragment_ProductRateInfo extends DialogFragment {
    TextView tv;
    Button btProductSave, btIncrease, btDecrease, btCancel;
    EditText etQty, etRate;
    public static StDatabase stDatabase;
    public String custCode;
    public long orderId;

    public DialogFragment_ProductRateInfo() {
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

        final String prodCode = getArguments().getString("prodCode");
        orderId = Long.parseLong(getArguments().getString("orderId"));
        custCode = getArguments().getString("custCode");



        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_fragment__product_rate_info, container, false);
        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();



        btProductSave = view.findViewById(R.id.bt_productSave);
        btCancel = view.findViewById(R.id.bt_cancel);
        btIncrease = view.findViewById(R.id.bt_increase);
        btDecrease = view.findViewById(R.id.bt_decrease);
        etQty = view.findViewById(R.id.et_qty);
        etRate = view.findViewById(R.id.et_rate);

        etRate.setText(String.valueOf(getArguments().getDouble("rate")));


        btProductSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double qty = Double.parseDouble(etQty.getText().toString());
                Double rate = Double.parseDouble((etRate.getText().toString()));
                CreateNewOrderIfNotExists(orderId);
                AddProductToOrder(orderId,prodCode,qty,rate);

                //passing data to the CustomerTransactionProductFragment
                Intent intent = new Intent();
                intent.putExtra("orderId",String.valueOf(orderId));

                getTargetFragment().onActivityResult(getTargetRequestCode(),1,intent);


                getDialog().dismiss();

            }
        });
        btIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Integer.parseInt(etQty.getText().toString());
                qty = qty+1 ;
                etQty.setText(String.valueOf(qty));

            }
        });

        btDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int qty = Integer.parseInt(etQty.getText().toString());
                if (qty>=1){qty=qty-1;}
                etQty.setText(String.valueOf(qty));

            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        tv = (TextView) view.findViewById(R.id.tv1);
        String sampleText = prodCode + getArguments().get("custCode") + getArguments().getString("orderId");
        tv.setText(sampleText);
        return view;



    }
    public void CreateNewOrderIfNotExists(long orderId){
        if (orderId == -1){
            Order order = new Order();
            order.setCustomerCode(custCode);
            order.setOrderStatus(false);

            this.orderId = stDatabase.stDao().createOrder(order);

        }


    }

    public void AddProductToOrder(long orderId, String prodCode, double qty, double rate){
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrderId((int) orderId);
        orderProduct.setProductCode(prodCode);
        orderProduct.setQty(qty);
        orderProduct.setRate(rate);
        Boolean notPresent = true;
        //check to see if the product is already present in the order
        List<OrderProduct> orderProductList = stDatabase.stDao().getOrderProductsById(orderId);
        for (OrderProduct op : orderProductList){
            if (op.getProductCode().equals(prodCode)){
                qty = op.getQty()+qty;
                int orderProductId = op.getOrderProductId();
                stDatabase.stDao().updateOrderProductById(qty, rate, orderProductId);
                notPresent = false;
                break;
            }
        }
        if (notPresent) {
            stDatabase.stDao().addProductToOrder(orderProduct);
        }
    }


}
