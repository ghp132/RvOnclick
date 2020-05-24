package com.example.RvOnclick.NetworkOperations;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.RvOnclick.ApplicationController;
import com.example.RvOnclick.MySingleton;
import com.example.RvOnclick.ProductsListItem;
import com.example.RvOnclick.R;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.StockEntry;
import com.example.RvOnclick.UserConfig;
import com.example.RvOnclick.Utils;
import com.example.RvOnclick.VolleyErrorRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

//import static com.android.volley.VolleyLog.TAG;

public class StockEntryCreator {
    public interface StockEntryPostedListener {
        void onStockEntryPosted(int synced, int requestCode, int resultCode, String info);
    }

    private static final String TAG = "StockEntryCreator";

    public void postStockEntry(final Context ctx,
                               final StDatabase stDatabase, final int requestCode,
                               final StockEntryPostedListener listener, final StockEntry stockEntryToPost, String purpose) {
        UserConfig uc = stDatabase.stDao().getAllUserConfig().get(0);
        final String siteUrl = uc.getLoginUrl();
        final ApplicationController ac = new ApplicationController();


        //fetching data from db
        List<ProductsListItem> itemList = stDatabase.stDao().getProductsListItemByProductsListId(stockEntryToPost.getId());
        String sourceWarehouse = stockEntryToPost.getSourceWarehouse();
        String targetWarehouse = stockEntryToPost.getTargetWarehouse();
        String companyName = stockEntryToPost.getCompany();

        //creating json object
        JSONObject jsonObject = new JSONObject();
        JSONArray itemJsonArr = new JSONArray();

        //creating items json array
        for (ProductsListItem item : itemList) {
            JSONObject itemJsonObj = new JSONObject();
            double stockUomQty = item.getQty() * item.getUomConversion();
            try {
                itemJsonObj.put("s_warehouse", sourceWarehouse);
                itemJsonObj.put("t_warehouse", targetWarehouse);
                itemJsonObj.put("item_code", item.getProductCode());
                itemJsonObj.put("qty", item.getQty());
                itemJsonObj.put("conversion_factor", item.getUomConversion());
                itemJsonArr.put(itemJsonObj);

            } catch (JSONException e) {
                e.printStackTrace();
                String info = "Json Array Error at " + item.getProductName();
                listener.onStockEntryPosted(Utils.FAILURE, requestCode, Utils.POST_JSON_CREATION_ERROR, info);
            }

        }
        Log.d(TAG, "postStockEntry: itemJsonArr: " + itemJsonArr.toString());

        try {
            jsonObject.put("docstatus", 1);
            jsonObject.put("company", companyName);
            jsonObject.put("purpose", purpose);
            jsonObject.put("app_stock_entry_id", stockEntryToPost.getUniqueValue());
            jsonObject.put("items", itemJsonArr);
        } catch (JSONException e) {
            String info = "Json Object Error";
            listener.onStockEntryPosted(Utils.FAILURE, requestCode, Utils.POST_JSON_CREATION_ERROR, info);
        }
        String url = siteUrl + "/api/resource/Stock%20Entry";
        Log.d(TAG, "postStockEntry: jsonObject: " + jsonObject);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                JSONObject rJson = new JSONObject();
                String stockEntryNumber = "";
                try {
                    stockEntryNumber = rJson.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String info = stockEntryNumber + " : Stock Transfer Successful";
                listener.onStockEntryPosted(Utils.SUCCESS, requestCode, Utils.VOLLEY_SUCCESS, info);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body = error.toString();
                error.printStackTrace();
                String info = body;
                Log.d(TAG, "onErrorResponse: info: " + info);
                if (error.networkResponse != null) {
                    if (error.networkResponse.data != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.d(TAG, "onErrorResponse: Error in making Stock Entry: " + body);
                            VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                            errorRecord.setOrgin(ctx.getString(R.string.stockEntry));
                            errorRecord.setErrorBody(body);
                            errorRecord.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                            stDatabase.stDao().addErrorRecord(errorRecord);
                            listener.onStockEntryPosted(Utils.FAILURE, requestCode, Utils.VOLLEY_ERROR_RESPONSE_BODY, info);
                            Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            info = e.toString();
                            listener.onStockEntryPosted(Utils.FAILURE, requestCode, Utils.VOLLEY_ERROR_BODY_PARSING_ERROR, info);
                        }
                    } else {
                        VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                        errorRecord.setOrgin(ctx.getString(R.string.loginStatus));
                        errorRecord.setErrorBody(info);
                        errorRecord.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                        stDatabase.stDao().addErrorRecord(errorRecord);
                        Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();
                        info = "Networkresponse is null" + info;
                        listener.onStockEntryPosted(Utils.FAILURE, requestCode, Utils.NETWORKRESPONSE_IS_NULL, info);
                    }
                    listener.onStockEntryPosted(Utils.FAILURE, requestCode, Utils.NETWORKRESPONSE_IS_NULL, info);
                }
            }
        }
        );
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

}
