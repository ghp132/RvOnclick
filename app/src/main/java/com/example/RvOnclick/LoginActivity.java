package com.example.RvOnclick;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    EditText tvLoginEmail, tvLoginPassword, tvUrl;
    Button btLogin, btGetData, btNextActivity, btPostSampleData;
    String loginEmail, loginPassword, loginUrl, siteUrl;
    public static StDatabase stDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvLoginEmail = (EditText) findViewById(R.id.tv_loginEmail);
        tvLoginPassword = (EditText) findViewById(R.id.tv_loginPassword);
        btLogin = (Button) findViewById(R.id.bt_login);
        btGetData = findViewById(R.id.bt_getData);
        btNextActivity = findViewById(R.id.bt_nextActivity);
        btPostSampleData = findViewById(R.id.bt_postSampleData);
        tvUrl = findViewById(R.id.tv_url);
        stDatabase = Room.databaseBuilder(getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();


        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);
        final CookieStore cookieStore = manager.getCookieStore();

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEmail = tvLoginEmail.getText().toString();
                loginPassword = tvLoginPassword.getText().toString();
                loginUrl = tvUrl.getText().toString();

                String url = loginUrl + "/api/method/login";
                RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                siteUrl = loginUrl;
                                Toast.makeText(getApplicationContext(),
                                        "Logged in" + response, Toast.LENGTH_SHORT).show();

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(), "That didn't work.", Toast.LENGTH_SHORT).show();
                    }
                }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("usr", loginEmail);
                        params.put("pwd", loginPassword);
                        return params;

                    }
                };
                requestQueue.add(stringRequest);


            }
        });

        btPostSampleData.setOnClickListener(new View.OnClickListener() {
            private JSONObject getParams() {
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put("item_code", "FL Jelley Tray");
                    jsonObj.put("qty", 1);
                    jsonObj.put("stock_uom", "Nos");
                    jsonObj.put("warehouse", "Stores - HC");
                    jsonObj.put("item_name", "FL Jelley Tray");
                    jsonObj.put("rate", "100");
                    jsonObj.put("amount", 100);
                    jsonObj.put("base_rate", "100");
                    jsonObj.put("delivery_date", "2018-11-30");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(jsonObj);
                JSONObject params = new JSONObject();
                try {
                    params.put("selling_price_list", "Standard Selling");
                    params.put("posting_date", "2018-10-22");
                    params.put("due_date", "2018-11-30");
                    params.put("order_type", "Sales");
                    params.put("delivery_date", "2018-11-30");
                    params.put("customer", "5 Star Agencies");
                    params.put("items", jsonArray);
                    params.put("is_pos", 0);
                    params.put("conversion_rate", 1);
                    params.put("customer_name", "Swathi Stores");
                    params.put("commission_rate", 0);
                    params.put("company", "Hari Company");
                    params.put("paid_amount", 0);
                    params.put("remarks", "No Remarks");
                    params.put("plc_conversion_rate", 1);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return params;
            }

            @Override
            public void onClick(View v) {
                String url = loginUrl + "/api/resource/Sales Order/";
                RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        url, getParams(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                siteUrl = loginUrl;
                                Toast.makeText(getApplicationContext(),
                                        "Logged in" + response, Toast.LENGTH_SHORT).show();

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(), "That didn't work.", Toast.LENGTH_SHORT).show();
                    }
                }
                ) {

                };
                requestQueue.add(jsonObjectRequest);
            }
        });

        btGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = siteUrl + "/api/resource/Item?fields=[\"*\"]&limit_page_length=1000";
                RequestQueue requesQueue = Volley.newRequestQueue(LoginActivity.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray json = response.getJSONArray("data");

                                    for (int i = 0; i < json.length(); i++) {
                                        JSONObject item = (JSONObject) json.get(i);
                                        String product_name = item.getString("item_name");
                                        String brand = item.getString("brand");
                                        String disabled = item.getString("disabled");
                                        Boolean disabledB = Boolean.parseBoolean(disabled);
                                        String product_code = item.getString("item_code");
                                        String product_group = item.getString("item_group");

                                        Product product = new Product();
                                        product.setProductCode(product_code);
                                        product.setProductName(product_name);
                                        product.setProductBrand(brand);
                                        product.setProductDisabled(disabledB);
                                        product.setProductGroup(product_group);

                                        stDatabase.stDao().addProduct(product);
                                    }
                                    Toast.makeText(getApplicationContext(), "DB Done", Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "DB not Done", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error in receiving JSON", Toast.LENGTH_SHORT).show();
                    }
                }
                );
                requesQueue.add(jsonObjectRequest);

                //---------------------------------------------------------------------------------------

                url = siteUrl + "/api/resource/Customer?fields=[\"*\"]&limit_page_length=1000";
                requesQueue = Volley.newRequestQueue(LoginActivity.this);
                jsonObjectRequest = new JsonObjectRequest(
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
                                        Boolean disabledB = Boolean.parseBoolean(disabled);
                                        String customer_id = customer.getString("name");
                                        String customer_group = customer.getString("customer_group");

                                        Customer dbCustomer = new Customer();
                                        dbCustomer.setCustomer_name(customer_name);
                                        dbCustomer.setTerritory(territory);
                                        dbCustomer.setCustomer_disabled(disabledB);
                                        dbCustomer.setCustomer_id(customer_id);
                                        dbCustomer.setCustomer_group(customer_group);


                                        stDatabase.stDao().addCustomer(dbCustomer);
                                    }
                                    Toast.makeText(getApplicationContext(), "DB Done", Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "DB not Done", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error in receiving JSON", Toast.LENGTH_SHORT).show();
                    }
                }
                );
                requesQueue.add(jsonObjectRequest);


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
}
