package com.example.RvOnclick.AllFragements;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.RvOnclick.Dialogs.ConfirmDialog;
import com.example.RvOnclick.Interfaces.IOnItemQtyAddedListener;
import com.example.RvOnclick.NetworkOperations.Login;
import com.example.RvOnclick.NetworkOperations.StockEntryCreator;
import com.example.RvOnclick.ProductsList;
import com.example.RvOnclick.ProductsListItem;
import com.example.RvOnclick.R;
import com.example.RvOnclick.Rv1Adapter;
import com.example.RvOnclick.Rv1Item;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.StockEntry;
import com.example.RvOnclick.UserConfig;
import com.example.RvOnclick.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListItemFragment extends Fragment implements
        Rv1Adapter.OnItemClickListener, Rv1Adapter.OnItemLongClickListener, ConfirmDialog.OnCancelledListener,
        ConfirmDialog.OnConfirmationListener, StockEntryCreator.StockEntryPostedListener, Login.ILogin {

    private static final String TAG = "ListItemFragment";
    StDatabase stDatabase;
    RecyclerView recyclerView;
    Rv1Adapter rv1Adapter;
    IOnItemQtyAddedListener qtyAddedListener;
    Rv1Adapter.OnItemClickListener onItemClickListener;
    Rv1Adapter.OnItemLongClickListener onItemLongClickListener;
    ConfirmDialog.OnConfirmationListener confirmationListener;
    ConfirmDialog.OnCancelledListener cancelledListener;
    StockEntryCreator.StockEntryPostedListener stockEntryPostedListener;
    Login.ILogin loginListener;
    Long productListId, docId;
    boolean isEditable;
    List<Rv1Item> rv1ItemList = new ArrayList<>();
    String warehouse;
    ProductsList productsList;
    String docType;
    int clickedPosition;
    Intent intent;
    Button btPost;
    RelativeLayout rlProgressBar;
    StockEntry stockEntry;


    public ListItemFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_item, container, false);

        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();

        onItemClickListener = this;
        onItemLongClickListener = this;
        confirmationListener = this;
        cancelledListener = this;
        stockEntryPostedListener = this;
        loginListener = this;

        intent = getActivity().getIntent();
        productListId = intent.getLongExtra(Utils.KEY_PRODUCT_LIST_ID, -1);
        docId = intent.getLongExtra(Utils.KEY_DOC_ID, -1);
        productsList = stDatabase.stDao().getPrductsListById(productListId);
        List<ProductsListItem> listItems = stDatabase.stDao().getProductsListItemByProductsListId(productListId);
        try {
            warehouse = productsList.getWarehouse();
            isEditable = productsList.isEditable();
            docType = productsList.getDocType();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, "onCreateView: productsList is null");
        }
        btPost = view.findViewById(R.id.bt_listItemPost);
        rlProgressBar = view.findViewById(R.id.rl_listItemPb);

        if (docType.equals(Utils.STOCK_ENTRY)) {
            btPost.setText("Transfer");
        }

        btPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StockEntryCreator stockEntryCreator = new StockEntryCreator();
                stockEntry = stDatabase.stDao().getStockEntryById(docId);
                rlProgressBar.setVisibility(View.VISIBLE);
                stockEntryCreator.postStockEntry(getActivity(), stDatabase,
                        Utils.REQ_STOCK_TRANSFER_FROM_LISTITEMFRAGMENT, stockEntryPostedListener,
                        stockEntry, Utils.STOCK_MATERIAL_TRANSFER);
            }
        });


        createItemListForRV(listItems);
        recyclerView = view.findViewById(R.id.rv_listItem);
        rv1Adapter = new Rv1Adapter(onItemClickListener, onItemLongClickListener, rv1ItemList, getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(rv1Adapter);

        return view;
    }

    private void createItemListForRV(List<ProductsListItem> itemList) {
        rv1ItemList.clear();
        if (productListId != -1) {
            for (ProductsListItem item : itemList) {
                Rv1Item rv1Item = new Rv1Item();
                rv1Item.setHeading(item.getProductName());
                rv1Item.setStrId(item.getProductCode());
                rv1Item.setInfo4(String.valueOf(item.getQty()));
                rv1Item.setLongId(item.getProductsListItemId());

                switch (docType) {
                    case Utils.STOCK_ENTRY:
                        rv1Item.setInfo2(item.getWarehouse());
                        break;
                }
                rv1ItemList.add(rv1Item);
            }
        }
    }

    @Override
    public void onItemClicked(View view, int position) {
        if (isEditable) {
            Intent intent = getActivity().getIntent();
            intent.putExtra("prodCode", rv1ItemList.get(position).getStrId());
            intent.putExtra(Utils.KEY_PREVIOUS_QTY, rv1ItemList.get(position).getInfo4());
            intent.putExtra(Utils.KEY_LIST_ITEM_ID, rv1ItemList.get(position).getLongId());
            AddProductDialogFragment df = new AddProductDialogFragment();
            df.setTargetFragment(this, 2);
            df.show(getFragmentManager(), Utils.ADD_PRODUCT_DIALOG_FRAGMENT);
        } else {
            Toast.makeText(getActivity(), "This doesn't seem to be editable", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemLongClicked(View view, int position) {
        if (isEditable) {
            String dialogTitle = "Delete Item";
            String dialogMessage = "This action will delete the selected Item. Do you want to continue";
            clickedPosition = position;
            new ConfirmDialog().showConfirmationDialog(getActivity(), dialogTitle, dialogMessage,
                    "Confirm", "Cancel", 1, confirmationListener, cancelledListener);


        }
    }

    @Override
    public void onConfirm(int requestCode) {
        Rv1Item rv1Item = rv1ItemList.get(clickedPosition);
        ProductsListItem item = stDatabase.stDao().getProductsListItemById(rv1Item.getLongId());
        Long childId = item.getChildId();
        Long parentId = item.getParentId();

        if (requestCode == 1) {
            //from long click of item
            if (childId != null) {
                Log.d(TAG, "onConfirm: rquestCode1, child id not null");
                ProductsListItem childItem = stDatabase.stDao().getProductsListItemById(childId);
                stDatabase.stDao().deleteProductsListItem(childItem);
                stDatabase.stDao().deleteProductsListItem(item);
                rv1ItemList.remove(clickedPosition);
                refreshRecyclerView();
            } else if (parentId != null) {
                new ConfirmDialog().showConfirmationDialog(getActivity(), "Item linked",
                        "Also Delete parent item?", "Yes",
                        "No", 2, confirmationListener, cancelledListener);
            } else {
                stDatabase.stDao().deleteProductsListItem(item);
                rv1ItemList.remove(clickedPosition);
                rv1Adapter.notifyItemRemoved(clickedPosition);
            }

        }
        if (requestCode == 2) {
            //from the previous if block - confirmation for deleting parent item.
            ProductsListItem parentItem = stDatabase.stDao().getProductsListItemById(parentId);
            stDatabase.stDao().deleteProductsListItem(parentItem);
            stDatabase.stDao().deleteProductsListItem(item);
        }

    }

    @Override
    public void onCancelled(int requestCode) {

        if (requestCode == 2) {
            Rv1Item rv1Item = rv1ItemList.get(clickedPosition);
            ProductsListItem item = stDatabase.stDao().getProductsListItemById(rv1Item.getLongId());
            Long parentId = item.getParentId();
            ProductsListItem parentItem = stDatabase.stDao().getProductsListItemById(parentId);
            stDatabase.stDao().deleteProductsListItem();
            parentItem.setChildId(null);
            stDatabase.stDao().updateProductsListItem(parentItem);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: before if block");
        if (requestCode == 2) {
            refreshRecyclerView();
        }
    }

    private void refreshRecyclerView() {
        List<ProductsListItem> itemList = stDatabase.stDao().getProductsListItemByProductsListId(productListId);
        rv1ItemList.clear();
        createItemListForRV(itemList);
        rv1Adapter.notifyDataSetChanged();

    }

    private void deleteListIfEmpty() {
        int noOfItemsInList = stDatabase.stDao().countProductsListItemByProductsListId(productListId);
        if (noOfItemsInList == 0) {
            stDatabase.stDao().deleteProductsList(productsList);
            if (docType.equals(Utils.STOCK_ENTRY)) {
                stDatabase.stDao().deleteStockEntry(stDatabase.stDao().getStockEntryById(docId));
                intent.putExtra(Utils.KEY_DOC_ID, -1);
                intent.putExtra(Utils.KEY_PRODUCT_LIST_ID, -1);
            }
        }
    }

    @Override
    public void onStockEntryPosted(int synced, int requestCode, int resultCode, String info) {
        Log.d(TAG, "onStockEntryPosted: info: " + info);
        if (info.equals(Utils.VOLLEY_ERROR_AUTH_FAILURE)) {
            UserConfig uc = stDatabase.stDao().getAllUserConfig().get(0);
            String url = uc.getLoginUrl();
            String email = uc.getUserId();
            String password = uc.getPassword();

            Login login = new Login();
            Log.d(TAG, "onStockEntryPosted: AuthFailureError-should try to login again");
            login.login(getActivity(), stDatabase, Utils.REQ_LOGIN_FROM_LISTITEMFRAGMENT, loginListener, url, email, password);

        } else if (synced == Utils.SUCCESS) {
            Log.d(TAG, "onStockEntryPosted: Login success");
            rlProgressBar.setVisibility(View.GONE);
        } else Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginResponseListener(boolean loggedIn, int requestCode, int resultCode, String info) {
        Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
        StockEntryCreator stockEntryCreator = new StockEntryCreator();
        if (loggedIn) {
            Log.d(TAG, "onLoginResponseListener: Logged In after AuthFailureError");
            Log.d(TAG, "onLoginResponseListener: Should try to post Stock Entry again.");
            stockEntryCreator.postStockEntry(getActivity(), stDatabase, Utils.REQ_STOCK_TRANSFER_FROM_LISTITEMFRAGMENT, stockEntryPostedListener, stockEntry, Utils.STOCK_MATERIAL_TRANSFER);
        } else Toast.makeText(getActivity(), "Cannot Login", Toast.LENGTH_SHORT).show();
    }
}
