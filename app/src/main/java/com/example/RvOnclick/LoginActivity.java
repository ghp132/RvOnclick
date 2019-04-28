package com.example.RvOnclick;

import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.opencsv.CSVReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    EditText tvLoginEmail, tvLoginPassword, tvUrl;
    Button btLogin, btGetData, btNextActivity, btPostOrders, btPostPayments,
            btUpdateUnknownPayments, btUpdateUnknownOrders, btConfig, btEggSales;
    String loginEmail, loginPassword, loginUrl, siteUrl;
    public static StDatabase stDatabase;
    public boolean newData;
    ApplicationController ac = new ApplicationController();
    private String TAG = "LoginActivity";
    FrameLayout pbLl;
    int requestCounter, responseCounter;
    TextView tvResponseDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvLoginEmail = (EditText) findViewById(R.id.tv_loginEmail);
        tvLoginPassword = (EditText) findViewById(R.id.tv_loginPassword);
        btLogin = (Button) findViewById(R.id.bt_login);
        btGetData = findViewById(R.id.bt_getData);
        btNextActivity = findViewById(R.id.bt_nextActivity);
        btPostOrders = findViewById(R.id.bt_postSampleData);
        btPostPayments = findViewById(R.id.bt_postPayment);
        btUpdateUnknownPayments = findViewById(R.id.bt_updateStatus);
        btUpdateUnknownOrders = findViewById(R.id.bt_updateOrderStatus);
        btConfig = findViewById(R.id.bt_config);
        btEggSales = findViewById(R.id.bt_eggSales);
        pbLl = findViewById(R.id.linlaProgressBar);
        newData = false;
        tvResponseDisplay = findViewById(R.id.tv_responseDisplay);


        //final String companyAbbreviation = "HE";
        //final String company = "Hari Enterprises";
        tvUrl = findViewById(R.id.tv_url);
        stDatabase = Room.databaseBuilder(getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().fallbackToDestructiveMigration().build();


        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);
        final CookieStore cookieStore = manager.getCookieStore();


        if (stDatabase.stDao().countUserConfig() >= 1) {
            fillLoginInfo();
            siteUrl=stDatabase.stDao().getAllUserConfig().get(0).getLoginUrl();
        }
        loginEmail = tvLoginEmail.getText().toString();
        loginPassword = tvLoginPassword.getText().toString();
        loginUrl = tvUrl.getText().toString();
        int sendSmsFromMobile = stDatabase.stDao().getSendSmsConfig(loginEmail);
        if (sendSmsFromMobile == 1) {
            ac.checkForSmsPermission(this, 1);
        }

        //SmsManager smsManager = SmsManager.getDefault();
        //smsManager.sendTextMessage("9092747505",null,"This is a test msg",null,null);

        //final Intent eggSalesIntent = new Intent(this, EggSalesActivity.class);
        btEggSales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<UserConfig> userConfigList = stDatabase.stDao().getAllUserConfig();
                String info = "";
                for (UserConfig uc : userConfigList) {
                    info = info + uc.getFullName() + "\n" + uc.getSendSms() + "\n\n";

                }
                tvResponseDisplay.setText(info);
            }
        });

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCounter = 2;
                responseCounter = 0;
                pbLl.setVisibility(View.VISIBLE);
                final List<UserConfig> userConfigList = stDatabase.stDao().getAllUserConfig();
                boolean userConfigPresent = false;
                loginEmail = tvLoginEmail.getText().toString();
                loginPassword = tvLoginPassword.getText().toString();
                loginUrl = tvUrl.getText().toString();
                saveLoginInfo();

                login(getApplicationContext());
                /*if (stDatabase.stDao().countUsers() != 0) {
                    stDatabase.stDao().deleteAllUsers();
                }
                getUserConfig(getApplicationContext());*/


            }
        });

        //------------------------------------------------------------------------------------------------------------

        final Intent configIntent = new Intent(this, ActivityConfig.class);
        btConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(configIntent);

            }
        });

        //------------------------------------------------------------------------------------------------------------

        btUpdateUnknownPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCounter = 2;
                responseCounter = 0;
                pbLl.setVisibility(View.VISIBLE);
                updateUnknownPaymentStatuses(loginUrl, getApplicationContext());
                updateUnknownOrderStatuses(loginUrl, getApplicationContext());
            }
        });

        //-------------------------------------------------------------------------------------------------------------

        btUpdateUnknownOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUnknownOrderStatuses(loginUrl, getApplicationContext());
            }
        });

        //------------------------------------------------------------------------------------------------------------

        final TextView textView = findViewById(R.id.tv_responseDisplay);
        final Context ctx = this;
        btPostOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCounter = 1;
                responseCounter = 0;
                pbLl.setVisibility(View.VISIBLE);
                postOrders(getApplicationContext());
            }
        });


        //sync payments
        btPostPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCounter = 1;
                responseCounter = 0;
                pbLl.setVisibility(View.VISIBLE);
                postPayments(getApplicationContext());

            }
        });

        //getting Items
        btGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCounter = 0;
                responseCounter = 0;


                if (stDatabase.stDao().countCompany() != 0) {
                    stDatabase.stDao().deleteAllCompanies();
                }
                ac.getCompanies(siteUrl, getApplicationContext(), stDatabase);
                //requestCounter += 1;


                newData = true;
                if (stDatabase.stDao().countProduct() != 0) {
                    stDatabase.stDao().deleteAllProduct();
                }

                syncItems(getApplicationContext());
                requestCounter += 1;

                if (stDatabase.stDao().countCustomer() != 0) {
                    stDatabase.stDao().deleteAllCustomer();
                }

                syncCustomers(getApplicationContext());
                requestCounter += 1;

                if (stDatabase.stDao().countInvoice() != 0) {
                    stDatabase.stDao().deleteAllInvoices();
                }
                syncInvoices(getApplicationContext());
                requestCounter += 1;

                if (stDatabase.stDao().countPriceList() != 0) {
                    stDatabase.stDao().deleteAllPriceLists();
                }
                getPriceListList(getApplicationContext());
                requestCounter += 1;

                if (stDatabase.stDao().countPrice() != 0) {
                    stDatabase.stDao().deleteAllPrice();
                }
                getSellingPriceList2(getApplicationContext());
                requestCounter += 1;

                if (stDatabase.stDao().countAccounts() != 0) {
                    stDatabase.stDao().deleteAllAccounts();
                }
                getAccounts(getApplicationContext());
                requestCounter += 1;

                if (stDatabase.stDao().countUsers() != 0) {
                    stDatabase.stDao().deleteAllUsers();
                }
                getUserConfig(getApplicationContext());
                requestCounter += 1;


            }
        });

        btNextActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });

    }


    public void postPayments(Context ctx) {

        String info = "";

        List<Payment> unsyncedPaymentList = stDatabase.stDao().getUnsyncedPayments();
        if (unsyncedPaymentList.isEmpty()) {
            responseCounter += 1;
            handleProgressBar();
        } else {
            String companyName, companyAbbreviation;
            Company company = new Company();

            for (final Payment payment : unsyncedPaymentList) {
                JSONObject jsonObjPayment = new JSONObject();
                JSONArray jsonArrReferences = new JSONArray();
                JSONObject jsonObjInvoices = new JSONObject();
                final Long paymentId = payment.getPaymentId();
                Double totalAllocatedAmount = payment.getPaymentAmt();
                String modeOfPayment;
                if (payment.getChequePayment()) {
                    break;
                } else {
                    modeOfPayment = "Cash";

                }

                stDatabase.stDao().updatePaymentStatus(0, payment.getPaymentId());
                int allocatePayment = 1;
                companyName = payment.getCompany();
                company = stDatabase.stDao().getCompanyByName(companyName);
                companyAbbreviation = company.getAbbr();
                String paidTo = "Cash - " + companyAbbreviation;
                String partyType = "Customer";
                String paidFrom = "Debtors - " + companyAbbreviation;
                String party = payment.getCustomerCode();
                Double receivedAmount = payment.getPaymentAmt();
                String refernceName = payment.getInvoiceNo();
                Double allocatedAmount = payment.getPaymentAmt();
                String referenceDocType = "Sales Invoice";
                String paymentType = "Receive";
                Double paidAmount = payment.getPaymentAmt();
                String appPaymentId = payment.getAppPaymentId();

                try {
                    jsonObjInvoices.put("reference_name", refernceName);
                    jsonObjInvoices.put("allocated_amount", allocatedAmount);
                    jsonObjInvoices.put("reference_doctype", referenceDocType);

                    jsonArrReferences.put(jsonObjInvoices);

                    jsonObjPayment.put("total_allocated_amount", totalAllocatedAmount);
                    jsonObjPayment.put("mode_of_payment", modeOfPayment);
                    jsonObjPayment.put("allocate_payment_amount", allocatePayment);
                    jsonObjPayment.put("paid_to", paidTo);
                    jsonObjPayment.put("party_type", partyType);
                    jsonObjPayment.put("company", companyName);
                    jsonObjPayment.put("paid_from", paidFrom);
                    jsonObjPayment.put("party", party);
                    jsonObjPayment.put("received_amount", receivedAmount);
                    jsonObjPayment.put("references", jsonArrReferences);
                    jsonObjPayment.put("payment_type", paymentType);
                    jsonObjPayment.put("paid_amount", paidAmount);
                    jsonObjPayment.put("app_payment_id", appPaymentId);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error in jsonObjPayment", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "postPayments: \n" + jsonObjPayment.toString());
                String url = loginUrl + "/api/resource/Payment%20Entry/";
                RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        url, jsonObjPayment,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                siteUrl = loginUrl;
                                //Toast.makeText(getApplicationContext(),
                                //    "Logged in" + response, Toast.LENGTH_SHORT).show();

                                //adding erpnext Order number to the order database table
                                Toast.makeText(getApplicationContext(), "payment done!", Toast.LENGTH_SHORT).show();
                                JSONObject rJson;
                                try {
                                    rJson = response.getJSONObject("Data");
                                    String paymentNumber = rJson.getString("name");
                                    stDatabase.stDao().updatePaymentNumber(paymentNumber, paymentId);
                                    stDatabase.stDao().updatePaymentStatus(1, paymentId);
                                    Toast.makeText(getApplicationContext(), paymentNumber, Toast.LENGTH_SHORT).show();


                                } catch (JSONException je) {
                                    je.printStackTrace();
                                }
                                responseCounter += 1;
                                handleProgressBar();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        responseCounter += 1;
                        handleProgressBar();
                        Toast.makeText(getApplicationContext(),
                                "Cannot post payments!\n" + error.toString(), Toast.LENGTH_SHORT).show();


                    }
                }
                );
                //jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000
                //      , 0, 3000));
                MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);

                info = info + jsonObjPayment.toString() + "\n\n";
                TextView textView = findViewById(R.id.tv_responseDisplay);
                textView.setText(info);
            }
        }

    }


    public void postOrders(Context ctx) {
        responseCounter = 0;
        JSONObject params = new JSONObject();
        JSONObject taxesC = new JSONObject();
        JSONObject taxesS = new JSONObject();
        JSONObject taxesI = new JSONObject();
        JSONArray taxes = new JSONArray();

        TextView textView = findViewById(R.id.tv_responseDisplay);

        String info = "";
        List<Order> unsycedOrderList = stDatabase.stDao().getUnsyncedOrders();
        for (Order order : unsycedOrderList) {
            ac.splitProductListByCompany(order, stDatabase);
        }
        unsycedOrderList = stDatabase.stDao().getUnsyncedOrders();
        if (unsycedOrderList.isEmpty()) {
            responseCounter += 1;
            handleProgressBar();
        } else {
            for (Order order : unsycedOrderList) {
                JSONArray salesTeamArray = new JSONArray();

                info = info + "\n\n";
                String appOrderId = order.getAppOrderId();
                final Long orderId = order.getOrderId();
                String priceListName = order.getPriceListName();
                if (priceListName == null || priceListName.equals("null")) {
                    priceListName = "Standard Selling";
                }
                String territory = order.getTerritory();
                //CustomField sales person to be added in User Doctype
                UserConfig uc=stDatabase.stDao().getAllUserConfig().get(0);
                String userEmail=uc.getUserId();
                User u=stDatabase.stDao().getUserByEmailId(userEmail);
                String salesPerson = u.getSalesPerson();
                String company = order.getCompanyName();

                info = info + orderId;
                String custCode = order.getCustomerCode();
                info = info + "\n" + custCode;
                Calendar calendar = Calendar.getInstance();
                String deliveryDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + (calendar.get(Calendar.DAY_OF_MONTH));
                info = info + "\n" + deliveryDate;
                textView.setText(info);

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


                RequestQueue queue = MySingleton.getInstance(getApplicationContext()).getRequestQueue();

                List<OrderProduct> unsyncedOrderProduct = stDatabase.stDao().getOrderProductsById(orderId);
                JSONArray jsonArray = new JSONArray();
                for (OrderProduct orderProduct : unsyncedOrderProduct) {
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
                        jsonObject.put("cost_center",costCenter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jsonArray.put(jsonObject);
                }
                order.setOrderStatus(0);
                stDatabase.stDao().updateOrder(order);
                try {
                    //params.put("delivery_date", deliveryDate);
                    params.put("customer", custCode);
                    params.put("territory", territory);
                    params.put("selling_price_list", priceListName);
                    params.put("company", company);
                    params.put("selling_price_list", priceListName);
                    params.put("items", jsonArray);
                    params.put("app_invoice_id", appOrderId);
                    params.put("update_stock", "1");


                    taxes = ac.getTaxArray(orderId, stDatabase);
                    params.put("taxes", taxes);
                    params.put("sales_team", salesTeamArray);

                    // params.put("taxes_and_charges", "In State GST Inclusive - HE");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                info = info + "\n" + params.toString();
                textView.setText(info);

                String url = loginUrl + "/api/resource/Sales%20Invoice/";
                RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        url, params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                siteUrl = loginUrl;
                                //Toast.makeText(getApplicationContext(),
                                //    "Logged in" + response, Toast.LENGTH_SHORT).show();

                                //adding erpnext Order number to the order database table
                                JSONObject rJson;
                                try {
                                    rJson = response.getJSONObject("Data");
                                    String orderNumber = rJson.getString("name");
                                    stDatabase.stDao().updateOrderNumber(orderNumber, orderId);
                                    stDatabase.stDao().updateOrderStatus(1, orderId);
                                    Toast.makeText(getApplicationContext(), orderNumber, Toast.LENGTH_SHORT).show();


                                } catch (JSONException je) {
                                    je.printStackTrace();
                                }
                                updateUnknownOrderStatuses(loginUrl, getApplicationContext());
                                responseCounter += 1;
                                handleProgressBar();

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        responseCounter += 1;
                        handleProgressBar();
                        Toast.makeText(getApplicationContext(), "Sales Invoice: "+error.toString(), Toast.LENGTH_SHORT).show();

                        if (error.networkResponse.data!=null){
                            String body = "";
                            try{
                                body = new String(error.networkResponse.data,"UTF-8");
                                //tvResponseDisplay.setText(body);
                                Log.d(TAG, "onErrorResponse: postOrders" + body);
                            }catch (UnsupportedEncodingException e){
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "UnsupportedEncodingException", Toast.LENGTH_SHORT).show();


                            }
                        }

                        //textView.setText(error.toString());
                    }
                }
                );
                //jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000
                //      , 0, 3000));
                MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
            }
        }
    }


    public void deleteOrders(View view) {
        stDatabase.stDao().deleteAllOrderProducts();
        stDatabase.stDao().deleteAllOrders();
        TextView textView = findViewById(R.id.tv_responseDisplay);
        //textView.setText("");
    }

    public void updateUnknownPaymentStatuses(String loginUrl, Context ctx) {
        String url;
        RequestQueue requestQueue;


        final List<Payment> unknownPayments = stDatabase.stDao().getPaymentByPaymentStatus(0);
        final int pageLimitLength = 5;

        url = loginUrl + "/api/resource/Payment%20Entry?fields=[\"name\",\"app_payment_id\",\"docstatus\"]&limit_page_length=" + pageLimitLength;
        requestQueue = Volley.newRequestQueue(LoginActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    JSONObject jsonObject;
                    JSONArray jsonArray;
                    Boolean matched = true;

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            jsonArray = response.getJSONArray("data");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        for (Payment payment : unknownPayments) {
                            String mobileAppPaymentId = payment.getAppPaymentId();
                            for (int t = 0; t <= pageLimitLength - 1; t++) {
                                try {
                                    jsonObject = jsonArray.getJSONObject(t);
                                    String appPaymentId = jsonObject.getString("app_payment_id");
                                    String paymnetNumber = jsonObject.getString("name");
                                    if (appPaymentId.equals(mobileAppPaymentId)) {
                                        if (jsonObject.getInt("docstatus") == 1) {
                                            payment.setPaymentStatus(2);
                                            payment.setPaymentNumber(paymnetNumber);
                                            stDatabase.stDao().updatePayment(payment);

                                            break;
                                        } else {
                                            payment.setPaymentStatus(1);
                                            payment.setPaymentNumber(paymnetNumber);
                                            stDatabase.stDao().updatePayment(payment);
                                            break;
                                        }
                                    } else {
                                        payment.setPaymentStatus(-1);
                                        stDatabase.stDao().updatePayment(payment);
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        responseCounter += 1;
                        handleProgressBar();
                        Toast.makeText(getApplicationContext(), "Payment Status Updated", Toast.LENGTH_SHORT).show();

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                responseCounter += 1;
                handleProgressBar();
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public void showDetail(View view) {
        List<Payment> payments = stDatabase.stDao().getAllPayments();
        TextView textView = findViewById(R.id.tv_responseDisplay);
        textView.setText("");
        String info = "";
        for (Payment payment : payments) {
            info = info + "\n" + payment.getInvoiceNo();
            info = info + "\n" + payment.getCustomerCode();
            info = info + "\n" + payment.getPaymentNumber();
            info = info + "\n" + "Id : " + payment.getPaymentId();
            info = info + "\n" + "Amount : " + payment.getPaymentAmt();
            info = info + "\n" + "AppPaymentID : " + payment.getAppPaymentId();
            info = info + "\n" + "Status : " + payment.getPaymentStatus() + "\n\n";
        }
        textView.setText(info);

    }

    public void updateUnknownOrderStatuses(String loginUrl, Context ctx) {
        String url;
        RequestQueue requestQueue;


        final List<Order> unknownOrders = stDatabase.stDao().getOrderByOrderStatus(0);
        final int pageLimitLength = 500;

        url = loginUrl + "/api/resource/Sales%20Invoice?fields=[\"name\",\"app_invoice_id\",\"docstatus\"]&limit_page_length=" + pageLimitLength;
        requestQueue = Volley.newRequestQueue(LoginActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    JSONObject jsonObject;
                    JSONArray jsonArray;
                    Boolean matched = true;

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            jsonArray = response.getJSONArray("data");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        for (Order order : unknownOrders) {
                            String mobileAppOrderId = order.getAppOrderId();
                            for (int t = 0; t <= pageLimitLength - 1; t++) {
                                try {
                                    jsonObject = jsonArray.getJSONObject(t);
                                    String appOrderId = jsonObject.getString("app_invoice_id");
                                    String orderNumber = jsonObject.getString("name");
                                    if (appOrderId.equals(mobileAppOrderId)) {
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


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        responseCounter += 1;
                        handleProgressBar();
                        Toast.makeText(getApplicationContext(), "Invoice Status Updated", Toast.LENGTH_SHORT).show();
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                responseCounter += 1;
                handleProgressBar();
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public void getAccounts(Context ctx) {
        String url = loginUrl + "/api/resource/Account/?fields=[\"name\",\"account_name\",\"account_type\",\"parent_account\",\"company\",\"is_group\"]&limit_page_length=1000";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray data = new JSONArray();
                        JSONObject jsonObject = new JSONObject();
                        try {
                            data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                jsonObject = data.getJSONObject(i);
                                String name, accountName, parentAccount, company, accountType;
                                int isGroup;
                                name = jsonObject.getString("name");
                                accountName = jsonObject.getString("account_name");
                                parentAccount = jsonObject.getString("parent_account");
                                accountType = jsonObject.getString("account_type");
                                company = jsonObject.getString("company");
                                isGroup = jsonObject.getInt("is_group");

                                Account account = new Account();
                                account.setAccountName(accountName);
                                account.setAccountType(accountType);
                                account.setCompany(company);
                                account.setName(name);
                                account.setParentAccount(parentAccount);
                                account.setIsGroup(isGroup);

                                stDatabase.stDao().addAccount(account);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Could not fetch accounts", Toast.LENGTH_SHORT).show();

                        }
                        responseCounter += 1;
                        handleProgressBar();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseCounter += 1;
                handleProgressBar();
            }
        });
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public void syncItems(Context ctx) {
        pbLl.setVisibility(View.VISIBLE);
        String url = loginUrl + "/api/resource/Item?fields=[\"*\"]&limit_page_length=1000";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray json = response.getJSONArray("data");
                            //Log.d(TAG, "onResponse: syncItems: response" + response.toString());

                            for (int i = 0; i < json.length(); i++) {
                                JSONObject item = (JSONObject) json.get(i);
                                String product_name = item.getString("item_name");
                                String brand = item.getString("brand");
                                String disabled = item.getString("disabled");
                                if (disabled.equals("1")) {
                                    disabled = "true";
                                } else {
                                    disabled = "false";
                                }
                                Boolean disabledB = Boolean.parseBoolean(disabled);
                                String product_code = item.getString("item_code");
                                String product_group = item.getString("item_group");
                                String product_rate = item.getString("standard_rate");
                                String company = item.getString("company");
                                Log.d(TAG, "onResponse: syncItems: Items:" + product_code + "Company" + company);
                                if (company.equals("MJ Farms")){
                                    int x = 1;
                                }


                                Product product = new Product();
                                product.setProductCode(product_code);
                                product.setProductName(product_name);
                                product.setProductBrand(brand);
                                product.setProductDisabled(disabledB);
                                product.setProductGroup(product_group);
                                product.setProductRate(Double.parseDouble(product_rate));
                                product.setProductCompany(company);

                                stDatabase.stDao().addProduct(product);

                            }
                            Toast.makeText(getApplicationContext(), "Item Done", Toast.LENGTH_SHORT).show();

                            if (newData) {
                                //getSellingPriceList(getApplicationContext());
                                getStockBalance(getApplicationContext());
                                newData = false;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        //tvResponseDisplay.setText("syncItemError");

                        responseCounter += 1;
                        handleProgressBar();
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                responseCounter += 1;
                handleProgressBar();
                Toast.makeText(getApplicationContext(), "Error in receiving Item Data", Toast.LENGTH_SHORT).show();


            }
        }
        );
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);

    }

    public void getPriceListList(Context ctx) {
        String url = siteUrl + "/api/resource/Price List/?fields=[\"price_list_name\",\"buying\",\"selling\",\"enabled\"]";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray data = new JSONArray();
                        JSONObject pList = new JSONObject();
                        try {
                            data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                pList = data.getJSONObject(i);
                                int selling = pList.getInt("selling");
                                String priceListName = pList.getString("price_list_name");
                                int enabled = pList.getInt("enabled");
                                int buying = pList.getInt("buying");

                                PriceList priceList = new PriceList();
                                priceList.setSelling(selling);
                                priceList.setPriceListName(priceListName);
                                priceList.setEnabled(enabled);
                                priceList.setBuying(buying);

                                stDatabase.stDao().addPriceList(priceList);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Could not fetch Price Lists", Toast.LENGTH_SHORT).show();
                        }

                        responseCounter += 1;
                        handleProgressBar();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseCounter += 1;
                handleProgressBar();
            }
        }
        );
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public void syncCustomers(final Context ctx) {
        pbLl.setVisibility(View.VISIBLE);
        String url = siteUrl + "/api/resource/Customer?fields=[\"*\"]&limit_page_length=1000";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray json = response.getJSONArray("data");

                            for (int i = 0; i < json.length(); i++) {
                                JSONObject customer = (JSONObject) json.get(i);
                                String customer_name = customer.getString("customer_name");
                                String territory = customer.getString("territory");
                                String disabled = customer.getString("disabled");
                                if (disabled.equals("1")) {
                                    disabled = "true";
                                } else {
                                    disabled = "false";
                                }
                                String mobileNo = customer.getString("mobile_no");
                                Boolean disabledB = Boolean.parseBoolean(disabled);
                                String customer_id = customer.getString("name");
                                String customer_group = customer.getString("customer_group");
                                String price_list = customer.getString("default_price_list");
                                //Double latitude = customer.getDouble("customer_latitude");
                                //Double longitude = customer.getDouble("customer_longitude");
                                String display_name = null;
                                if (customer.has("display_name")) {
                                    display_name = customer.getString("display_name");
                                }

                                Customer dbCustomer = new Customer();
                                dbCustomer.setCustomer_name(customer_name);
                                dbCustomer.setTerritory(territory);
                                dbCustomer.setCustomer_disabled(disabledB);
                                dbCustomer.setCustomer_id(customer_id);
                                dbCustomer.setCustomer_group(customer_group);
                                dbCustomer.setDisplay_name(display_name);
                                dbCustomer.setPrice_list(price_list);
                                dbCustomer.setMobileNo(mobileNo);
                                //dbCustomer.setLatitude(latitude);
                                //dbCustomer.setLongitude(longitude);


                                stDatabase.stDao().addCustomer(dbCustomer);
                            }
                            Toast.makeText(getApplicationContext(), "Customer Done", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error in parsing customer Json", Toast.LENGTH_SHORT).show();
                        }

                        responseCounter += 1;
                        handleProgressBar();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body=error.toString();
                error.printStackTrace();
                responseCounter += 1;
                handleProgressBar();
                Toast.makeText(getApplicationContext(), "Error in receiving Customer JSON", Toast.LENGTH_SHORT).show();
                //tvResponseDisplay.setText("syncCustomerError");
                if (error.networkResponse.data!=null){
                    try{
                        body = new String(error.networkResponse.data,"UTF-8");
                        tvResponseDisplay.setText(body);
                        Log.d(TAG, "onErrorResponse: SyncCustomer" + body);
                    }catch (UnsupportedEncodingException e){
                        e.printStackTrace();
                        Toast.makeText(ctx, "UnsupportedEncodingException", Toast.LENGTH_SHORT).show();


                    }
                }
            }
        }
        );
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public void getStockBalance(Context ctx) {
        pbLl.setVisibility(View.VISIBLE);
        String url = siteUrl + "/?report_name=Stock+Balance&filters=%7B%22from_date%22%3A%22" + getCurrentDate() + "%22%2C%22to_date%22%3A%22" + getCurrentDate() + "%22%7D&cmd=frappe.desk.query_report.run&_=1546137019256";
        final TextView textView = findViewById(R.id.tv_responseDisplay);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject message = response.getJSONObject("message");
                            JSONArray results = message.getJSONArray("result");
                            JSONArray result;
                            StringBuilder sb = new StringBuilder();
                            Product product = new Product();
                            for (int i = 0; i < results.length() - 1; i++) {
                                result = results.getJSONArray(i);
                                String str = result.getString(0);
                                product = stDatabase.stDao().getProductByProductCode(result.getString(0));
                                if(product==null){
                                    int x=1;
                                }
                                product.setStock(result.getDouble(13));
                                stDatabase.stDao().updateProduct(product);
                                sb.append(result.getString(0));
                                sb.append("\n");
                                sb.append(result.getDouble(13));
                                sb.append("\n\n");
                            }
                            //textView.setText(sb);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Stock not updated", Toast.LENGTH_SHORT).show();
                            textView.setText(e.toString());
                        }

                        responseCounter += 1;
                        handleProgressBar();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                responseCounter += 1;
                handleProgressBar();
                Toast.makeText(getApplicationContext(), "Error in receiving Stock Balance", Toast.LENGTH_SHORT).show();


            }

        }

        );
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public void syncInvoices(Context ctx) {
        pbLl.setVisibility(View.VISIBLE);
        String url = siteUrl + "/api/resource/Sales%20Invoice?fields=[\"name\",\"customer\",\"grand_total\",\"company\",\"outstanding_amount\",\"docstatus\",\"posting_date\"]&limit_page_length=2000";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray json = response.getJSONArray("data");

                            for (int i = 0; i < json.length(); i++) {
                                JSONObject invoice = (JSONObject) json.get(i);
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
                            }
                            Toast.makeText(getApplicationContext(), "Invoice Done", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Invoice not Done", Toast.LENGTH_SHORT).show();
                        }
                        responseCounter += 1;
                        handleProgressBar();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                responseCounter += 1;
                handleProgressBar();
                Toast.makeText(getApplicationContext(), "Error in receiving Invoice JSON", Toast.LENGTH_SHORT).show();
            }
        }
        );
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);


    }

    public void syncUsers() {
    }

    public void getLoggedUser() {
        String url = loginUrl + "/api/method/frappe.auth.get_logged_user";
    }

    public void login(Context ctx) {
        loginUrl = tvUrl.getText().toString();
        loginPassword = tvLoginPassword.getText().toString();
        loginEmail = tvLoginEmail.getText().toString();


        ac.checkConfigExistence("loginUrl", loginUrl, stDatabase);
        ac.checkConfigExistence("loginPassword", loginPassword, stDatabase);
        ac.checkConfigExistence("loginEmail", loginEmail, stDatabase);

        String url = loginUrl + "/api/method/login";
        JSONObject params = new JSONObject();
        try {
            params.put("usr", loginEmail);
            params.put("pwd", loginPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        siteUrl = loginUrl;
                        Toast.makeText(getApplicationContext(),
                                "Logged in" + response.toString(), Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject json = new JSONObject(response);
                            String fullName = json.getString("full_name");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        responseCounter += 1;
                        handleProgressBar();
                        //login(getApplicationContext());
                        if (stDatabase.stDao().countUsers() != 0) {
                            stDatabase.stDao().deleteAllUsers();
                        }
                        getUserConfig(getApplicationContext());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                responseCounter += 1;
                handleProgressBar();
                Toast.makeText(getApplicationContext(), "Cannot login.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("usr", loginEmail);
                params.put("pwd", loginPassword);
                return params;

            }


        };
        MySingleton.getInstance(ctx).addToRequestQueue(stringRequest);
    }

    public void getSellingPriceList2(Context ctx) {
        String url = loginUrl;
        final TextView textView = findViewById(R.id.tv_responseDisplay);

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        CSVReader reader = new CSVReader(new StringReader(response));
                        List<String[]> records = new ArrayList<>();
                        try {
                            records = reader.readAll();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Iterator<String[]> iterator = records.iterator();

                        int counter = 0;
                        StringBuilder sb = new StringBuilder();
                        while (iterator.hasNext()) {
                            String[] record = iterator.next();
                            if (counter != 0) {
                                Price price = new Price();
                                price.setPrice(Double.valueOf(record[5]));
                                price.setPriceId(record[1]);
                                price.setPriceList(record[2]);
                                price.setProductCode(record[4]);
                                stDatabase.stDao().addPrice(price);

                                /*sb.append(record[4]);
                                sb.append(", ");
                                sb.append(record[5]);
                                sb.append("\n\n");*/
                            }
                            counter++;
                        }
                        //textView.setText(sb.toString());
                        responseCounter += 1;
                        handleProgressBar();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseCounter += 1;
                handleProgressBar();

            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("doctype", "Item Price");
                params.put("fields", "[\"`tabItem Price`.`name`\",\"`tabItem Price`.`price_list`\",\"`tabItem Price`.`currency`\",\"`tabItem Price`.`item_code`\",\"`tabItem Price`.`price_list_rate`\"]");
                params.put("save_user_settings_fields", "1");
                params.put("with_childnames", "1");
                params.put("file_format_type", "CSV");
                params.put("cmd", "frappe.desk.reportview.export_query");
                return params;
            }

        };
        MySingleton.getInstance(ctx).addToRequestQueue(stringRequest);
    }

    public void getSellingPriceList(Context ctx) {
        String url = loginUrl;
        final TextView textView = findViewById(R.id.tv_responseDisplay);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //textView.setText(response);
                        int commaOccurence = 0;
                        String[] cell = new String[6];
                        String sr, name, price, priceList, currency, itemCode, rate;
                        String[] row = response.split("\r\n");
                        String str = "";
                        for (int i = 0; i < row.length; i++) {
                            String findChar = ",";
                            int countOfComma = countChar(row[i], findChar.charAt(0));
                            if (countOfComma > 5) {
                                commaOccurence = 0;
                                findChar = "\"";
                                int countOfQuote = countChar(row[i], findChar.charAt(0));
                                if (countOfQuote > 2) {
                                    Toast.makeText(getApplicationContext(), "Too many special chatacters to deal with - " + row[i], Toast.LENGTH_SHORT).show();
                                } else if (countOfQuote < 2) {
                                    Toast.makeText(getApplicationContext(), "Found just one double quote in ItemPriceList " + row[i], Toast.LENGTH_SHORT).show();
                                } else {
                                    int lengthOfRow = row[i].length();
                                    String cellBuilder = "";
                                    String itemWithComma = "";
                                    boolean foundFirstQuote = false;
                                    String testString;
                                    for (int d = 0; d < lengthOfRow; d++) {
                                        testString = Character.toString(row[i].charAt(d));
                                        if (foundFirstQuote) {
                                            while (!testString.equals("\"")) {
                                                itemWithComma = itemWithComma + testString;
                                                d++;
                                                testString = Character.toString(row[i].charAt(d));
                                            }
                                            foundFirstQuote = false;
                                            cell[4] = itemWithComma;
                                            commaOccurence++;
                                            d++;
                                            continue;
                                        }

                                        if (testString.equals("\"")) {
                                            foundFirstQuote = true;
                                            continue;

                                        } else {


                                            while (!testString.equals(",") & d < row[i].length()) {
                                                cellBuilder = cellBuilder + testString;
                                                d++;
                                                if (d < row[i].length()) {
                                                    testString = Character.toString(row[i].charAt(d));
                                                }
                                            }

                                            cell[commaOccurence] = cellBuilder;
                                            cellBuilder = "";
                                            commaOccurence++;
                                        }


                                    }
                                }
                            } else {
                                findChar = "\"";
                                int countOfQuote = countChar(row[i], findChar.charAt(0));
                                if (countOfQuote > 0) {
                                    Toast.makeText(getApplicationContext(), "Check special characters in " + row[i], Toast.LENGTH_SHORT).show();
                                    continue;

                                } else {

                                    cell = row[i].split(",");
                                    if (cell[2].equals("Standard Selling")) {
                                        String updateItem = cell[4];
                                        Product product = new Product();
                                        product = stDatabase.stDao().getProductByProductCode(cell[4]);
                                        double ssRate = Double.parseDouble(cell[5]);
                                        if (ssRate == 0) {
                                            int x = 1;
                                        }
                                        try {
                                            product.setProductRate(ssRate);
                                        } catch (NullPointerException e) {
                                            int x = 1;
                                        }
                                        stDatabase.stDao().updateProduct(product);
                                        str = str + cell[4];
                                    }
                                }
                            }
                            //str = str + "\n" + row[i];
                        }
                        //textView.setText(str);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textView.setText(error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("doctype", "Item Price");
                params.put("fields", "[\"`tabItem Price`.`name`\",\"`tabItem Price`.`price_list`\",\"`tabItem Price`.`currency`\",\"`tabItem Price`.`item_code`\",\"`tabItem Price`.`price_list_rate`\"]");
                params.put("save_user_settings_fields", "1");
                params.put("with_childnames", "1");
                params.put("file_format_type", "CSV");
                params.put("cmd", "frappe.desk.reportview.export_query");
                return params;
            }

        };

        MySingleton.getInstance(ctx).addToRequestQueue(stringRequest);
    }


    public void getUserConfig(final Context ctx) {
        String url = loginUrl + "/api/resource/User/?fields=[\"email\",\"full_name\",\"send_sms_from_mobile\",\"can_edit_rate\",\"sales_person\"]";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray data = new JSONArray();
                        JSONObject jsonObject = new JSONObject();
                        try {
                            data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                jsonObject = data.getJSONObject(i);
                                String email = jsonObject.getString("email");
                                int sendSms = jsonObject.getInt("send_sms_from_mobile");
                                int allowRateChange = jsonObject.getInt("can_edit_rate");
                                String salesPerson = jsonObject.getString("sales_person");
                                String emailId = jsonObject.getString("email");
                                String fullName = jsonObject.getString("full_name");
                                User user = new User();
                                user.setCanEditRate(allowRateChange);
                                user.setEmailId(emailId);
                                user.setFullName(fullName);
                                user.setSendSms(sendSms);
                                user.setSalesPerson(salesPerson);
                                stDatabase.stDao().addUser(user);
                                if (email.equals(loginEmail)) {
                                    List<UserConfig> userConfigList = stDatabase.stDao().getAllUserConfig();
                                    UserConfig userConfig = userConfigList.get(0);
                                    userConfig.setSendSms(sendSms);
                                    userConfig.setAllowRateChange(allowRateChange);
                                    userConfig.setSalesPerson(salesPerson);
                                    stDatabase.stDao().updateUserConfig(userConfig);
                                    Toast.makeText(ctx, "Fetched UserConfig.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error in parsing UserConfig", Toast.LENGTH_SHORT).show();

                        }
                        responseCounter += 1;
                        handleProgressBar();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx, "Eror in fetching UserConfig", Toast.LENGTH_SHORT).show();
                responseCounter += 1;
                handleProgressBar();
            }
        });
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }


    public int countChar(String str, char c) {
        int count = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c)
                count++;
        }

        return count;
    }

    private void handleProgressBar() {
        if (requestCounter == responseCounter) {
            pbLl.setVisibility(View.GONE);
        }
    }

    private String getCurrentDate() {
        String currentDate;
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = df.format(c);

        return currentDate;
    }

    private void fillLoginInfo() {
        List<UserConfig> userConfigList = stDatabase.stDao().getAllUserConfig();
        UserConfig userConfig = userConfigList.get(0);
        tvUrl.setText(userConfig.getLoginUrl());
        tvLoginEmail.setText(userConfig.getUserId());
        tvLoginPassword.setText(userConfig.getPassword());
    }

    private void saveLoginInfo() {
        if (stDatabase.stDao().countUserConfig() != 0) {
            stDatabase.stDao().deleteAllUserConfig();
        }

        UserConfig userConfig = new UserConfig();
        userConfig.setUserId(loginEmail);
        userConfig.setPassword(loginPassword);
        userConfig.setLoginUrl(loginUrl);
        stDatabase.stDao().addUserConfig(userConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        int smsPermissionGranted = ac.onResultOfRequestPermission(requestCode, permissions, grantResults, this);
    }



}