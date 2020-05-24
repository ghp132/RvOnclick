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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.android.volley.VolleyLog.TAG;

public class LoginStatusGetter {
    public interface LoginCheckedLister {
        void onLoginChecked(int loggedIn, int requestCode, int resultCode, String info);
    }

    public void checkLoginStatus(final Context ctx, final StDatabase stDatabase, final int requestCode,
                                 final LoginCheckedLister listener) {
        UserConfig uc = stDatabase.stDao().getAllUserConfig().get(0);
        final String siteUrl = uc.getLoginUrl();
        final ApplicationController ac = new ApplicationController();

        String url = siteUrl + "/api/method/frappe.auth.get_logged_user";
        String userEmail;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            listener.onLoginChecked(Utils.SUCCESS, requestCode, Utils.VOLLEY_SUCCESS, message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onLoginChecked(Utils.SUCCESS, requestCode, Utils.ON_RESPONSE_JSON_ERROR, e.toString());
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
                            Log.d(TAG, "onErrorResponse: Error in Checking Login Status: " + body);
                            VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                            errorRecord.setOrgin(ctx.getString(R.string.loginStatus));
                            errorRecord.setErrorBody(body);
                            errorRecord.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                            stDatabase.stDao().addErrorRecord(errorRecord);
                            Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            info = e.toString();
                            listener.onLoginChecked(Utils.FAILURE, requestCode, Utils.VOLLEY_ERROR_BODY_PARSING_ERROR, info);

                        }

                    } else {

                        VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                        errorRecord.setOrgin(ctx.getString(R.string.loginStatus));
                        errorRecord.setErrorBody(info);
                        errorRecord.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                        stDatabase.stDao().addErrorRecord(errorRecord);
                        Toast.makeText(ctx, "See error log.", Toast.LENGTH_SHORT).show();


                        info = "Networkresponse is null" + info;
                        listener.onLoginChecked(Utils.FAILURE, requestCode, Utils.NETWORKRESPONSE_IS_NULL, info);
                    }
                }
            }
        }
        );
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);

    }
}
