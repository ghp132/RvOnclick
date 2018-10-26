package com.example.RvOnclick;


import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment2 extends Fragment {
    StDatabase stDatabase;
    List<Order> orderList;
    List<OrderProduct> orderProductList;

    TextView textView;



    public TabFragment2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_fragment2, container, false);
        String ordersList = "";
        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();

         orderList = stDatabase.stDao().getAllOrder();
        textView=view.findViewById(R.id.tv_fragment2);
        for (int i = 0; i<orderList.size(); i++){
            Long orderId = orderList.get(i).getOrderId();
            String orderCustomerCode = orderList.get(i).getCustomerCode();
            ordersList = ordersList + "\n\n" + orderId + "\n" + orderCustomerCode;
            orderProductList = stDatabase.stDao().getOrderProductsById(orderId);
            for (int c = 0; c<orderProductList.size(); c++){
                String prodCode = orderProductList.get(c).getProductCode();
                double qty = orderProductList.get(c).getQty();
                ordersList = ordersList + "\n" + prodCode + " : "+ qty;
            }

        }
        textView.setText(ordersList);
        return view;
    }

}
