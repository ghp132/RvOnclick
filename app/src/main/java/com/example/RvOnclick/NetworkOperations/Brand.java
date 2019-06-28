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
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.UserConfig;
import com.example.RvOnclick.Utils;
import com.example.RvOnclick.VolleyErrorRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.android.volley.VolleyLog.TAG;

public class Brand {
    public interface brandReceivedListener {
        void onBrandReceived(int synced, int requestCode, int resultCode, String info);
    }

    public void getBrands(final Context ctx, final StDatabase stDatabase, final int requestCode,
                          final brandReceivedListener listener) {

        UserConfig uc = stDatabase.stDao().getAllUserConfig().get(0);
        final String siteUrl = uc.getLoginUrl();
        final ApplicationController ac = new ApplicationController();

        final int pageLimitLength = Utils.BRAND_PAGE_LIMIT_LENGTH;

        String url = siteUrl + "/api/resource/Brand?fields=[\"*\"]&limit_page_length=" +
                pageLimitLength;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray json = response.getJSONArray("data");

                            for (int i = 0; i < json.length(); i++) {
                                JSONObject brandObj = (JSONObject) json.get(i);
                                String brandName = brandObj.getString("name");

                                com.example.RvOnclick.Brand brand = new com.example.RvOnclick.Brand();
                                brand.setBrandName(brandName);

                                stDatabase.stDao().addBrand(brand);
                            }
                            String info = "Brand Updated";
                            listener.onBrandReceived(Utils.SUCCESS, requestCode, Utils.VOLLEY_SUCCESS, info);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String info = "Error in parsing Brand Json";
                            listener.onBrandReceived(Utils.SUCCESS, requestCode, Utils.VOLLEY_ERROR_RESPONSE_BODY, info);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body = error.toString();
                error.printStackTrace();
                String info = body;
                if (error.networkResponse != null) {
                    if (error.networkResponse.data != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.d(TAG, "onErrorResponse: Error in receiving Brand" + body);
                            VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                            errorRecord.setOrgin("Brand");
                            errorRecord.setErrorBody(body);
                            errorRecord.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                            stDatabase.stDao().addErrorRecord(errorRecord);
                            Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            info = e.toString();
                            listener.onBrandReceived(Utils.FAILURE, requestCode, Utils.VOLLEY_ERROR_BODY_PARSING_ERROR, info);

                        }
                    }
                } else {

                    VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                    errorRecord.setOrgin("Brand");
                    errorRecord.setErrorBody(info);
                    errorRecord.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                    stDatabase.stDao().addErrorRecord(errorRecord);
                    Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();


                    info = "Networkresponse is null" + info;
                    listener.onBrandReceived(Utils.FAILURE, requestCode, Utils.NETWORKRESPONSE_IS_NULL, info);
                }
            }
        }
        );
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);

    }

}
