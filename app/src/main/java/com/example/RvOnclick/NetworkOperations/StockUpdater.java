package com.example.RvOnclick.NetworkOperations;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.RvOnclick.MySingleton;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.UserConfig;
import com.example.RvOnclick.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.RvOnclick.AllFragements.ProductListFragment.TAG;

public class StockUpdater {
    public interface StockUpdateListener {
        void onStockUpdated(int stockUpdated, int requestCode, int resultCode, String info);
    }

    public void UpdateStock(final Context ctx, final StDatabase stDatabase, final int requestCode,
                            final StockUpdateListener listener, String fromDate, String toDate, String warehouse) {

        UserConfig uc = stDatabase.stDao().getAllUserConfig().get(0);
        final String url = uc.getLoginUrl();
        final String cmd = "frappe.desk.query_report.run";


        String filterCriteria = "\"from_date\":\"" + fromDate + "\",\"to_date\":\"" + toDate + "\"";
        final String report_name = "Stock Balance";

        if (warehouse != null) {
            filterCriteria = filterCriteria + ",\"warehouse\":\"" + warehouse + "\"";
        }
        final String filters = "{" + filterCriteria + "}";

/*
        JSONObject params = new JSONObject();
        try {
            params.put("cmd", cmd);
            params.put("filters",filters);
            params.put("report_name",report_name);
        } catch (JSONException e){
            e.printStackTrace();
            listener.onStockUpdated(Utils.FAILURE,requestCode,Utils.POST_JSON_CREATION_ERROR,e.toString());
        }
*/
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseJson = new JSONObject(response);
                            listener.onStockUpdated(Utils.SUCCESS, requestCode, Utils.VOLLEY_SUCCESS, response);
                            Log.d(TAG, "onResponse: " + response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onStockUpdated(Utils.FAILURE, requestCode, Utils.ON_RESPONSE_JSON_ERROR, e.toString());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onStockUpdated(Utils.FAILURE, requestCode, Utils.VOLLEY_ERROR, error.toString());
                Log.d(TAG, "onErrorResponse: " + error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("cmd", cmd);
                params.put("filters", filters);
                params.put("report_name", report_name);
                return params;
            }
        };
        MySingleton.getInstance(ctx).addToRequestQueue(stringRequest);
    }
}
