package com.example.RvOnclick;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
public class TabFragment2 extends Fragment implements AdapterOrder.OnItemClickListener {
    StDatabase stDatabase;
    List<Order> orderList = new ArrayList<>();
    List<OrderProduct> orderProductList;
    AdapterOrder adapterOrder;
    private AdapterOrder.OnItemClickListener listener;

    TextView textView;



    public TabFragment2() {
        // Required empty public constructor
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
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
            String orderNumber = orderList.get(i).getOrderNumber();
            int status = orderList.get(i).getOrderStatus();
            ordersList = ordersList + "\n\n" + orderId + "\n" + orderCustomerCode + "\n" + orderNumber+"\n"+status;
            orderProductList = stDatabase.stDao().getOrderProductsById(orderId);
            for (int c = 0; c<orderProductList.size(); c++){
                String prodCode = orderProductList.get(c).getProductCode();
                double qty = orderProductList.get(c).getQty();
                ordersList = ordersList + "\n" + prodCode + " : "+ qty;
            }

        }
        textView.setText(ordersList);

        //if (ordersList.isEmpty()){
        //    return  view;
        //} else {
            RecyclerView recyclerView = view.findViewById(R.id.order_recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            listener = this;
            adapterOrder = new AdapterOrder(listener, orderList, getActivity());
            recyclerView.setAdapter(adapterOrder);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            return view;

        //}



    }

    @Override
    public void onItemClicked(View v, int position){
        Order order = orderList.get(position);
        Long orderId = order.getOrderId();
        String custCode = order.getCustomerCode();
        Intent intent = new Intent(getActivity(),CustomerTransactionActivity.class);
        intent.putExtra("custCode",custCode);
        intent.putExtra("orderId",orderId.toString());
        intent.putExtra("fromOrderList","true");
        //Toast.makeText(getActivity().getApplicationContext(),orderId.toString(),Toast.LENGTH_SHORT).show();
        getActivity().startActivity(intent);
    }

    @Override
    public void onResume(){
        updateOrderListFromDB();
        super.onResume();

    }

    private void updateOrderListFromDB(){
        orderList.clear();
        orderList.addAll(stDatabase.stDao().getAllOrder());
        adapterOrder.notifyDataSetChanged();
    }

}
