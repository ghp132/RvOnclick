package com.example.RvOnclick;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ApplicationController {

    public static String TAG = "ApplicationController";

    public static StDatabase stDatabase;



    public List<Customer> sortCustomerList(List<Customer> sortList) {

        //sorts customer list
        Collections.sort(sortList, new Comparator<Customer>() {
            @Override
            public int compare(Customer o1, Customer o2) {
                return o1.getCustomer_name().compareToIgnoreCase(o2.getCustomer_name());
            }
        });
        return sortList;
    }

    public static void hideSoftKeyboard(Activity activity) {
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    public String getOrderStatusDefn(int i) {
        String status;
        //order statuses
        // -1 sync unattempted
        // 0 sync attempted - result unknown
        // 1 draft
        // 2 submitted
        switch (i) {
            case -1:
                status = "Pending";
                return status;
            case 0:
                status = "Unknown";
                return status;
            case 1:
                status = "Draft";
                return status;
            case 2:
                status = "Submitted";
                return status;
            default:
                status = "";
                return status;


        }
    }


    public void checkConfigExistence(String configName, String configValue, StDatabase stDatabase) {
        //stDatabase = Room.databaseBuilder(getApplicationContext(), StDatabase.class, "StDB")
        //  .allowMainThreadQueries().build();
        TblSettings setting = new TblSettings();
        List<TblSettings> settings = stDatabase.stDao().getAllSettings();
        boolean present = false;
        for (TblSettings st : settings) {
            if (st.getDefn().equals(configName)) {
                present = true;
                st.setSvalues(configValue);
                stDatabase.stDao().updateConfig(st);
            }

        }
        if (!present) {
            setting.setDefn(configName);
            setting.setSvalues(configValue);
            stDatabase.stDao().addConfig(setting);
        }
    }


    public void getCompanies(String siteUrl, final Context ctx, final StDatabase stDatabase) {
        String url = siteUrl + "/api/resource/Company/?fields=[\"*\"]";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray data = new JSONArray();
                        JSONObject cList = new JSONObject();
                        try {
                            data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                cList = data.getJSONObject(i);
                                String companyName = cList.getString("company_name");
                                String abbr = cList.getString("abbr");
                                Company company = new Company();
                                company.setCompanyName(companyName);
                                company.setAbbr(abbr);
                                stDatabase.stDao().addCompnany(company);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx, "Could not parse Company List", Toast.LENGTH_SHORT).show();
                        }
                        makeShortToast(ctx, "Company list done!");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx, "Could not fetch Company list", Toast.LENGTH_SHORT).show();
            }
        }
        );
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public void makeShortToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public void splitProductListByCompany(Order order, StDatabase stDatabase) {
        if (stDatabase.stDao().countCompany() > 1) {
            Long orderId = order.getOrderId();

            List<OrderProduct> orderProductList = stDatabase.stDao().getOrderProductsById(orderId);
            Customer customer = stDatabase.stDao().getCustomerbyCustomerCode(order.getCustomerCode());
            List<String> companyNameList = new ArrayList<>();
            for (OrderProduct op : orderProductList) {
                String company = op.getCompanyName();
                if (!companyNameList.contains(company)) {
                    companyNameList.add(company);
                }
            }
            if (companyNameList.size() > 1) {
                int counter = 1;
                Long newOrderId = Long.valueOf("-1");
                for (String company : companyNameList) {
                    if (counter > 1) {
                        for (OrderProduct op : orderProductList) {
                            if (op.getCompanyName().equals(company)) {
                                //create new order with product
                                newOrderId = createNewOrderIfNotExists(newOrderId, customer, order.getOrderId(), company, stDatabase);
                                OrderProduct newOp = op;
                                stDatabase.stDao().deleteOrderProduct(op);
                                newOp.setOrderId(Integer.valueOf(String.valueOf(newOrderId)));
                                newOp.setCompanyName(company);
                                stDatabase.stDao().addProductToOrder(newOp);
                            }

                        }
                    }
                    counter++;
                }

            }
        } else if (stDatabase.stDao().countCompany() == 1) {
            List<OrderProduct> orderProductList = stDatabase.stDao().getOrderProductsById(order.getOrderId());
            Company company = stDatabase.stDao().getAllCompanies().get(0);
            for (OrderProduct op : orderProductList) {
                op.setCompanyName(company.getCompanyName());
            }
            order.setCompanyName(company.getCompanyName());
            stDatabase.stDao().updateOrder(order);


        }
    }

    public JSONArray getTaxArray(Long orderId, StDatabase stDatabase) {
        String CONFIG_CGST_NAME, CONFIG_SGST_NAME, CONFIG_IGST_NAME;
        CONFIG_CGST_NAME = "cgstAccountName";
        CONFIG_IGST_NAME = "igstAccountName";
        CONFIG_SGST_NAME = "sgstAccountName";
        JSONArray taxes = new JSONArray();
        //get order
        Order order = stDatabase.stDao().getOrderByOrderId(orderId);
        //get company
        String companyName = order.getCompanyName();
        //get abbr
        Company company = stDatabase.stDao().getCompanyByName(companyName);
        String companyAbbr = company.getAbbr();
        //check inter or intrastate(Later)
        // get inter or intrastate account after checking isGroup child
        //includer if-intrastate(Later)
        TblSettings configCgst = stDatabase.stDao().getConfigByName(CONFIG_CGST_NAME);
        String cgstAccountName;
        if (configCgst == null){
            cgstAccountName = "CGST";
        } else {
            cgstAccountName = configCgst.getSvalues();
        }

        TblSettings configSgst = stDatabase.stDao().getConfigByName(CONFIG_SGST_NAME);
        String sgstAccountName;
        if (configSgst == null){
            sgstAccountName = "SGST";
        } else {
            sgstAccountName = configSgst.getSvalues();
        }


        List<Account> cgstAccountList = getChildAccountNames(cgstAccountName,company, stDatabase);

        for (Account account:cgstAccountList){

        }
        taxes = prepareTaxJson(cgstAccountList, company, stDatabase, taxes);
        List<Account> sgstAccountList = getChildAccountNames(sgstAccountName,company, stDatabase);
        taxes = prepareTaxJson(sgstAccountList, company, stDatabase, taxes);

        // create json object for each tax account
        //add to json array
        return taxes;
    }

    //prepare individual tax Json
    private JSONArray prepareTaxJson(List<Account> taxAccountList, Company company, StDatabase stDatabase,JSONArray taxes) {
        String companyAbbr = company.getAbbr();
        for (Account account : taxAccountList) {
            JSONObject taxesJson = new JSONObject();
            String accountHead, docType, chargeType, description, includedInPrintRate;
            accountHead = account.getAccountName() + " - " + companyAbbr;
            docType = "Sales Taxes and Charges";
            chargeType = "On Net Total";
            description = accountHead;
            includedInPrintRate = "1";
            try {
                taxesJson.put("account_head", accountHead);
                taxesJson.put("doctype", docType);
                taxesJson.put("charge_type", chargeType);
                taxesJson.put("description", description);
                taxesJson.put("included_in_print_rate", includedInPrintRate);
                taxes.put(taxesJson);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "prepareTaxJson: " + "Error while creating Json Object");
            }
        }
        return taxes;
    }

    //gets child account arraylist if account name isGroup
    public List<Account> getChildAccountNames(String accountName,Company company, StDatabase stDatabase) {
        List<Account> accountList = new ArrayList<>();
        String companyAbbr = company.getAbbr();
        accountName = accountName + " - " + companyAbbr;
        Account inputAccount = stDatabase.stDao().getAccountByName(accountName);
        int isGroup = inputAccount.getIsGroup();
        if (isGroup == 1) {
            accountList = stDatabase.stDao().getAccountByParentAccount(accountName);
        } else {
            accountList.add(inputAccount);
        }
        return accountList;
    }


    public Long createNewOrderIfNotExists(Long orderId, Customer customer, Long splitFrom, String companyName, StDatabase stDatabase) {
        if (orderId == -1) {
            Order order = new Order();
            String custCode = customer.getCustomer_id();
            order.setCustomerCode(custCode);
            order.setOrderStatus(-1);
            order.setSplitFrom(splitFrom);
            order.setCompanyName(companyName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmddhhmmss");
            String ts = simpleDateFormat.format(new Date());
            order.setAppOrderId(ts + orderId + "R");

            orderId = stDatabase.stDao().createOrder(order);
            String appOrderId = ts + orderId + "R";
            stDatabase.stDao().updateAppOrderId(appOrderId, orderId);
            return orderId;
        }
        return orderId;
    }

    public void newOrderProduct(OrderProduct orderProduct) {
        Boolean notPresent = true;
        List<OrderProduct> orderProductList = stDatabase.stDao().getOrderProductsById(orderProduct.getOrderId());
        for (OrderProduct op : orderProductList) {
            if (op.getProductCode().equals(orderProduct.getProductCode())) {
                int orderProductId = op.getOrderProductId();
                stDatabase.stDao().updateOrderProduct(orderProduct);
                notPresent = false;
                break;
            }
        }
        if (notPresent) {
            stDatabase.stDao().addProductToOrder(orderProduct);
        }
    }

    public void updateCurrentOrderQty(Long orderId, StDatabase stDatabase){

        List<OrderProduct> orderProductList = stDatabase.stDao().getOrderProductsById(orderId);
        List<Product> productList = stDatabase.stDao().getAllProducts();
        for (OrderProduct op:orderProductList){
            String opCode = op.getProductCode();
            Double opQty = op.getQty();
            for (Product p : productList){
                String pCode = p.getProductCode();
                if (opCode.equals(pCode)){
                    p.setCurrentOrderQty(opQty);
                } else {
                    p.setCurrentOrderQty(0.00);
                }
            }
        }
    }

    public List<Customer> filterCustomersByProximity(List<Customer> customerList, Location currentLocation){
        List<Customer> nearbyCustomers = new ArrayList<>();
        for (Customer customer:customerList) {
            Location customerLocation = new Location("point A");
            if (customer.getLatitude() != null && customer.getLatitude() != null) {
                customerLocation.setLatitude(customer.getLatitude());
                customerLocation.setLongitude(customer.getLongitude());

                float distance = currentLocation.distanceTo(customerLocation);
                if (distance < 100) {
                    nearbyCustomers.add(customer);
                }
            }
        }

        return  nearbyCustomers;
    }


}



