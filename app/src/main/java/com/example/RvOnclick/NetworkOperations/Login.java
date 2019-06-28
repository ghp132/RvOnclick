package com.example.RvOnclick.NetworkOperations;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.RvOnclick.ApplicationController;
import com.example.RvOnclick.MySingleton;
import com.example.RvOnclick.StDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class Login {

    public interface ILogin {
        public void onLoginResponseListener(boolean loggedIn, int requestCode, int resultCode, String info);
    }

    public void login(final Context ctx, final StDatabase stDatabase, final int requestCode,
                      final ILogin iLogin, final String loginUrl,
                      final String loginEmail, final String loginPassword) {

        ApplicationController ac = new ApplicationController();

        ac.checkConfigExistence("loginUrl", loginUrl, stDatabase);
        ac.checkConfigExistence("loginPassword", loginPassword, stDatabase);
        ac.checkConfigExistence("loginEmail", loginEmail, stDatabase);

        String url = loginUrl + "/api/method/login";

        JSONObject params = new JSONObject();
        try {
            params.put("usr", loginEmail);
            params.put("pwd", loginPassword);
        } catch (
                JSONException e) {
            e.printStackTrace();
        }
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String fullName = "";
                        Toast.makeText(ctx,
                                "Logged in" + response.toString(), Toast.LENGTH_SHORT).show();

                        try {
                            JSONObject json = new JSONObject(response);
                            fullName = json.getString("full_name");

                        } catch (JSONException e) {
                            iLogin.onLoginResponseListener(true, requestCode, 1, e.toString());
                            e.printStackTrace();
                        }

                        if (stDatabase.stDao().countUsers() != 0) {
                            stDatabase.stDao().deleteAllUsers();
                        }
                        iLogin.onLoginResponseListener(true, requestCode, 2, fullName);
                        //getUserConfig(getApplicationContext());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(ctx, "Cannot login.", Toast.LENGTH_SHORT).show();
                iLogin.onLoginResponseListener(false, requestCode, 3, error.toString());

                if (error.networkResponse != null) {
                    if (error.networkResponse.data != null) {
                        String body;
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            //tvResponseDisplay.setText(body);
                            iLogin.onLoginResponseListener(false, requestCode, 4, body);
                            Log.d(TAG, "onErrorResponse: Cannot Login" + body);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            Toast.makeText(ctx, "UnsupportedEncodingException", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

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
}
