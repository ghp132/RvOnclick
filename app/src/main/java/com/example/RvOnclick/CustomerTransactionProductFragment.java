package com.example.RvOnclick;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerTransactionProductFragment extends Fragment implements ProductAdapter.OnItemClickListener {


    public static StDatabase stDatabase;
    protected String custCode;
    public int orderId, orderStatus;
    public List<Product> productList, searchableProductList;
    private ProductAdapter.OnItemClickListener listener;
    EditText productSearchBox;
    public static String TAG = "CustomerTransactionFragment";

    public CustomerTransactionProductFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_transaction_product, container, false);
        ApplicationController ac = new ApplicationController();

        //Toast.makeText(getActivity().getApplicationContext(),getActivity().getIntent().getStringExtra("orderId"),Toast.LENGTH_SHORT).show();
        custCode = getArguments().getString("custCode");
        if (getArguments().getString("orderId") != null) {
            orderId = Integer.parseInt(getArguments().getString("orderId"));
        } else if (getActivity().getIntent().getStringExtra("orderId") != null) {
            orderId = Integer.parseInt(getActivity().getIntent().getStringExtra("orderId"));
        } else {
            orderId = -1; //order id to be set -1 for the first item of the order => order is created only
            //after the first item is selected

        }
        productSearchBox = view.findViewById(R.id.et_productSearch);

        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();
        productList = stDatabase.stDao().getEnabledProducts(false);
        //updating prices based on the default price list
        Customer customer = stDatabase.stDao().getCustomerbyCustomerCode(custCode);
        String priceList = customer.getPrice_list();
        //ac.makeShortToast(getActivity(),"went through this " + priceList);

        List<Price> prices = stDatabase.stDao().getPricesByPriceList(priceList);
        getActivity().getIntent().putExtra("priceList","Standard Selling");
        if (priceList != null) {
            getActivity().getIntent().putExtra("priceList",customer.getPrice_list());
            getActivity().getIntent().putExtra("territory",customer.getTerritory());


            for (Product product : productList) {
                //setting the price of all items to 0 before updating prices from price list
                product.setProductRate(0);
                stDatabase.stDao().updateProduct(product);
                int indexOfProduct = productList.indexOf(product);
                productList.set(indexOfProduct, product);

            }


            for (Price p : prices) {
                //updating prices of all items from the customer price list
                String prodCodeFromPrices = p.getProductCode();
                for (Product product : productList) {
                    String prodCode = product.getProductCode();
                    Log.d(TAG, "onCreateView: prodCode");
                    if (prodCodeFromPrices.equals(prodCode)) {
                        int indexOfProduct = productList.indexOf(product);
                        product.setProductRate(p.getPrice());
                        //check later
                        stDatabase.stDao().updateProduct(product);
                        productList.set(indexOfProduct, product);
                        Log.d(TAG, "onCreateView: SettingPrice" + product.getProductName() + "|" + product.getProductRate());
                    }
                }
            }

        }


        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.product_recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        productList = sortProductList(productList);


        listener = this;
        final ProductAdapter productAdapter = new ProductAdapter(listener, productList, getActivity());
        recyclerView.setAdapter(productAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        productAdapter.notifyDataSetChanged();


        productSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //auto-generated - I'm assuming this is needed

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // fires up when there is any change in the search text box
                List<Product> filtered = new ArrayList<>();
                searchableProductList = stDatabase.stDao().getProduct();
                for (Product product : searchableProductList) {
                    String searchString = product.getProductName().toLowerCase();
                    if (searchString.contains(productSearchBox.getText().toString().toLowerCase())) {
                        filtered.add(product);
                    }
                }

                productList = sortProductList(filtered);
                final ProductAdapter productAdapter = new ProductAdapter(listener, productList, getActivity());
                recyclerView.setAdapter(productAdapter);
                productAdapter.notifyDataSetChanged();


            }

            @Override
            public void afterTextChanged(Editable s) {
                //auto-generated - I'm assuming this is needed

            }
        });

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //get the order id from the dialogfragment
        orderId = Integer.parseInt(data.getStringExtra("orderId"));
        getActivity().getIntent().putExtra("orderId", String.valueOf(orderId));

        productSearchBox.selectAll();
    }


    @Override
    public void onItemClicked(View v, int position) {
        if (orderId == -1) {
            orderStatus = -1;
        } else {
            orderStatus = stDatabase.stDao().getOrderStatusByOrderId(orderId);
        }
        if (orderStatus == -1) {
            String prodCode = productList.get(position).getProductCode();
            double rate = productList.get(position).getProductRate();

            Bundle args = new Bundle();
            args.putString("prodCode", prodCode);
            args.putString("custCode", custCode);
            args.putString("orderId", String.valueOf(orderId));
            args.putDouble("rate", rate);


            DialogFragment_ProductRateInfo df = new DialogFragment_ProductRateInfo();
            df.setArguments(args);
            df.setCancelable(false);
            df.setTargetFragment(this, 1);


            df.show(getFragmentManager(), "Dialog");
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "This order cannot be editted." + orderStatus, Toast.LENGTH_SHORT).show();
        }

    }

    private List<Product> sortProductList(List<Product> sortList) {
        //sorts the list
        Collections.sort(sortList, new Comparator<Product>() {
            @Override
            public int compare(Product o1, Product o2) {
                return o1.getProductName().compareToIgnoreCase(o2.getProductName());
            }
        });
        return sortList;
    }


    /*protected void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);
        mListState = mLayoutmMlayoutManager.onSaveInstance();
    }*/


}
