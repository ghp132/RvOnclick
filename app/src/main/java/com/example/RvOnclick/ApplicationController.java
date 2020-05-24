package com.example.RvOnclick;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.RvOnclick.Dialogs.InfoDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ApplicationController {

    public static String TAG = "ApplicationController";

    public static StDatabase stDatabase;
    public static BluetoothSocket btsocket;


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

    public List<Territory> sortTerritoryList(List<com.example.RvOnclick.Territory> sortList) {
        //sorts territory list
        Collections.sort(sortList, new Comparator<com.example.RvOnclick.Territory>() {
            @Override
            public int compare(com.example.RvOnclick.Territory o1, com.example.RvOnclick.Territory o2) {
                return o1.getTerritoryName().compareToIgnoreCase(o2.getTerritoryName());
            }
        });
        return sortList;
    }

    public List<Brand> sortBrandList(List<Brand> sortList) {
        //sorts brand list
        Collections.sort(sortList, new Comparator<Brand>() {
            @Override
            public int compare(Brand o1, Brand o2) {
                return o1.getBrandName().compareToIgnoreCase(o2.getBrandName());
            }
        });
        return sortList;
    }

    public void hideSoftKeyboard(Activity activity) {
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


    public void getCompanies(final String siteUrl, final Context ctx, final StDatabase stDatabase) {
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
                                int isDefault = cList.getInt("is_default");
                                String addressName = cList.getString("stockem_address");
                                String sINamingSeries = cList.getString("sales_invoice_naming_series");

                                Company company = new Company();
                                company.setCompanyName(companyName);
                                company.setAbbr(abbr);
                                company.setIsDefault(isDefault);
                                company.setSalesInvoice_NamingSeries(sINamingSeries);
                                stDatabase.stDao().addCompnany(company);
                                getCompanyAddress(siteUrl, ctx, stDatabase, addressName, companyName);

                            }
                            if (stDatabase.stDao().getDefaultCompany() == null) {
                                Company defaultComapany = stDatabase.stDao().getAllCompanies().get(0);
                                defaultComapany.setIsDefault(1);
                                stDatabase.stDao().updateCompany(defaultComapany);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx, e.toString(), Toast.LENGTH_SHORT).show();
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
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(80000,
                3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public void getCompanyAddress(String siteUrl, final Context ctx, final StDatabase stDatabase,
                                  final String addressName, final String companyName) {
        String url = siteUrl + "/api/resource/Address/?fields=[\"*\"]";
        if (addressName != null) {
            url = url + "&filters=[[\"Address\",\"name\",\"=\",\"" + addressName + "\"]]";
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray data = new JSONArray();
                        JSONObject address = new JSONObject();
                        try {
                            data = response.getJSONArray("data");
                            Log.d(TAG, "onResponse: getCompanyAddress: " + addressName + ": " + data.toString());
                            for (int i = 0; i < data.length(); i++) {
                                address = data.getJSONObject(i);
                                String addressLine1 = address.getString("address_line1");
                                String addressLine2 = address.getString("address_line2");
                                String city = address.getString("city");
                                String phone = address.getString("phone");
                                String gstin = address.getString("gstin");

                                Company company = stDatabase.stDao().getCompanyByCompanyName(companyName);
                                company.setAddress_line1(addressLine1);
                                company.setAddress_line2(addressLine2);
                                company.setCity(city);
                                company.setPhone(phone);
                                company.setGstin(gstin);
                                stDatabase.stDao().updateCompany(company);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        makeShortToast(ctx, "Company Address done!");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx, "Could not fetch Address list", Toast.LENGTH_SHORT).show();
            }
        });
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public void makeShortToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public List<Long> splitProductListByCompany(Order order, StDatabase stDatabase) {
        List<Long> orderIdList = new ArrayList<>();
        if (stDatabase.stDao().countCompany() > 1) {
            Long orderId = order.getOrderId();
            orderIdList.add(orderId);
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
                for (String company : companyNameList) {
                    if (counter > 1) {
                        //Items belonging to the the first company in the list will remain in the existing order
                        Long newOrderId = Long.valueOf("-1");
                        for (OrderProduct op : orderProductList) {
                            if (op.getCompanyName().equals(company)) {
                                //create new order with product
                                newOrderId = createNewOrderIfNotExists(newOrderId, customer, order.getOrderId(), company, stDatabase);
                                orderIdList.add(newOrderId);
                                OrderProduct newOp = op;
                                stDatabase.stDao().deleteOrderProduct(op);
                                newOp.setOrderId(Integer.valueOf(String.valueOf(newOrderId)));
                                newOp.setCompanyName(company);
                                stDatabase.stDao().addProductToOrder(newOp);
                            }

                        }
                    } else {
                        order.setCompanyName(company);
                        stDatabase.stDao().updateOrder(order);
                    }
                    counter++;
                }

            } else if (companyNameList.size() == 1) {
                order.setCompanyName(companyNameList.get(0));
                stDatabase.stDao().updateOrder(order);
            }
        } else if (stDatabase.stDao().countCompany() == 1) {
            orderIdList.add(order.getOrderId());
            List<OrderProduct> orderProductList = stDatabase.stDao().getOrderProductsById(order.getOrderId());
            Company company = stDatabase.stDao().getAllCompanies().get(0);
            for (OrderProduct op : orderProductList) {
                op.setCompanyName(company.getCompanyName());
            }
            order.setCompanyName(company.getCompanyName());
            stDatabase.stDao().updateOrder(order);


        }
        updateAndCreateInvoicesIfNotExists(orderIdList, stDatabase);
        return orderIdList;
    }

    private List<Long> updateAndCreateInvoicesIfNotExists(List<Long> orderIdList, StDatabase stDatabase) {
        List<Long> invoiceIdList = new ArrayList<>();
        for (long orderId : orderIdList) {
            Order order = stDatabase.stDao().getOrderByOrderId(orderId);
            double grandTotal = Math.round(stDatabase.stDao().getOrderTotalValueByOrderId(orderId));
            Invoice invoice = stDatabase.stDao().getInvoiceByOrderId(orderId);
            if (invoice == null) {
                Invoice newInvoice = new Invoice();
                newInvoice.setGrandTotal(grandTotal);
                newInvoice.setDocStatus(Utils.DOC_STATUS_UNATTEMPTED);
                newInvoice.setOrderId(orderId);
                newInvoice.setCompany(order.getCompanyName());
                newInvoice.setCustomer(order.getCustomerCode());
                newInvoice.setOutstanding(grandTotal);
                newInvoice.setInvoiceDate(getCurrentDate());
                newInvoice.setPaidAmount(0.0);
                newInvoice.setInvoiceNumber("Unsynced" + orderId);
                stDatabase.stDao().createInvoice(newInvoice);

            } else {
                invoice.setGrandTotal(grandTotal);
                invoice.setOutstanding(grandTotal);
                String companyName = order.getCompanyName();
                invoice.setCompany(companyName);
                stDatabase.stDao().updateInvoice(invoice);
                Invoice testInvoice = stDatabase.stDao().getInvoiceByOrderId(orderId);
                String testCompany = testInvoice.getCompany();
                int x = 1;
            }
        }

        return invoiceIdList;
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
        if (configCgst == null) {
            cgstAccountName = "Central GST";
        } else {
            cgstAccountName = configCgst.getSvalues();
        }

        TblSettings configSgst = stDatabase.stDao().getConfigByName(CONFIG_SGST_NAME);
        String sgstAccountName;
        if (configSgst == null) {
            sgstAccountName = "State GST";
        } else {
            sgstAccountName = configSgst.getSvalues();
        }


        List<Account> cgstAccountList = getChildAccountNames(cgstAccountName, company, stDatabase);

        for (Account account : cgstAccountList) {

        }
        taxes = prepareTaxJson(cgstAccountList, company, stDatabase, taxes);
        List<Account> sgstAccountList = getChildAccountNames(sgstAccountName, company, stDatabase);
        taxes = prepareTaxJson(sgstAccountList, company, stDatabase, taxes);

        // create json object for each tax account
        //add to json array
        return taxes;
    }

    //prepare individual tax Json
    private JSONArray prepareTaxJson(List<Account> taxAccountList, Company company, StDatabase stDatabase, JSONArray taxes) {
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
    public List<Account> getChildAccountNames(String accountName, Company company, StDatabase stDatabase) {
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
                long orderProductId = op.getOrderProductId();
                stDatabase.stDao().updateOrderProduct(orderProduct);
                notPresent = false;
                break;
            }
        }
        if (notPresent) {
            stDatabase.stDao().addProductToOrder(orderProduct);
        }
    }

    public void updateCurrentOrderQty(Long orderId, StDatabase stDatabase) {

        List<OrderProduct> orderProductList = stDatabase.stDao().getOrderProductsById(orderId);
        List<Product> productList = stDatabase.stDao().getAllProducts();
        for (OrderProduct op : orderProductList) {
            String opCode = op.getProductCode();
            Double opQty = op.getQty();
            for (Product p : productList) {
                String pCode = p.getProductCode();
                if (opCode.equals(pCode)) {
                    p.setCurrentOrderQty(opQty);
                } else {
                    p.setCurrentOrderQty(0.00);
                }
            }
        }
    }

    public List<Customer> filterCustomersByProximity(List<Customer> customerList, Location currentLocation) {
        List<Customer> nearbyCustomers = new ArrayList<>();
        for (Customer customer : customerList) {
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

        return nearbyCustomers;
    }

    public void showKeyboard(View view, Context ctx) {
        //if (view!=null){
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(ctx.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);        //}
    }

    public int checkForSmsPermission(Context ctx, int requestCode) {
        int permissionGranted;
        if (ActivityCompat.checkSelfPermission(ctx,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission not yet granted. Use requestPermissions().
            // MY_PERMISSIONS_REQUEST_SEND_SMS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            ActivityCompat.requestPermissions((Activity) ctx,
                    new String[]{Manifest.permission.SEND_SMS},
                    requestCode);
            permissionGranted = 0;
        } else {
            // Permission already granted.
            permissionGranted = 1;
        }
        return permissionGranted;
    }

    public int onResultOfRequestPermission(int requestCode,
                                           String permissions[], int[] grantResults, Context ctx) {
        int permissionGranted = 0;
        switch (requestCode) {
            case 1: {
                if (permissions[0].equalsIgnoreCase(Manifest.permission.SEND_SMS)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted
                    permissionGranted = 1;
                } else {
                    // Permission denied.
                    Toast.makeText(ctx, "SMS Permission was denied", Toast.LENGTH_SHORT).show();
                    permissionGranted = 0;
                }
            }
        }
        return permissionGranted;
    }


    public void deleteOrderProduct(Context ctx, long orderProductId, StDatabase stDatabase, int requestCode) {
        //Todo: update order total at the top of the page in customerTransactionProduct
        OrderProduct orderProduct = stDatabase.stDao().getOrderProdutByOrderProductId(orderProductId);
        long childId = orderProduct.getChildId();
        long parentId = orderProduct.getParentId();
        if (childId != 0) {
            OrderProduct childOrderProduct = stDatabase.stDao().getOrderProdutByOrderProductId(childId);
            stDatabase.stDao().deleteOrderProduct(childOrderProduct);
        }
        if (parentId != 0) {
            OrderProduct parentOrderProduct = stDatabase.stDao().getOrderProdutByOrderProductId(parentId);
            parentOrderProduct.setChildId(0);
            stDatabase.stDao().updateOrderProduct(parentOrderProduct);
        }
        stDatabase.stDao().deleteOrderProduct(orderProduct);

        //updating the grand total of the corresponding invoice if it remains unsynced
        Invoice invoice = stDatabase.stDao().getInvoiceByOrderId(orderProduct.getOrderId());
        if (invoice.getDocStatus() == Utils.DOC_STATUS_UNATTEMPTED) {
            double invoiceTotal = stDatabase.stDao().getOrderTotalValueByOrderId(orderProduct.getOrderId());
            invoice.setGrandTotal(invoiceTotal);
            invoice.setOutstanding(Double.valueOf(Math.round(invoiceTotal)));
            stDatabase.stDao().updateInvoice(invoice);

            if (requestCode == 2) {
                //request from longClick of recyclerview item in customer order to delete the item
                //check to see if payment has already been made. If paid in excess of the current grandTotal delete the payment
                //TODO: check to see if only one payment can exist for one invoice(synced and unsynced)
                Payment payment = stDatabase.stDao().getUnsyncedPaymentByInvoiceNo(invoice.getInvoiceNumber());
                if (payment != null) {
                    double paidAmt = payment.getPaymentAmt();
                    String customerCode = payment.getCustomerCode();
                    String invoiceNo = payment.getInvoiceNo();
                    invoice.setPaidAmount(0.0);
                    stDatabase.stDao().updateInvoice(invoice);
                    stDatabase.stDao().deletePayment(payment);
                    String dialogTitle = "Payment Deleted";
                    String dialogMsg = "A payment of Rs." + paidAmt + " by " + customerCode +
                            " with Invoice No. " + invoiceNo + " has been deleted";
                    InfoDialog infoDialog = new InfoDialog();
                    infoDialog.showInfoDialog(ctx, dialogTitle, dialogMsg);
                }

            }

        }


    }





    public void sendSms(String mobileNo, String smsContent, Context ctx) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(mobileNo, null, smsContent, null, null);
        Log.d(TAG, "sendSms: Sent:" + mobileNo + ":" + smsContent);
        Toast.makeText(ctx, "SMS Sent.", Toast.LENGTH_SHORT).show();
    }


    public interface OnPriceProcessedListener {
        void onPriceProcessed(int position, List<Product> productList);
    }

    public interface OnInvoicePostedListener {
        void onInvoicePosted(boolean posted, long orderId, int resultCode, int noOfInvoices, int invoiceCount, int requestCode, Invoice postedInvoice);
    }

    public interface OnInvoiceStatusUpdatedListener {
        void onInvoiceStatusUpdated(boolean success, int resultCode, int requestCode, List<Order> updatedOrders);
    }

    public interface OnInvoiceFetchedListener {
        void onInvoiceFetched(boolean success, int resultCode, int requestCode, List<Invoice> fetchedInvoices);
    }


    public class PostAndUpdateInvoices {
        OnInvoiceStatusUpdatedListener onInvoiceStatusUpdatedListener;
        OnInvoicePostedListener onInvoicePostedListener;
        boolean responseReceived = false;
        boolean shouldUpdateStatus = false;
        int noOfProcessedInvoices = 0;


        PostAndUpdateInvoices(OnInvoicePostedListener onInvoicePostedListener,
                              OnInvoiceStatusUpdatedListener onInvoiceStatusUpdatedListener) {
            this.onInvoicePostedListener = onInvoicePostedListener;
            this.onInvoiceStatusUpdatedListener = onInvoiceStatusUpdatedListener;

        }

        public void postOrders(final Context ctx, final List<Order> orderList, final StDatabase stDatabase, final int requestCode) {
            JSONObject params = new JSONObject();
            JSONObject taxesC = new JSONObject();
            JSONObject taxesS = new JSONObject();
            JSONObject taxesI = new JSONObject();

            JSONArray taxes = new JSONArray();
            UserConfig uc = stDatabase.stDao().getAllUserConfig().get(0);

            List<Order> splitOrderList = new ArrayList<>();

            //splitting items in order by company and creating a new arraylist of orders
            for (Order order : orderList) {
                List<Long> splitOrderIdList = splitProductListByCompany(order, stDatabase);
                splitOrderList.addAll(createSplitOrderList(splitOrderIdList, stDatabase));
            }
            orderList.clear();
            orderList.addAll(splitOrderList);
            final int noOfInvoices = orderList.size();


            for (final Order order : orderList) {
                noOfProcessedInvoices += 1;
                JSONArray salesTeamArray = new JSONArray();

                String appOrderId = order.getAppOrderId();
                final Long orderId = order.getOrderId();
                String priceListName = order.getPriceListName();
                if (priceListName == null || priceListName.equals("null")) {
                    priceListName = "Standard Selling";
                }
                String territory = order.getTerritory();
                //CustomField sales person to be added in User Doctype
                //UserConfig contains only one row as of 15/05/2019


                String userEmail = uc.getUserId();
                User u = stDatabase.stDao().getUserByEmailId(userEmail);
                String salesPerson = u.getSalesPerson();
                String company = order.getCompanyName();

                String custCode = order.getCustomerCode();
                Calendar calendar = Calendar.getInstance();
                String deliveryDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + (calendar.get(Calendar.DAY_OF_MONTH));

                //creating salesTeamArray
                JSONObject salesPersonObject = new JSONObject();
                try {
                    salesPersonObject.put("parenttype", "Sales Invoice");
                    salesPersonObject.put("allocated_percentage", "100");
                    salesPersonObject.put("sales_person", salesPerson);
                    salesTeamArray.put(salesPersonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ctx, "Error while creating salesPersonObject", Toast.LENGTH_SHORT).show();
                }


                RequestQueue queue = MySingleton.getInstance(ctx).getRequestQueue();

                List<OrderProduct> orderProducts = stDatabase.stDao().getOrderProductsById(orderId);
                JSONArray jsonArray = new JSONArray();
                for (OrderProduct orderProduct : orderProducts) {
                    JSONObject jsonObject = new JSONObject();
                    String itemCode = orderProduct.getProductCode();
                    Double qty = orderProduct.getQty();
                    Double rate = orderProduct.getRate();
                    String warehouse = orderProduct.getWarehouse();
                    String costCenter = "Main - " + stDatabase.stDao().getAbbrByCompanyName(company);
                    double discountPercentage = orderProduct.getDiscountPercentage();
                    try {
                        jsonObject.put("item_code", itemCode);
                        jsonObject.put("qty", qty);
                        jsonObject.put("rate", rate);
                        jsonObject.put("discount_percentage", discountPercentage);
                        jsonObject.put("warehouse", warehouse);
                        jsonObject.put("cost_center", costCenter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jsonArray.put(jsonObject);
                }
                order.setOrderStatus(0);
                stDatabase.stDao().updateOrder(order);
                try {
                    params.put("customer", custCode);
                    params.put("territory", territory);
                    params.put("selling_price_list", priceListName);
                    params.put("company", company);
                    params.put("selling_price_list", priceListName);
                    params.put("items", jsonArray);
                    params.put("app_invoice_id", appOrderId);
                    params.put("update_stock", "1");


                    taxes = getTaxArray(orderId, stDatabase);
                    params.put("taxes", taxes);
                    params.put("sales_team", salesTeamArray);

                    // params.put("taxes_and_charges", "In State GST Inclusive - HE");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final String loginUrl = uc.getLoginUrl();
                String url = loginUrl + "/api/resource/Sales%20Invoice/";
                RequestQueue requestQueue = Volley.newRequestQueue(ctx);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        url, params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                responseReceived = true;
                                //Toast.makeText(getApplicationContext(),
                                //    "Logged in" + response, Toast.LENGTH_SHORT).show();

                                //adding erpnext Order number to the order database table
                                JSONObject rJson;
                                try {
                                    rJson = response.getJSONObject("data");
                                    String orderNumber = rJson.getString("name");
                                    stDatabase.stDao().updateOrderNumber(orderNumber, orderId);
                                    stDatabase.stDao().updateOrderStatus(1, orderId);
                                    Toast.makeText(ctx, orderNumber, Toast.LENGTH_SHORT).show();

                                    //populating invoices table
                                    Invoice newInvoice = parseInvoiceJson(rJson);
                                    stDatabase.stDao().createInvoice(newInvoice);
                                    updatePaymentFromOrder(orderId, stDatabase);

                                    onInvoicePostedListener.onInvoicePosted(true, orderId, 0, noOfInvoices, noOfProcessedInvoices, requestCode, newInvoice);

                                } catch (JSONException je) {
                                    je.printStackTrace();
                                    onInvoicePostedListener.onInvoicePosted(false, orderId, 1, noOfInvoices, noOfProcessedInvoices, requestCode, null);
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        responseReceived = true;
                        error.printStackTrace();
                        stDatabase.stDao().updateOrderStatus(-1, orderId);
                        onInvoicePostedListener.onInvoicePosted(false, orderId, 2, noOfInvoices, noOfProcessedInvoices, requestCode, null);
                        shouldUpdateStatus = true;
                        List<Order> orderList1 = new ArrayList<>();
                        orderList1.add(order);
                        updateUnknownOrderStatuses(ctx, orderList1, stDatabase, requestCode);
                        Toast.makeText(ctx, "Updating Invoice Status" + error.toString(), Toast.LENGTH_SHORT).show();

                        if (error.networkResponse != null) {
                            if (error.networkResponse.data != null) {
                                String body;
                                try {
                                    body = new String(error.networkResponse.data, "UTF-8");
                                    //tvResponseDisplay.setText(body);
                                    Log.d(TAG, "onErrorResponse: postOrders" + body);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                    Toast.makeText(ctx, "UnsupportedEncodingException", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
                );//jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(80000,
                //3,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
            }
            if (shouldUpdateStatus) {
                updateUnknownOrderStatuses(ctx, orderList, stDatabase, requestCode);
            }


        }

        public void updateUnknownOrderStatuses(final Context ctx, final List<Order> orders, final StDatabase stDatabase, final int requestCode) {
            String url;
            final int pageLimitLength = 500;
            final OnInvoiceStatusUpdatedListener invoiceStatusUpdatedListener;
            String loginUrl = stDatabase.stDao().getAllUserConfig().get(0).getLoginUrl();
            final List<Order> updatedOrders = new ArrayList<>();

            url = loginUrl + "/api/resource/Sales%20Invoice?fields=[\"name\",\"app_invoice_id\",\"docstatus\"]&limit_page_length=" + pageLimitLength;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        JSONObject jsonObject;
                        JSONArray jsonArray;

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                jsonArray = response.getJSONArray("data");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                onInvoiceStatusUpdatedListener.onInvoiceStatusUpdated(false, 1, requestCode, updatedOrders);
                            }

                            for (Order order : orders) {
                                if (order.getOrderStatus() == 0) {
                                    String mobileAppOrderId = order.getAppOrderId();
                                    for (int t = 0; t <= pageLimitLength - 1; t++) {
                                        try {
                                            jsonObject = jsonArray.getJSONObject(t);
                                            String appOrderId = jsonObject.getString("app_invoice_id");
                                            String orderNumber = jsonObject.getString("name");
                                            if (appOrderId.equals(mobileAppOrderId)) {
                                                updatedOrders.add(order);
                                                if (jsonObject.getInt("docstatus") == 1) {
                                                    order.setOrderStatus(2);
                                                    order.setOrderNumber(orderNumber);
                                                    stDatabase.stDao().updateOrder(order);

                                                    break;
                                                } else {
                                                    order.setOrderStatus(1);
                                                    order.setOrderNumber(orderNumber);
                                                    stDatabase.stDao().updateOrder(order);
                                                    break;
                                                }
                                            } else {
                                                order.setOrderStatus(-1);
                                                stDatabase.stDao().updateOrder(order);
                                            }

                                            onInvoiceStatusUpdatedListener.onInvoiceStatusUpdated(true, 0, requestCode, updatedOrders);


                                        } catch (JSONException e) {
                                            onInvoiceStatusUpdatedListener.onInvoiceStatusUpdated(false, 2, requestCode, updatedOrders);
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            Toast.makeText(ctx, "Invoice Status Updated", Toast.LENGTH_SHORT).show();
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    onInvoiceStatusUpdatedListener.onInvoiceStatusUpdated(false, 3, requestCode, updatedOrders);
                    error.printStackTrace();
                    Toast.makeText(ctx, error.toString(), Toast.LENGTH_SHORT).show();

                }
            });
            MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
        }


        /*
        public void fetchFileteredInvoices(final Context ctx, List<String> invoiceNumbers) {
            boolean applyFilter = false;
            String filterUrl;
            String loginUrl = stDatabase.stDao().getAllUserConfig().get(0).getLoginUrl();
            if (invoiceNumbers != null) {
                for (String invoiceNumber : invoiceNumbers) {
                    filterUrl = "&filters=[[\"Sales%20Invoice\",\"name\",\"=\"," + invoiceNumber;


                    String url = loginUrl + "/api/resource/Sales%20Invoice?fields=[\"name\",\"customer\",\"rounded_total\",\"company\",\"outstanding_amount\",\"docstatus\",\"posting_date\"]";
                    url = url + filterUrl;
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONArray json = response.getJSONArray("data");
                                        JSONObject invoiceJson;

                                        for (int i = 0; i < json.length(); i++) {
                                            invoiceJson = (JSONObject) json.get(i);
                                            */
                                            /*
                                            String customer_code = invoice.getString("customer");
                                            Double grand_total = Double.parseDouble(invoice.getString("grand_total"));
                                            String invoice_no = invoice.getString("name");
                                            String company = invoice.getString("company");
                                            Double outstanding_amount = Double.parseDouble(invoice.getString("outstanding_amount"));
                                            Boolean invoice_status = Boolean.parseBoolean(invoice.getString("docstatus"));
                                            String invoice_date = invoice.getString("posting_date");

                                            Invoice dbInvoice = new Invoice();
                                            dbInvoice.setInvoiceNumber(invoice_no);
                                            dbInvoice.setCustomer(customer_code);
                                            dbInvoice.setCompany(company);
                                            dbInvoice.setGrandTotal(grand_total);
                                            dbInvoice.setInvoiceStatus(invoice_status);
                                            dbInvoice.setOutstanding(outstanding_amount);
                                            dbInvoice.setInvoiceDate(invoice_date);
                                            dbInvoice.setPaidAmount(0.00);

                                            stDatabase.stDao().createInvoice(dbInvoice);
                                            */
                                            /*
                                            Invoice invoice = parseInvoiceJson(invoiceJson);
                                            stDatabase.stDao().createInvoice(invoice);
                                        }

                                        Toast.makeText(ctx, "Invoice Done", Toast.LENGTH_SHORT).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(ctx, "Invoice not Done", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(ctx, "Error in receiving Invoice JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                    );
                    MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);


                }
            }
        }*/
    }

    public List<Order> createSplitOrderList(List<Long> orderIdList, StDatabase stDatabase) {
        //creates arraylist of Orders from arraylist of OrderIds
        List<Order> splitOrderList = new ArrayList<>();
        for (long orderId : orderIdList) {
            Order order = stDatabase.stDao().getOrderByOrderId(orderId);
            splitOrderList.add(order);
        }
        return splitOrderList;

    }

    public Invoice parseInvoiceJson(JSONObject invoiceJson) {
        Invoice invoice = new Invoice();
        try {


            String customer_code = invoiceJson.getString("customer");
            Double grand_total = Double.parseDouble(invoiceJson.getString("rounded_total"));
            String invoice_no = invoiceJson.getString("name");
            String company = invoiceJson.getString("company");
            Double outstanding_amount = Double.parseDouble(invoiceJson.getString("outstanding_amount"));
            Boolean invoice_status = Boolean.parseBoolean(invoiceJson.getString("docstatus"));
            String invoice_date = invoiceJson.getString("posting_date");

            invoice.setInvoiceNumber(invoice_no);
            invoice.setCustomer(customer_code);
            invoice.setCompany(company);
            invoice.setGrandTotal(grand_total);
            invoice.setInvoiceStatus(invoice_status);
            invoice.setOutstanding(outstanding_amount);
            invoice.setInvoiceDate(invoice_date);
            invoice.setPaidAmount(0.00);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return invoice;
    }

    public void updateInvoiceAndPayment(JSONObject invoiceJson, long orderId, StDatabase stDatabase) {
        Invoice invoice = stDatabase.stDao().getInvoiceByOrderId(orderId);
        Payment payment = stDatabase.stDao().getUnsyncedPaymentByInvoiceNo(invoice.getInvoiceNumber());
        try {


            String customer_code = invoiceJson.getString("customer");
            Double grand_total = Double.parseDouble(invoiceJson.getString("rounded_total"));
            String invoice_no = invoiceJson.getString("name");
            String company = invoiceJson.getString("company");
            Double outstanding_amount = Double.parseDouble(invoiceJson.getString("outstanding_amount"));
            Boolean invoice_status = Boolean.parseBoolean(invoiceJson.getString("docstatus"));
            String invoice_date = invoiceJson.getString("posting_date");

            invoice.setInvoiceNumber(invoice_no);
            //invoice.setCustomer(customer_code);
            //invoice.setCompany(company);
            invoice.setGrandTotal(grand_total);
            invoice.setInvoiceStatus(invoice_status);
            if (invoice_status) {
                invoice.setDocStatus(Utils.DOC_STATUS_SUBMITTED);
            } else {
                invoice.setDocStatus(Utils.DOC_STATUS_DRAFT);
            }
            invoice.setOutstanding(outstanding_amount);
            invoice.setInvoiceDate(invoice_date);

            //invoice.setPaidAmount(0.00);
            if (payment != null) {
                payment.setInvoiceNo(invoice_no);
                stDatabase.stDao().updatePayment(payment);
            }

            stDatabase.stDao().updateInvoice(invoice);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void updatePaymentFromOrder(Long orderId, StDatabase stDatabase) {
        Order order = stDatabase.stDao().getOrderByOrderId(orderId);
        Payment payment = new Payment();
        if (order.getPaidAmt() > 0.0 && order.getOrderNumber() != null) {
            payment.setAppPaymentId(order.getAppOrderId());
            payment.setCompany(order.getCompanyName());
            payment.setCustomerCode(order.getCustomerCode());
            payment.setInvoiceNo(order.getOrderNumber());
            payment.setPaymentDate(getCurrentDate());
            payment.setPaymentStatus(-1);
            payment.setPaymentAmt(order.getPaidAmt());
            payment.setChequePayment(false);
            stDatabase.stDao().makePayment(payment);
        }
    }

    public String getCurrentDate() {
        String currentDate;
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = df.format(c);

        return currentDate;
    }

    public void pay4sms(final Context ctx, String token, int credit, String message, String phNumber) {
        final String url = "http://pay4sms.in/sendsms/?token=" + token + "&credit=" + credit +
                "&message=" + message + "&number=" + phNumber;

/*
            JSONObject params = new JSONObject();
            try {
                params.put("usr", loginEmail);
                params.put("pwd", loginPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
        //RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(ctx,
                                response.toString(), Toast.LENGTH_SHORT).show();


                        //login(getApplicationContext());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(ctx, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        MySingleton.getInstance(ctx).addToRequestQueue(stringRequest);

    }

    public Payment createPaymentFromOrder(long orderId, StDatabase stDatabase) {
        Payment payment = new Payment();
        Order order = stDatabase.stDao().getOrderByOrderId(orderId);
        double paidAmt = order.getPaidAmt();
        if (paidAmt != 0.0) {
            String invoiceNo = order.getOrderNumber();
            Invoice invoice = stDatabase.stDao().getInvoiceByInvoiceNo(invoiceNo);
            payment.setPaymentAmt(paidAmt);
            payment.setPaymentStatus(0);
            payment.setPaymentDate(getCurrentDate());
            payment.setChequePayment(false);
            payment.setInvoiceNo(invoiceNo);
            payment.setCustomerCode(order.getCustomerCode());
            payment.setCompany(order.getCompanyName());

            long paymentId = stDatabase.stDao().makePayment(payment);
            payment.setPaymentId(paymentId);
            payment.setAppPaymentId(createUniqueAppPaymentId(paymentId));
            stDatabase.stDao().updatePayment(payment);
            return payment;
        } else {
            return null;
        }


        //return payment;
    }

    public List<Payment> createPaymentFromOrderList(List<Order> orderList, StDatabase stDatabase) {
        List<Payment> paymentList = new ArrayList<>();
        for (Order order : orderList) {
            paymentList.add(createPaymentFromOrder(order.getOrderId(), stDatabase));
        }
        return paymentList;
    }

    public String createUniqueAppPaymentId(Long paymentId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String ts = simpleDateFormat.format(new Date());
        String uniqueAppPaymentId = ts + paymentId + "P";
        return uniqueAppPaymentId;

    }

    public String createTimeStamp(String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        String timeStamp = simpleDateFormat.format(new Date());
        return timeStamp;
    }

    public Invoice updateOfflineInvoice(Invoice invoice, JSONObject invoiceJson) {
        try {


            String customer_code = invoiceJson.getString("customer");
            Double grand_total = Double.parseDouble(invoiceJson.getString("rounded_total"));
            String invoice_no = invoiceJson.getString("name");
            String company = invoiceJson.getString("company");
            Double outstanding_amount = Double.parseDouble(invoiceJson.getString("outstanding_amount"));
            Boolean invoice_status = Boolean.parseBoolean(invoiceJson.getString("docstatus"));
            String invoice_date = invoiceJson.getString("posting_date");

            invoice.setGrandTotal(grand_total);
            invoice.setInvoiceNumber(invoice_no);
            invoice.setOutstanding(outstanding_amount);
            invoice.setInvoiceStatus(invoice_status);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return invoice;
    }


    public void displayNetworkError(String html, Context ctx) {
        Intent m2aIntent = new Intent(ctx, Main2Activity.class);
        m2aIntent.putExtra("html", html);
        m2aIntent.putExtra("fragment", Utils.ERROR_DISPLAY_FRAGMENT);
        m2aIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(m2aIntent);
    }

    private void processError(Context ctx, VolleyError error, String origin) {
        if (error.networkResponse != null) {
            if (error.networkResponse.data != null) {
                String body = "";
                try {
                    body = new String(error.networkResponse.data, "UTF-8");
                    VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                    errorRecord.setOrgin(origin);
                    errorRecord.setErrorBody(body);
                    errorRecord.setTimeStamp(createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                    stDatabase.stDao().addErrorRecord(errorRecord);
                    Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(ctx, "UnsupportedEncodingException", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            VolleyErrorRecord errorRecord = new VolleyErrorRecord();
            errorRecord.setOrgin(origin);
            errorRecord.setErrorBody(error.toString());
            errorRecord.setTimeStamp(createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
            stDatabase.stDao().addErrorRecord(errorRecord);
            Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();
        }
    }

    public String getCompanyName(String productCode, StDatabase stDatabase) {
        //returns the Company name for items if added or returns a default company name if not added

        String companyName = stDatabase.stDao().getProductByProductCode(productCode).getProductCompany();
        boolean companyDefaultSet = false;
        if (companyName == null || companyName.equals("null")) {
            //company name might be null if company name has not been added to the Company custom field for Item
            //hence default company will be added to those
            //if no default company is set presumably the first company will be added
            List<Company> companies = stDatabase.stDao().getAllCompanies();
            for (Company company : companies) {
                if (company.getIsDefault() == 1) {
                    companyName = company.getCompanyName();
                    companyDefaultSet = true;
                    break;
                }
            }
            if (!companyDefaultSet) {
                companyName = companies.get(0).getCompanyName();
            }
        }

        return companyName;
    }


}