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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.android.volley.VolleyLog.TAG;

public class Territory {
    public interface territoryReceivedListener {
        void onTerritoryReceived(int synced, int requestCode, int resultCode, String info);
    }

    public void getTerritory(final Context ctx, final StDatabase stDatabase, final int requestCode,
                             final territoryReceivedListener listener) {
        UserConfig uc = stDatabase.stDao().getAllUserConfig().get(0);
        final String siteUrl = uc.getLoginUrl();
        final ApplicationController ac = new ApplicationController();

        final int pageLimitLength = Utils.TERRITORY_PAGE_LIMIT_LENGTH;

        String url = siteUrl + "/api/resource/Territory?fields=[\"*\"]&limit_page_length=" +
                pageLimitLength;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray json = response.getJSONArray("data");

                            for (int i = 0; i < json.length(); i++) {
                                JSONObject territoryObj = (JSONObject) json.get(i);
                                String territoryName = territoryObj.getString("territory_name");
                                boolean isGroup = (territoryObj.getString("is_group").equals("1"));
                                String parentTerritory = territoryObj.getString("parent_territory");

                                com.example.RvOnclick.Territory territory = new com.example.RvOnclick.Territory();
                                territory.setTerritoryName(territoryName);
                                territory.setGroup(isGroup);
                                territory.setParentTerritory(parentTerritory);

                                stDatabase.stDao().addTerritory(territory);
                            }
                            String info = "Territory Updated";
                            listener.onTerritoryReceived(Utils.SUCCESS, requestCode, Utils.VOLLEY_SUCCESS, info);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String info = "Error in parsing Territory Json";
                            listener.onTerritoryReceived(Utils.SUCCESS, requestCode, Utils.VOLLEY_ERROR_RESPONSE_BODY, info);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String body = error.toString();
                error.printStackTrace();
                String info = body;
                //listener.onTerritoryReceived(Utils.FAILURE,requestCode,Utils.VOLLEY_ERROR,info);
                if (error.networkResponse != null) {
                    if (error.networkResponse.data != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.d(TAG, "onErrorResponse: Error in receiving territory" + body);
                            VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                            errorRecord.setOrgin(ctx.getString(R.string.territory));
                            errorRecord.setErrorBody(body);
                            errorRecord.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                            stDatabase.stDao().addErrorRecord(errorRecord);
                            Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            info = e.toString();
                            listener.onTerritoryReceived(Utils.FAILURE, requestCode, Utils.VOLLEY_ERROR_BODY_PARSING_ERROR, info);

                        }
                    }
                } else {

                    VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                    errorRecord.setOrgin("Territory");
                    errorRecord.setErrorBody(info);
                    errorRecord.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                    stDatabase.stDao().addErrorRecord(errorRecord);
                    Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();


                    info = "Networkresponse is null" + info;
                    listener.onTerritoryReceived(Utils.FAILURE, requestCode, Utils.NETWORKRESPONSE_IS_NULL, info);
                }
            }
        }
        );
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);

    }
}
