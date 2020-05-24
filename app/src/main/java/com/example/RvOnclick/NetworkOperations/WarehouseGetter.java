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
import com.example.RvOnclick.R;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.UserConfig;
import com.example.RvOnclick.Utils;
import com.example.RvOnclick.VolleyErrorRecord;
import com.example.RvOnclick.Warehouse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.android.volley.VolleyLog.TAG;

public class WarehouseGetter {
    public interface warehouseReceivedListener {
        void onWarehouseReceived(int synced, int requestCode, int resultCode, String info);
    }

    public void getWarehouse(final Context ctx, final StDatabase stDatabase, final int requestCode,
                             final WarehouseGetter.warehouseReceivedListener listener) {
        UserConfig uc = stDatabase.stDao().getAllUserConfig().get(0);
        final String siteUrl = uc.getLoginUrl();
        final ApplicationController ac = new ApplicationController();

        final int pageLimitLength = Utils.WAREHOUSE_PAGE_LIMIT_LENGTH;

        String url = siteUrl + "/api/resource/Warehouse?fields=[\"*\"]&limit_page_length=" +
                pageLimitLength;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray json = response.getJSONArray("data");

                            for (int i = 0; i < json.length(); i++) {
                                JSONObject warehouseObj = (JSONObject) json.get(i);
                                String name = warehouseObj.getString("name");
                                boolean isGroup = (warehouseObj.getString("is_group").equals("1"));
                                String parentWarehouse = warehouseObj.getString("parent_warehouse");
                                String warehouseName = warehouseObj.getString("warehouse_name");
                                String companyName = warehouseObj.getString("company");

                                Warehouse warehouse = new Warehouse();
                                warehouse.setName(name);
                                warehouse.setCompany(companyName);
                                warehouse.setGroup(isGroup);
                                warehouse.setParentWarehouse(parentWarehouse);
                                warehouse.setWarehouseName(warehouseName);

                                stDatabase.stDao().addWarehouse(warehouse);
                            }
                            String info = "Warehouse Updated";
                            listener.onWarehouseReceived(Utils.SUCCESS, requestCode, Utils.VOLLEY_SUCCESS, info);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String info = "Error in parsing Territory Json";
                            listener.onWarehouseReceived(Utils.SUCCESS, requestCode, Utils.VOLLEY_ERROR_RESPONSE_BODY, info);
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
                            Log.d(TAG, "onErrorResponse: Error in receiving warehouse" + body);
                            VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                            errorRecord.setOrgin(ctx.getString(R.string.warehouse));
                            errorRecord.setErrorBody(body);
                            errorRecord.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                            stDatabase.stDao().addErrorRecord(errorRecord);
                            Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            info = e.toString();
                            listener.onWarehouseReceived(Utils.FAILURE, requestCode, Utils.VOLLEY_ERROR_BODY_PARSING_ERROR, info);

                        }
                    }
                } else {

                    VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                    errorRecord.setOrgin(ctx.getString(R.string.warehouse));
                    errorRecord.setErrorBody(info);
                    errorRecord.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                    stDatabase.stDao().addErrorRecord(errorRecord);
                    Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();
                    info = "Networkresponse is null" + info;
                    listener.onWarehouseReceived(Utils.FAILURE, requestCode, Utils.NETWORKRESPONSE_IS_NULL, info);
                }
            }
        }
        );
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }
}
