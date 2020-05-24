package com.example.RvOnclick.AllFragements;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.RvOnclick.ApplicationController;
import com.example.RvOnclick.BackgroundTasks.AsyncStockUpdater;
import com.example.RvOnclick.Brand;
import com.example.RvOnclick.Customer;
import com.example.RvOnclick.Interfaces.IOnItemQtyAddedListener;
import com.example.RvOnclick.NetworkOperations.StockUpdater;
import com.example.RvOnclick.Price;
import com.example.RvOnclick.Product;
import com.example.RvOnclick.ProductsListItem;
import com.example.RvOnclick.R;
import com.example.RvOnclick.Rv1Adapter;
import com.example.RvOnclick.Rv1Item;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */

/**
 * RV1ITEM DESC
 * strAttr1 is Brand
 * strId is ProductCode
 */

public class ProductListFragment extends Fragment implements IOnItemQtyAddedListener,
        ApplicationController.OnPriceProcessedListener, Rv1Adapter.OnItemClickListener,
        Rv1Adapter.OnItemLongClickListener, AsyncStockUpdater.AsyncStockUpdateListener, StockUpdater.StockUpdateListener {


    public static StDatabase stDatabase;
    protected String custCode;
    public long prodListId, fragmentId;
    boolean isEditable;
    public List<Product> productMasters;
    private List<Rv1Item> searchableProductList = new ArrayList<>();
    private List<Rv1Item> rv1ItemList = new ArrayList<>();
    private List<Rv1Item> fullRv1ItemList = new ArrayList<>();
    Rv1Adapter productAdapter;
    RecyclerView recyclerView;
    private Rv1Adapter.OnItemClickListener listener;
    private Rv1Adapter.OnItemLongClickListener longClickListener;
    private StockUpdater.StockUpdateListener stockUpdateListener;
    private IOnItemQtyAddedListener qtyAddedListener;
    private ApplicationController.OnPriceProcessedListener priceProcessedListener;
    private AsyncStockUpdater.AsyncStockUpdateListener asyncStockUpdateListener;
    EditText productSearchBox;
    Spinner spBrandList;
    Button btShowItems;
    public static String TAG = "ProductListFragment";
    private String priceList, docType, docTypeFor;
    List<Brand> brands;
    String warehouse, stockBalanceReport, fromWarehouse;
    AsyncStockUpdater.StockBalanceUpdater stockUpdateAsyncTask;


    public ProductListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);
        spBrandList = view.findViewById(R.id.sp_brandListpl);
        btShowItems = view.findViewById(R.id.bt_prodListShowItems);

        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();
        ApplicationController ac = new ApplicationController();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        stockBalanceReport = "";
        warehouse = sharedPreferences.getString("default_warehouse", "Stores - "
                + stDatabase.stDao().getAbbrByCompanyName(stDatabase.stDao().
                getDefaultCompany().getCompanyName()));

        brands = ac.sortBrandList(stDatabase.stDao().getAllBrands());
        String[] brandArray = new String[brands.size() + 1];

        //adding 'all' as the first element of the dropdown list
        brandArray[0] = "All";

        //creating array adapter for spinner
        int bc = 1;
        for (Brand brand : brands) {
            brandArray[bc] = brand.getBrandName();
            bc++;
        }
        //populating BrandSpinner
        ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, brandArray);
        spBrandList.setAdapter(brandAdapter);


        fragmentId = this.getId();
        stDatabase.stDao().resetCurrentOrderQty();

        //custCode = getArguments().getString("custCode");
        Intent intent = getActivity().getIntent();
        docType = intent.getStringExtra("docType");
        docTypeFor = intent.getStringExtra("docTypeFor");
        fromWarehouse = intent.getStringExtra(Utils.KEY_FROM_WAREHOUSE);
        String strProductListId = intent.getStringExtra(Utils.KEY_PRODUCT_LIST_ID);
        if (intent.getStringExtra(Utils.KEY_PRODUCT_LIST_ID) != null) {
            prodListId = Long.parseLong(getActivity().getIntent().getStringExtra(Utils.KEY_PRODUCT_LIST_ID));
            //prodListId = Integer.parseInt(getActivity().getIntent().getStringExtra("prodListId"));
        } else {
            prodListId = -1; //ProductsList id to be set -1 for the first item of the list => list is created only
            //after the first item is selected

        }
        productSearchBox = view.findViewById(R.id.et_productSearchpl);
        productSearchBox.requestFocus();
        productMasters = stDatabase.stDao().getEnabledProducts(false);
        rv1ItemList = createItemListForRV(productMasters);

        //updating prices based on the default price list
        if (!docType.equals(Utils.STOCK_ENTRY)) {
            Customer customer = stDatabase.stDao().getCustomerbyCustomerCode(custCode);
            priceList = customer.getPrice_list();
            if (priceList.equals("null")) {
                priceList = "Standard Selling";
            }

            getActivity().getIntent().putExtra("priceList", "Standard Selling");
            if (priceList != null) {
                getActivity().getIntent().putExtra("priceList", priceList);
                getActivity().getIntent().putExtra("territory", customer.getTerritory());

                priceProcessedListener = this;

                new ProductListFragment.UpdatePrices(rv1ItemList, priceList, priceProcessedListener).execute();
            }
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.product_recyclerViewpl);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<Rv1Item> sortedRv1ItemList = sortRv1Items(rv1ItemList);
        rv1ItemList.clear();
        rv1ItemList.addAll(sortedRv1ItemList);

        qtyAddedListener = this;
        listener = this;
        longClickListener = this;
        productAdapter = new Rv1Adapter(listener, longClickListener, rv1ItemList, getActivity());
        recyclerView.setAdapter(productAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //productAdapter.notifyDataSetChanged();
        if (prodListId != -1) {
            new ProductListFragment.UpdateCurrentOrderQty(rv1ItemList).execute();
        }


        productSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //auto-generated - I'm assuming this is needed

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // fires up when there is any change in the search text box
                filterProducts();

            }

            @Override
            public void afterTextChanged(Editable s) {
                //auto-generated - I'm assuming this is needed

            }
        });


        spBrandList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: spBrandList: BrandListSpinner: " +
                        spBrandList.getSelectedItem().toString());
                filterProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btShowItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListItemFragment listItemFragment = new ListItemFragment();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragement_container, listItemFragment, Utils.LIST_ITEM_FRAGMENT);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        asyncStockUpdateListener = this;
        stockUpdateListener = this;
        String currentDate = ac.createTimeStamp("yyyy-MM-dd");
        new StockUpdater().UpdateStock(getActivity(), stDatabase, 1, stockUpdateListener,
                currentDate, currentDate, fromWarehouse);

        return view;
    }

    private List<Rv1Item> createItemListForRV(List<Product> prodList) {
        //rv1ItemList.clear();
        List<Rv1Item> rItemList = new ArrayList<>();
        for (Product p : prodList) {
            Rv1Item item = new Rv1Item();
            String pName = p.getProductName();
            String pCode = p.getProductCode();
            String pBrand = p.getProductBrand();
            //Customer c = stDatabase.stDao().getCustomerbyCustomerCode(cCode);
            //String customerName = stDatabase.stDao().getCustomerbyCustomerCode(payment.getCustomerCode()).getCustomer_name();
            item.setHeading(pName);

            item.setStrId(pCode);
            item.setStrAttr1(pBrand);
            /*
            item.setInfo2(payment.getInvoiceNo());
            item.setInfo4(payment.getPaymentAmt().toString());
            item.setIntId(payment.getPaymentId().intValue());
            item.setInfo3(String.valueOf(payment.getPaymentStatus()));
            */
            rItemList.add(item);

        }
        fullRv1ItemList.clear();
        fullRv1ItemList.addAll(rItemList);
        return rItemList;


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //get the order id from the dialogfragment
        prodListId = data.getLongExtra("productListId", -1);
        getActivity().getIntent().putExtra(Utils.KEY_PRODUCT_LIST_ID, prodListId);

        productSearchBox.selectAll();
        if (prodListId != -1) {
            new UpdateCurrentOrderQty(rv1ItemList).execute();
        }
    }


    @Override
    public void onItemClicked(View v, int position) {
        if (prodListId == -1) {
            isEditable = true;
        } else {
            isEditable = stDatabase.stDao().isProductListEditable(prodListId);

        }
        if (isEditable) {
            String prodCode = rv1ItemList.get(position).getStrId();
            getActivity().getIntent().putExtra(Utils.KEY_WAREHOUSE, warehouse);
            getActivity().getIntent().putExtra("prodCode", prodCode);
            //double rate = Double.parseDouble(rv1ItemList.get(position).getInfo4());

/*
            Bundle args = new Bundle();
            args.putString("prodCode", prodCode);
            args.putString("docTypeFor", docTypeFor);
            args.putString("productListId", String.valueOf(prodListId));
            args.putDouble("rate", rate);
            args.putString("warehouse",warehouse);

*/

            AddProductDialogFragment df = new AddProductDialogFragment();
            //df.setArguments(args);
            //df.setCancelable(false);
            df.setTargetFragment(this, 1);


            df.show(getFragmentManager(), Utils.ADD_PRODUCT_DIALOG_FRAGMENT);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "This List cannot be editted.", Toast.LENGTH_SHORT).show();
        }

    }

    private List<Rv1Item> sortRv1Items(List<Rv1Item> sortList) {
        //sorts the list
        Collections.sort(sortList, new Comparator<Rv1Item>() {
            @Override
            public int compare(Rv1Item o1, Rv1Item o2) {
                return o1.getHeading().compareToIgnoreCase(o2.getHeading());
            }
        });
        return sortList;
    }


    @Override
    public void onItemQtyAdded(Rv1Item rItem, int position) {
        rv1ItemList.remove(position);
        rv1ItemList.add(position, rItem);
        productAdapter.notifyItemChanged(position);
    }

    @Override
    public void onPriceProcessed(final int position, List<Product> productListFromAsyncTask) {
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
        } catch (NullPointerException e) {
            Log.d(TAG, "onPriceProcessed: callBack" + e.toString());
        }


    }

    @Override
    public void onItemLongClicked(View view, int position) {

    }

    @Override
    public void onStockUpdatedAsync(String[] values) {

        int positionOfProd = Integer.valueOf(values[0]);
        double availableQty = Double.valueOf(values[1]);
        Log.d(TAG, "onStockUpdatedAsync: values: " + positionOfProd + ": " + availableQty + ": " + rv1ItemList.get(positionOfProd).getHeading());
        try {


            rv1ItemList.get(positionOfProd).setInfo2(String.valueOf(availableQty));
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "onStockUpdatedAsync: onStockUpdatedAsync: IndexOutOfBounds Exception: " + e.toString());
        }
        int positionInFullList = fullRv1ItemList.indexOf(rv1ItemList.get(positionOfProd));
        fullRv1ItemList.set(positionInFullList, rv1ItemList.get(positionOfProd));
        //fullRv1ItemList.get(rv1ItemList.indexOf(rv1ItemList.get(positionOfProd))).setInfo2(String.valueOf(availableQty));
        productAdapter.notifyItemChanged(positionOfProd);
    }

    @Override
    public void onStockUpdated(int stockUpdated, int requestCode, int resultCode, String info) {
        Log.d(TAG, "onStockUpdated: Stock Balance Report: " + info);
        stockBalanceReport = info;
        AsyncStockUpdater asyncStockUpdater = new AsyncStockUpdater(getActivity(), rv1ItemList,
                null, Utils.STOCK_BAL_RVITEM, info, asyncStockUpdateListener);

        stockUpdateAsyncTask = asyncStockUpdater.new StockBalanceUpdater();
        stockUpdateAsyncTask.execute();


    }


    private class UpdateCurrentOrderQty extends AsyncTask<String, Integer, String> {
        List<Rv1Item> at1Rv1ItemList;
        int positionOfProduct;

        private UpdateCurrentOrderQty(List<Rv1Item> at1Rv1ItemList) {
            this.at1Rv1ItemList = at1Rv1ItemList;
        }

        @Override
        protected String doInBackground(String... strings) {
            //int positionOfProduct;
            List<ProductsListItem> productsListItems = stDatabase.stDao().getProductsListItemByProductsListId(prodListId);
            String info2Value;
            outerMostLoop:
            for (ProductsListItem op : productsListItems) {
                String orderProdCode = op.getProductCode();
                Log.d(TAG, "doInBackground: updateCurrentOrderQty: orderProdCode: " + orderProdCode);
                for (Rv1Item rItem : at1Rv1ItemList) {
                    String prodCode = rItem.getStrId();
                    Log.d(TAG, "doInBackground: updateCurrentOrderQty: prodCode: " + prodCode);
                    if (orderProdCode.equals(prodCode) && op.getParentId() == null) {
                        //child item will be skipped from looping once again through the product list
                        //child item will loop through the order item to get freeQty and update the item
                        double qty = Double.valueOf(op.getQty());
                        info2Value = String.valueOf(qty);
                        positionOfProduct = at1Rv1ItemList.indexOf(rItem);
                        Log.d(TAG, "doInBackground: updateCurrentOrderQty: orderProdCode equals prodCode: " + orderProdCode);
                        Log.d(TAG, "doInBackground: updateCurrentOrderQty: qty: " + qty + " info2Value: " + info2Value + " positionOfProduct: " + positionOfProduct);
                        //rItem.setCurrentOrderQty(qty);
                        //stDatabase.stDao().updateProduct(product);
                        if (op.getChildId() != null) {
                            for (ProductsListItem innerOp : productsListItems) {
                                //looping through items in Order to get the free item and qty
                                if (op.getChildId() == innerOp.getProductsListItemId()) {
                                    double freeQty = innerOp.getQty();
                                    //product.setCurrentOrderFreeQty(freeQty);
                                    info2Value = info2Value + "(" + freeQty + ")";

                                    break;
                                    //stDatabase.stDao().updateProduct(product);

                                }
                            }

                        }
                        rItem.setInfo4(info2Value);
                        rv1ItemList.set(positionOfProduct, rItem);
                        int x = searchableProductList.indexOf(rItem);
                        fullRv1ItemList.set(x, rItem);

                        //rv1ItemList.remove(positionOfProduct);
                        //rv1ItemList.add(positionOfProduct, rItem);

                        //itemProcessedListener2.onItemProcessed(product,positionOfProduct);
                        if (isCancelled()) {
                            Log.d(TAG, "doInBackground: updateCurrentOrderQty: beaking outermost loop");
                            break outerMostLoop;
                        }
                        publishProgress(positionOfProduct);
                        /*
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onProgressUpdate(positionOfProduct);
                                }
                            });
                            //onProgressUpdate(positionOfProduct);
                        } catch (NullPointerException e) {
                            Log.d(TAG, "doInBackground: calling onProgressUpdate");
                            e.printStackTrace();
                        }*/
                        //onProgressUpdate(positionOfProduct);
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
            } catch (NullPointerException e) {
                Log.d(TAG, "onProgressUpdate: " + e.toString());
                e.printStackTrace();
            }


        }
    }

    private static class UpdatePrices extends AsyncTask<String, Integer, Void> {
        //this class updates prices for each item from the default price list for the selected customer
        //default price list is from erp
        List<Rv1Item> at2Rv1ItemList = new ArrayList<>();
        String priceList;
        ApplicationController.OnPriceProcessedListener priceProcessedListener;

        UpdatePrices(List<Rv1Item> at2Rv1ItemList, String priceList, ApplicationController.OnPriceProcessedListener priceProcessedListener) {
            this.at2Rv1ItemList.addAll(at2Rv1ItemList);
            this.priceList = priceList;
            this.priceProcessedListener = priceProcessedListener;
        }

        @Override
        protected Void doInBackground(String... strings) {
            List<Price> prices = stDatabase.stDao().getPricesByPriceList(priceList);
            if (priceList != null) {

                //depracating
                //stDatabase.stDao().resetProductRate();

                for (Price p : prices) {
                    //updating prices of all items from the customer price list
                    String prodCodeFromPrices = p.getProductCode();
                    for (Rv1Item rItem : at2Rv1ItemList) {
                        Rv1Item updatedRv1Item;
                        String prodCode = rItem.getStrId();
                        Log.d(TAG, "onCreateView: prodCode");
                        if (prodCodeFromPrices.equals(prodCode)) {
                            int indexOfProduct = at2Rv1ItemList.indexOf(rItem);
                            updatedRv1Item = rItem;
                            updatedRv1Item.setInfo4(String.valueOf(p.getPrice()));
                            //check later
                            //stDatabase.stDao().updateProduct(updatedProduct);
                            //at2Rv1ItemList.set(indexOfProduct, product);
                            Log.d(TAG, "onCreateView: SettingPrice" + rItem.getHeading() + "|" + rItem.getInfo4());
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
            priceProcessedListener.onPriceProcessed(values[0], null);
        }
    }

    private void filterProducts() {
        try {
            stockUpdateAsyncTask.cancel(true);
            Log.d(TAG, "filterProducts: stockUpdateAsyncTask cancelled");
        } catch (NullPointerException e) {
            Log.d(TAG, "filterProducts: stockUpdateAsyncTask is null");
        }
        List<Rv1Item> filtered = new ArrayList<>();
        searchableProductList.clear();
        searchableProductList.addAll(fullRv1ItemList);
        for (Rv1Item rItem : searchableProductList) {
            String searchString = rItem.getHeading().toLowerCase();
            if (searchString.contains(productSearchBox.getText().toString().toLowerCase())) {
                if (spBrandList.getSelectedItemId() != 0) {
                    String brandName = spBrandList.getSelectedItem().toString();
                    if (rItem.getStrAttr1().equals(brandName)) {
                        filtered.add(rItem);
                    }
                } else filtered.add(rItem);
            }
        }

        rv1ItemList.clear();
        rv1ItemList.addAll(sortRv1Items(filtered));
        if (!stockBalanceReport.equals("")) {
            //as items are filtered the list size changes hence throws IndexOutOfBoundsException

            AsyncStockUpdater asyncStockUpdater = new AsyncStockUpdater(getActivity(), rv1ItemList,
                    null, Utils.STOCK_BAL_RVITEM, stockBalanceReport, asyncStockUpdateListener);
            Log.d(TAG, "filterProducts: starting stockUpdateAsyncTask");
            stockUpdateAsyncTask = asyncStockUpdater.new StockBalanceUpdater();
            stockUpdateAsyncTask.execute();
        }
        productAdapter.notifyDataSetChanged();
    }


    @Override
    public void onResume() {
        //Log.d(TAG, "onResume: prodListId: "+getActivity().getIntent().getLongExtra(Utils.KEY_PRODUCT_LIST_ID,Long.valueOf(-1)));
        Integer intProdListId = getActivity().getIntent().getIntExtra(Utils.KEY_PRODUCT_LIST_ID, -1);
        Log.d(TAG, "onResume: resuming");
        if (intProdListId != -1) {
            prodListId = Long.valueOf(getActivity().getIntent().getStringExtra(Utils.KEY_PRODUCT_LIST_ID));

            productSearchBox.selectAll();
            if (prodListId != 0) {
                Log.d(TAG, "onResume: updateCurrentOrderQty executing");
                new UpdateCurrentOrderQty(rv1ItemList).execute();
            }
        }


        super.onResume();
    }

    @Override
    public void onPause() {
        stockUpdateAsyncTask.cancel(true);
        super.onPause();
    }

}
