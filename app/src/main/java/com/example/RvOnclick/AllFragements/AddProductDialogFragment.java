package com.example.RvOnclick.AllFragements;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.RvOnclick.ApplicationController;
import com.example.RvOnclick.Product;
import com.example.RvOnclick.ProductsList;
import com.example.RvOnclick.ProductsListItem;
import com.example.RvOnclick.R;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.StockEntry;
import com.example.RvOnclick.Utils;
import com.example.RvOnclick.Warehouse;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddProductDialogFragment extends DialogFragment {

    TextView tv;
    Button btIncrease, btDecrease, btProductSave, btCancel;
    TextView tv_warehouse, tv_rate;
    EditText etQty, etFreeQty, etRate;
    Spinner spWarehouse, spQtyUom, spFreeQtyUom;
    private static StDatabase stDatabase;
    private String docTypeFor, doctType, qtyUom, freeQtyUom, warehouse, companyName, prodCode, listCompany;
    private long productListId, docId, listItemId;
    private double orderTotal, salesUomConversionFactor, qty, freeQty, rate;
    private Product product;
    ApplicationController ac = new ApplicationController();
    Intent intent;
    String transactionType;
    ProductsListItem listItem;


    public AddProductDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();
        //getting data from intent
        //orderid, custcode, pricelistname, territory
        intent = getActivity().getIntent();
        prodCode = intent.getStringExtra("prodCode");
        if (intent.getIntExtra(Utils.KEY_PRODUCT_LIST_ID, -1) == -1) {
            productListId = -1;
        } else {
            productListId = Long.valueOf(intent.getStringExtra(Utils.KEY_PRODUCT_LIST_ID));
        }

        listItemId = intent.getLongExtra(Utils.KEY_LIST_ITEM_ID, -1);
        if (listItemId != -1) {
            //checking if the item is child.
            //if the item is child then the item will be th parent
            //haven't worked on filling child item details in the dialog. wasn't necessary for time being
            Long parentId = stDatabase.stDao().getProductsListItemById(listItemId).getParentId();
            if (parentId != null) {
                listItem = stDatabase.stDao().getProductsListItemById(parentId);
            } else {
                listItem = stDatabase.stDao().getProductsListItemById(listItemId);
            }
        }
        listCompany = intent.getStringExtra("listCompany");
        docTypeFor = intent.getStringExtra("docTypeFor");
        doctType = intent.getStringExtra("docType");
        transactionType = intent.getStringExtra(Utils.KEY_TRANSACTION_TYPE);
        if (transactionType.equals(Utils.STOCK_MATERIAL_TRANSFER)) {
            warehouse = intent.getStringExtra(Utils.KEY_FROM_WAREHOUSE);
        } else warehouse = intent.getStringExtra(Utils.KEY_WAREHOUSE);


        companyName = ac.getCompanyName(prodCode, stDatabase);


        View view = inflater.inflate(R.layout.fragment_add_product_dialog, container, false);


        tv = view.findViewById(R.id.tv1pl);
        btCancel = view.findViewById(R.id.bt_cancelpl);
        btProductSave = view.findViewById(R.id.bt_productSavepl);
        btDecrease = view.findViewById(R.id.bt_decreasepl);
        btIncrease = view.findViewById(R.id.bt_increasepl);
        etQty = view.findViewById(R.id.et_qtypl);
        etFreeQty = view.findViewById(R.id.et_freeQtypl);
        spWarehouse = view.findViewById(R.id.sp_warehousepl);
        spQtyUom = view.findViewById(R.id.sp_qtyUom);
        spFreeQtyUom = view.findViewById(R.id.sp_freeQtyUom);
        etRate = view.findViewById(R.id.et_ratepl);
        tv_warehouse = view.findViewById(R.id.tv_addProductWarehouse);
        tv_rate = view.findViewById(R.id.tv_addProductRate);

        if (doctType.equals(Utils.STOCK_ENTRY)) {
            etRate.setVisibility(View.INVISIBLE);
            etFreeQty.setVisibility(View.INVISIBLE);
            spFreeQtyUom.setVisibility(View.INVISIBLE);
            spWarehouse.setVisibility(View.INVISIBLE);
            tv_rate.setVisibility(View.INVISIBLE);
            tv_warehouse.setVisibility(View.INVISIBLE);
        }


        //populating warehouse spinner
        List<Warehouse> warehouseList = stDatabase.stDao().getNonGroupWarehouseByCompany(companyName);
        String[] warehouseArray = new String[warehouseList.size()];
        int counter = 0;
        for (Warehouse wh : warehouseList) {
            warehouseArray[counter] = wh.getName();
            counter++;
        }

        ArrayAdapter<String> warehouseAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, warehouseArray);
        spWarehouse.setAdapter(warehouseAdapter);

        //populating Qty and FreeQty spinner
        product = stDatabase.stDao().getProductByProductCode(prodCode);
        tv.setText(product.getProductName());
        String[] salesUomArray = new String[2];
        salesUomArray[0] = product.getStockUom();
        salesUomArray[1] = product.getDefaultSalesUom();
        salesUomConversionFactor = product.getDefSalesUomConversion();

        ArrayAdapter<String> qtyUomAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, salesUomArray);
        spQtyUom.setAdapter(qtyUomAdapter);
        spFreeQtyUom.setAdapter(qtyUomAdapter);

        //setting value for warehouse spinner
        counter = 0;
        for (String wh : warehouseArray) {
            if (wh.equals(warehouse)) {
                break;
            }
            counter++;
        }
        spWarehouse.setSelection(counter);


        switch (getTargetRequestCode()) {
            case 1:
                etQty.setText("1.0");
                break;
            case 2:
                etQty.setText(String.valueOf(listItem.getQty()));
        }
        if (getTargetRequestCode() == 1) {
            etQty.setText("1.0");
        }
        etQty.selectAll();
        etQty.requestFocus();
        ac.showKeyboard(etQty, getActivity());


        btIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlusMinus(1);
            }
        });

        btDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlusMinus(-1);
            }
        });

        btProductSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warehouse = spWarehouse.getSelectedItem().toString();
                qty = Double.parseDouble(etQty.getText().toString());
                freeQty = 0;
                if (!etFreeQty.getText().toString().equals("")) {
                    freeQty = Double.parseDouble(etFreeQty.getText().toString());
                }
                if (doctType.equals(Utils.STOCK_ENTRY)) {
                    //stock entry does not require rate. valuation rate is autofilled in server
                    rate = 0;
                } else {
                    try {
                        rate = Double.parseDouble(etRate.getText().toString());
                    } catch (NullPointerException e) {
                        rate = 0;
                    }
                }
                if (qty > 0) {
                    createNewListIfNotExists();
                }
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean multipleItemsAllowed = sharedPreferences.getBoolean("allow_multiple_items", true);
                qtyUom = spQtyUom.toString();
                freeQtyUom = spFreeQtyUom.toString();
                if (!multipleItemsAllowed) {
                    deleteLinkedItemsIfPresent(prodCode, productListId);
                    addItemToList();
                } else {
                    addItemToList();
                }
                intent.putExtra(Utils.KEY_PRODUCT_LIST_ID, productListId);
                getTargetFragment().onActivityResult(getTargetRequestCode(), 1, intent);
                getDialog().dismiss();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    private void addItemToList() {
        if (qty != 0) {
            //parent item
            if (listItemId == -1) {
                //if this is not editting existing list item, new item will be added or
                // existing item from the top of this class will be edited
                listItem = new ProductsListItem();
            }
            ProductsListItem listFreeItem = new ProductsListItem();
            listItem.setBaseUom(product.getStockUom());
            listItem.setCess(product.getCess());
            listItem.setCgst(product.getCgst());
            listItem.setCompanyName(companyName);
            listItem.setIgst(product.getIgst());
            listItem.setProductCode(prodCode);
            listItem.setProductsListId(productListId);
            listItem.setProductName(product.getProductName());
            listItem.setQty(qty);
            listItem.setRate(rate);
            listItem.setSgst(product.getSgst());
            listItem.setUom(qtyUom);
            listItem.setUomConversion(salesUomConversionFactor);
            listItem.setWarehouse(warehouse);
            if (listItemId == -1) {
                //new list item is created here
                List<Long> listItemIdList = stDatabase.stDao().addProductsListItems(listItem);
                listItemId = listItemIdList.get(0);

            } else {
                //existing list item will be edited with this update query
                stDatabase.stDao().updateProductsListItem(listItem);
            }

            if (freeQty != 0) {
                //childItem
                //todo: handle free qty for item that already exists
                listFreeItem.setBaseUom(product.getStockUom());
                listFreeItem.setCess(product.getCess());
                listFreeItem.setCgst(product.getCgst());
                listFreeItem.setCompanyName(companyName);
                listFreeItem.setIgst(product.getIgst());
                listFreeItem.setProductCode(prodCode);
                listFreeItem.setProductsListId(productListId);
                listFreeItem.setProductName(product.getProductName());
                listFreeItem.setQty(freeQty);
                listFreeItem.setRate(rate);

                listFreeItem.setSgst(product.getSgst());
                listFreeItem.setUom(freeQtyUom);
                listFreeItem.setUomConversion(salesUomConversionFactor);
                listFreeItem.setWarehouse(warehouse);

                //unique fields to child item
                listFreeItem.setParentId(listItemId);
                listFreeItem.setDiscountPercentage(100);

                Long listFreeItemId = stDatabase.stDao().addProductsListItems(listFreeItem).get(0);
                listItem.setChildId(listFreeItemId);
                stDatabase.stDao().updateProductsListItem(listItem);
            } else {
                //checking if already existing product list item has a child
                if (listItem.getChildId() != null) {
                    //if the item has child, it cannot be deleted by making it zero
                    Toast.makeText(getActivity(), "Delete free item by long pressing the item in the list view.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void deleteLinkedItemsIfPresent(String productCode, Long listId) {
        //loops through a given list for a specific item and deletes the parent and child and breaks at the first occurrence
        List<ProductsListItem> listItems = stDatabase.stDao().getProductsListItemByProductsListId(listId);
        for (ProductsListItem item : listItems) {
            ProductsListItem linkedItem = new ProductsListItem();
            if (item.getProductCode().equals(productCode)) {
                long linkedId = -1;
                if (item.getParentId() == null) {
                    //this item is not a child
                    if (item.getChildId() == null) {
                        //this item is neither a child nor a parent - this is single. dies alone
                        stDatabase.stDao().deleteProductsListItem(item);
                        break;
                    } else {
                        //this item has a child. kill them both
                        linkedId = item.getChildId();
                        linkedItem = stDatabase.stDao().getProductsListItemById(linkedId);
                        stDatabase.stDao().deleteProductsListItem(item);
                        stDatabase.stDao().deleteProductsListItem(linkedItem);
                        break;
                    }
                } else {
                    //this item has a parent. kill them both
                    linkedId = item.getParentId();
                    linkedItem = stDatabase.stDao().getProductsListItemById(linkedId);
                    stDatabase.stDao().deleteProductsListItem(item);
                    stDatabase.stDao().deleteProductsListItem(linkedItem);
                    break;
                }
            }
        }
    }


    private void createNewListIfNotExists() {
        List<Long> listIdList = new ArrayList<>();
        //long listId=productListId;
        if (productListId == -1) {
            ProductsList list = new ProductsList();
            list.setCompany(listCompany);
            list.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd HH:MM:SS"));
            list.setDocType(Utils.STOCK_ENTRY);
            list.setEditable(true);
            listIdList = stDatabase.stDao().addProductsList(list);
            productListId = listIdList.get(0);
            intent.putExtra(Utils.KEY_PRODUCT_LIST_ID, productListId);


            switch (doctType) {
                case Utils.STOCK_ENTRY:
                    String toWarehouse;
                    StockEntry stockEntry = new StockEntry();
                    String transactionType = intent.getStringExtra("transactionType");
                    if (transactionType.equals(Utils.STOCK_MATERIAL_TRANSFER)) {
                        toWarehouse = intent.getStringExtra("toWarehouse");
                        stockEntry.setTargetWarehouse(toWarehouse);
                    }
                    stockEntry.setCompany(listCompany);
                    stockEntry.setDocStatus(Utils.DOC_STATUS_UNATTEMPTED);
                    stockEntry.setProductsListId(productListId);
                    stockEntry.setSourceWarehouse(warehouse);
                    String uniqueValue = "STE" + ac.createTimeStamp("yyyyMMddHHmmSSS");
                    stockEntry.setUniqueValue(uniqueValue);
                    docId = stDatabase.stDao().addStockEntry(stockEntry).get(0);
                    intent.putExtra(Utils.KEY_DOC_ID, docId);


            }
        }

    }


    private void handlePlusMinus(int change) {
        View focussedView = getActivity().getCurrentFocus();
        if (focussedView == etFreeQty || focussedView == etQty) {
            handleQtyPlus(focussedView);
        } else {
            focussedView = etQty;
            if (change == 1) {
                handleQtyPlus(focussedView);
            } else handleQtyMinus(focussedView);
        }
    }


    private void handleQtyPlus(View focussedView) {
        EditText focussedET = (EditText) focussedView;
        double currentQty;
        if (focussedET.getText() == null) {
            currentQty = 0;
        } else currentQty = Double.valueOf(focussedET.getText().toString());
        currentQty = currentQty + 1;
        focussedET.setText(String.valueOf(currentQty));
        focussedET.selectAll();

    }

    private void handleQtyMinus(View focussedView) {
        EditText focussedET = (EditText) focussedView;
        double currentQty;
        if (focussedET.getText() == null) {
            currentQty = 0;
        } else currentQty = Double.valueOf(focussedET.getText().toString());
        if (currentQty >= 1) {
            currentQty = currentQty - 1;
            focussedET.setText(String.valueOf(currentQty));
            focussedET.selectAll();

        }

    }
}
