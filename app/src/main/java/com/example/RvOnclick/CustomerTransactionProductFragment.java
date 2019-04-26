package com.example.RvOnclick;


import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
public class CustomerTransactionProductFragment extends Fragment implements
        ProductAdapter.OnItemClickListener, OnItemProcessedListener,
ApplicationController.OnPriceProcessedListener{


    public static StDatabase stDatabase;
    protected String custCode;
    public int orderId, orderStatus, fragmentId;
    public List<Product> productList, searchableProductList;
    ProductAdapter productAdapter;
    RecyclerView recyclerView;
    private ProductAdapter.OnItemClickListener listener;
    private OnItemProcessedListener itemProcessedListener;
    private ApplicationController.OnPriceProcessedListener priceProcessedListener;
    EditText productSearchBox;
    ProgressDialog progressDialog;
    public static String TAG = "CustomerTransactionFragment";
    private String priceList;

    public CustomerTransactionProductFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_transaction_product, container, false);
        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();
        ApplicationController ac = new ApplicationController();
        fragmentId=this.getId();
        stDatabase.stDao().resetCurrentOrderQty();

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


        productList = stDatabase.stDao().getEnabledProducts(false);
        //updating prices based on the default price list
        Customer customer = stDatabase.stDao().getCustomerbyCustomerCode(custCode);
        priceList = customer.getPrice_list();
        if (priceList.equals("null")) {
            priceList = "Standard Selling";
        }

        //List<Price> prices = stDatabase.stDao().getPricesByPriceList(priceList);
        getActivity().getIntent().putExtra("priceList", "Standard Selling");
        if (priceList != null) {
            getActivity().getIntent().putExtra("priceList", priceList);
            getActivity().getIntent().putExtra("territory", customer.getTerritory());

            priceProcessedListener = this;
            new UpdatePrices(productList,priceList,priceProcessedListener).execute();


            /*stDatabase.stDao().resetProductRate();

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
                        break;
                    }
                }
            }*/

        }


        recyclerView = (RecyclerView) view.findViewById(R.id.product_recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        productList = sortProductList(productList);

        itemProcessedListener = this;
        listener = this;
        productAdapter = new ProductAdapter(listener, productList, getActivity());
        recyclerView.setAdapter(productAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        productAdapter.notifyDataSetChanged();
        if (orderId != -1) {
            new UpdateCurrentOrderQty(productList).execute();
        }

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

                productList.clear();
                productList.addAll(sortProductList(filtered));
                //productList = sortProductList(filtered);
                //final ProductAdapter productAdapter = new ProductAdapter(listener, productList, getActivity());
                //recyclerView.setAdapter(productAdapter);
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
        if (orderId != -1) {
            new UpdateCurrentOrderQty(productList).execute();
        }
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


    @Override
    public void onItemProcessed(Product product, int position) {
        productList.remove(position);
        productList.add(position, product);
        productAdapter.notifyItemChanged(position);
    }

    @Override
    public void onPriceProcessed(final int position) {
        try {
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    // Stuff that updates the UI

                    //productList.remove(position);
                    //productList.add(position, product);
                    productAdapter.notifyItemChanged(position);

                }
            });
        } catch (NullPointerException e){
            Log.d(TAG, "onPriceProcessed: callBack" + e.toString());
        }


    }


    private class UpdateCurrentOrderQty extends AsyncTask<String, Integer, String> {
        List<Product> at1ProductList;

        public UpdateCurrentOrderQty(List<Product> at1ProductList) {
            this.at1ProductList = at1ProductList;
        }

        @Override
        protected String doInBackground(String... strings) {

            List<OrderProduct> orderProducts = stDatabase.stDao().getOrderProductsById(orderId);
            for (OrderProduct op : orderProducts) {
                String orderProdCode = op.getProductCode();
                for (Product product : at1ProductList) {
                    String prodCode = product.getProductCode();
                    if (orderProdCode.equals(prodCode) && op.getParentId() == 0) {
                        //child item will be skipped from looping once again through the product list
                        //child item will loop through the order item to get freeQty and update the item
                        double qty = op.getQty();
                        int positionOfProduct = at1ProductList.indexOf(product);
                        product.setCurrentOrderQty(qty);
                        stDatabase.stDao().updateProduct(product);
                        if (op.getChildId() != 0) {
                            for (OrderProduct innerOp : orderProducts) {
                                //looping through items in order to get the free item and qty
                                if (op.getChildId() == innerOp.getOrderProductId()) {
                                    double freeQty = innerOp.getQty();
                                    product.setCurrentOrderFreeQty(freeQty);
                                    at1ProductList.remove(positionOfProduct);
                                    at1ProductList.add(positionOfProduct, product);
                                    stDatabase.stDao().updateProduct(product);

                                }
                            }
                        }
                        //itemProcessedListener2.onItemProcessed(product,positionOfProduct);
                        onProgressUpdate(positionOfProduct);
                        break;
                    }
                }

            }


            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            try {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Stuff that updates the UI
                        productAdapter.notifyItemChanged(values[0]);

                    }
                });
            } catch (NullPointerException e){
                Log.d(TAG, "onProgressUpdate: " + e.toString());
                e.printStackTrace();
            }


        }
    }

    private static class UpdatePrices extends AsyncTask<String,Integer,Void>{
        List<Product> at2ProductList=new ArrayList<>();
        String priceList;
        ApplicationController.OnPriceProcessedListener priceProcessedListener;
        UpdatePrices(List<Product> at2ProductList, String priceList, ApplicationController.OnPriceProcessedListener priceProcessedListener) {
            this.at2ProductList.addAll(at2ProductList);
            this.priceList = priceList;
            this.priceProcessedListener=priceProcessedListener;
        }

        @Override
        protected Void doInBackground(String... strings) {
            List<Price> prices = stDatabase.stDao().getPricesByPriceList(priceList);
            //getActivity().getIntent().putExtra("priceList", "Standard Selling");
            if (priceList != null) {
                //getActivity().getIntent().putExtra("priceList", priceList);
                //getActivity().getIntent().putExtra("territory", customer.getTerritory());


                stDatabase.stDao().resetProductRate();

                for (Price p : prices) {
                    //updating prices of all items from the customer price list
                    String prodCodeFromPrices = p.getProductCode();
                    for (Product product : at2ProductList) {
                        Product updatedProduct;
                        String prodCode = product.getProductCode();
                        Log.d(TAG, "onCreateView: prodCode");
                        if (prodCodeFromPrices.equals(prodCode)) {
                            int indexOfProduct = at2ProductList.indexOf(product);
                            updatedProduct = product;
                            updatedProduct.setProductRate(p.getPrice());
                            //check later
                            stDatabase.stDao().updateProduct(updatedProduct);
                            //at2ProductList.set(indexOfProduct, product);
                            Log.d(TAG, "onCreateView: SettingPrice" + product.getProductName() + "|" + product.getProductRate());
                            //if ()
                            publishProgress(indexOfProduct);
                            break;
                        }
                    }
                }

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            priceProcessedListener.onPriceProcessed(values[0]);
            /* try {
                .runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Stuff that updates the UI
                        productAdapter.notifyItemChanged(values[0]);

                    }
                });
            } catch (NullPointerException e){
                Log.d(TAG, "onProgressUpdate: "+e.toString());
                e.printStackTrace();
            }*/
        }
    }

}
