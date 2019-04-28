package com.example.RvOnclick;


import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DialogFragment_ProductRateInfo extends DialogFragment {
    TextView tv;
    Button btProductSave, btIncrease, btDecrease, btCancel;
    EditText etQty, etRate, etFreeQty;
    public static StDatabase stDatabase;
    public String custCode, priceListName, territory;
    public long orderId;
    ApplicationController ac = new ApplicationController();

    public DialogFragment_ProductRateInfo() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        final String prodCode = getArguments().getString("prodCode");
        orderId = Long.parseLong(getArguments().getString("orderId"));
        custCode = getArguments().getString("custCode");
        priceListName = intent.getStringExtra("priceList");
        territory = intent.getStringExtra("territory");




        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_fragment__product_rate_info, container, false);
        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();

        btProductSave = view.findViewById(R.id.bt_productSave);
        btCancel = view.findViewById(R.id.bt_cancel);
        btIncrease = view.findViewById(R.id.bt_increase);
        btDecrease = view.findViewById(R.id.bt_decrease);
        etQty = view.findViewById(R.id.et_qty);
        etFreeQty = view.findViewById(R.id.et_freeQty);




        etRate = view.findViewById(R.id.et_rate);

        etRate.setText(String.valueOf(getArguments().getDouble("rate")));
        if (stDatabase.stDao().getAllUserConfig().get(0).getAllowRateChange()==0){
            disableEditText(etRate);
        }
        Double qty = getArguments().getDouble("qty");
        double freeQty = getArguments().getDouble("freeQty");
        etQty.setText(Double.toString(qty));
        etFreeQty.setText(Double.toString(freeQty));

        if (getTargetRequestCode()==1) {
            etQty.setText("1.0");
        }
        etQty.selectAll();

        etQty.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        btProductSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double qty = Double.parseDouble(etQty.getText().toString());
                if (etFreeQty.getText().toString().equals("")){etFreeQty.setText("0");}
                double freeQty = Double.parseDouble(etFreeQty.getText().toString());
                Double rate = Double.parseDouble((etRate.getText().toString()));
                if (qty > 0) {
                    CreateNewOrderIfNotExists(orderId);
                    long existingId = alreadyPresent(orderId,prodCode);
                    if (existingId!=0) {
                        //same item already existing will be deleted.
                        OrderProduct orderProduct=stDatabase.stDao().getOrderProdutByOrderProductId(existingId);
                        if (orderProduct.getParentId()!=0){
                            //passing parentId instead of childId to delete both parent and child.
                            existingId = orderProduct.getParentId();
                        }
                        ac.deleteOrderProduct(existingId,stDatabase);
                    }
                       long parentId = AddProductToOrder(orderId, prodCode, qty, rate);

                    if (freeQty>0){
                        addFreeQtyToOrder(orderId,prodCode,freeQty,parentId);
                    }
                }

                int countOfProducts;
                if (orderId != -1 && qty == 0) {
                    countOfProducts = stDatabase.stDao().countOrderProduct(orderId);
                    List<OrderProduct> singleProductList = stDatabase.stDao().getOrderProductsById(orderId);
                    if (countOfProducts == 1) {
                        if (singleProductList.get(0).getProductCode().equals(prodCode)) {
                            deleteOrder(orderId);
                        }
                    } else if (countOfProducts > 1) {
                        for (OrderProduct orderProduct : singleProductList) {
                            if (orderProduct.getProductCode().equals(prodCode)) {
                                stDatabase.stDao().deleteOrderProduct(orderProduct);
                            }
                        }
                    }
                }
                //passing data to the CustomerTransactionProductFragment
                Intent intent = new Intent();
                intent.putExtra("orderId", String.valueOf(orderId));

                getTargetFragment().onActivityResult(getTargetRequestCode(), 1, intent);
                getDialog().dismiss();
            }
        });
        btIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double qty = Double.parseDouble(etQty.getText().toString());
                qty = qty + 1;
                etQty.setText(Double.toString(qty));

            }
        });

        btDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double qty = Double.parseDouble(etQty.getText().toString());
                if (qty >= 1) {
                    qty = qty - 1;
                }
                etQty.setText(Double.toString(qty));
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

    //
    public void CreateNewOrderIfNotExists(long orderId) {
        if (orderId == -1) {
            Order order = new Order();
            order.setCustomerCode(custCode);
            order.setPriceListName(priceListName);
            order.setTerritory(territory);
            order.setOrderStatus(-1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmddhhmmss");
            String ts = simpleDateFormat.format(new Date());
            order.setAppOrderId(ts + orderId + "R");

            this.orderId = stDatabase.stDao().createOrder(order);
            String appOrderId = ts + this.orderId + "R";
            stDatabase.stDao().updateAppOrderId(appOrderId, this.orderId);
        }
    }

    //Adds items to the order in OrderProduct database table
    public long AddProductToOrder(long orderId, String prodCode, double qty, double rate) {
        ApplicationController ac = new ApplicationController();
        Product product = stDatabase.stDao().getProductByProductCode(prodCode);
        String companyName = getCompanyName(prodCode);
        String abbr = stDatabase.stDao().getAbbrByCompanyName(companyName);
        String warehouse = "Stores - " + abbr;
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrderId(orderId);
        orderProduct.setProductCode(prodCode);
        orderProduct.setQty(qty);
        orderProduct.setRate(rate);
        orderProduct.setWarehouse(warehouse);

        orderProduct.setCompanyName(companyName);

        long parentId = stDatabase.stDao().addProductToOrder(orderProduct);
        ac.updateCurrentOrderQty(orderId, stDatabase);

        /*Boolean notPresent = true;
        //check to see if the product is already present in the order
        List<OrderProduct> orderProductList = stDatabase.stDao().getOrderProductsById(orderId);
        for (OrderProduct op : orderProductList) {
            if (op.getProductCode().equals(prodCode)) {
                int orderProductId = op.getOrderProductId();
                stDatabase.stDao().updateOrderProductById(qty, rate, orderProductId);
                notPresent = false;
                ac.updateCurrentOrderQty(orderId,stDatabase);
                break;
            }
        }
        if (notPresent) {
            //Company company = stDatabase.stDao().getCompanyByCompanyName()
            //orderProduct = createGstJsonArray()
            stDatabase.stDao().addProductToOrder(orderProduct);
            ac.updateCurrentOrderQty(orderId, stDatabase);
        }*/

        return parentId;
    }

    private void addFreeQtyToOrder(long orderId, String prodCode,double freeQty, long parentId){
        String companyName = getCompanyName(prodCode);
        OrderProduct orderProduct = new OrderProduct();
        OrderProduct parent = new OrderProduct();
        orderProduct.setOrderId(orderId);
        orderProduct.setProductCode(prodCode);
        orderProduct.setParentId(parentId);
        orderProduct.setQty(freeQty);
        orderProduct.setDiscountPercentage(100.0);
        orderProduct.setCompanyName(companyName);
        long childId = stDatabase.stDao().addProductToOrder(orderProduct);
        parent = stDatabase.stDao().getOrderProdutByOrderProductId(parentId);
        parent.setChildId(childId);
        stDatabase.stDao().updateOrderProduct(parent);
    }

    private long alreadyPresent(long orderId, String prodCode){
        long existingId=0;
        List<OrderProduct> orderProductList = stDatabase.stDao().getOrderProductsById(orderId);
        for (OrderProduct op : orderProductList) {
            if (op.getProductCode().equals(prodCode)) {
                existingId = op.getOrderProductId();
            }
        }

        return existingId;
    }


    public void deleteOrder(Long orderId) {
        stDatabase.stDao().deleteOrderByOrderId(orderId);
        stDatabase.stDao().deleteOrderProductByOrderId(orderId);
    }

    public JSONArray createGstJsonArray(Company company, Context ctx){
        String taxNameCgst, taxNameSgst, companyAbbr;
        JSONArray gstJsonArray = new JSONArray();
        companyAbbr = company.getAbbr();

        //for intra-state
        TblSettings taxConfigCgst = stDatabase.stDao().getConfigByName("cgstAccountName");
        TblSettings taxConfigSgst = stDatabase.stDao().getConfigByName("sgstAccountName");
        taxNameCgst = taxConfigCgst.getSvalues();
        taxNameSgst = taxConfigSgst.getSvalues();
        JSONObject cgstJsonObj = createTaxJsonObject(taxNameCgst,companyAbbr,ctx);
        JSONObject sgstJsonObj = createTaxJsonObject(taxNameSgst, companyAbbr, ctx);
        gstJsonArray.put(cgstJsonObj);
        gstJsonArray.put(sgstJsonObj);

        //for inter-state
        //write code here - create if statement

        return gstJsonArray;

        }

    public JSONObject createTaxJsonObject(String taxName, String companyAbbr,Context ctx){
        ApplicationController ac = new ApplicationController();
        JSONObject taxJson = new JSONObject();
        taxName = taxName + " - " + companyAbbr;
        List<Account> listOfTaxes = stDatabase.stDao().getAccountByParentAccount(taxName);
        for (Account account : listOfTaxes) {
            try {
                taxJson.put("account_head", taxName);
                taxJson.put("doctype", "Sales Taxes and Charges");
                taxJson.put("charge_type", "On Net Total");
                taxJson.put("description", taxName);
                taxJson.put("included_in_print_rate", "1");
            } catch (JSONException e) {
                e.printStackTrace();
                ac.makeShortToast(ctx, "Could not create Tax JSON");
            }
        }


        return taxJson;

    }


    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundColor(Color.TRANSPARENT);
    }

    private String getCompanyName(String pCode){
        String companyName=stDatabase.stDao().getProductByProductCode(pCode).getProductCompany();
        boolean companyDefaultSet=false;
        if (companyName==null||companyName.equals("null")){
            //company name might be null if company name has not been added to the Company custom field for Item
            //hence default company will be added to those
            //if no default company is set presumably the first company will be added
            List<Company> companies = stDatabase.stDao().getAllCompanies();
            for (Company company:companies){
                if (company.getIsDefault()==1){
                    companyName = company.getCompanyName();
                    companyDefaultSet=true;
                    break;
                }
            }
            if (!companyDefaultSet){
                companyName = companies.get(0).getCompanyName();
            }
        }

        return companyName;
    }

}
