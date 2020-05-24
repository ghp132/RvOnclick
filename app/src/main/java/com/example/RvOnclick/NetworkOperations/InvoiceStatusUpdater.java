package com.example.RvOnclick.NetworkOperations;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.RvOnclick.ApplicationController;
import com.example.RvOnclick.MySingleton;
import com.example.RvOnclick.Order;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.UserConfig;
import com.example.RvOnclick.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class InvoiceStatusUpdater {
    public interface invoiceStatusUpdateListener {
        void onInvoiceStatusUpdated(int updated, int requestCode, int resultCode, List<Order> orders);
    }

    public void updateInvoiceStatus(Context ctx, final StDatabase stDatabase, final int requestCode,
                                    final invoiceStatusUpdateListener listener, final List<Order> orders) {
        UserConfig uc = new UserConfig();
        final String siteUrl = uc.getLoginUrl();
        final ApplicationController ac = new ApplicationController();

        final int pageLimitLength = Utils.SALES_INVOICE_PAGE_LIMIT_LENGTH;
        //int noOfOrders = orders.size();

        String url = siteUrl + "/api/resource/Sales%20Invoice?fields=[\"name\",\"app_invoice_id\"," +
                "\"docstatus\",\"customer\",\"rounded_total\",\"company\",\"outstanding_amount\"," +
                "\"posting_date\"]&limit_page_length=" + pageLimitLength;

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

                        for (Order order : orders) {
                            String mobileAppOrderId = order.getAppOrderId();
                            for (int t = 0; t <= pageLimitLength - 1; t++) {
                                try {
                                    jsonObject = jsonArray.getJSONObject(t);
                                    String appOrderId = jsonObject.getString("app_invoice_id");
                                    String orderNumber = jsonObject.getString("name");
                                    if (appOrderId.equals(mobileAppOrderId)) {
                                        if (jsonObject.getInt("docstatus") == 1) {
                                            order.setOrderStatus(Utils.DOC_STATUS_SUBMITTED);
                                            order.setOrderNumber(orderNumber);
                                            stDatabase.stDao().updateOrder(order);
                                            ac.updatePaymentFromOrder(order.getOrderId(), stDatabase);
                                            stDatabase.stDao().createInvoice((ac.parseInvoiceJson(jsonObject)));

                                            break;
                                        } else {
                                            order.setOrderStatus(Utils.DOC_STATUS_DRAFT);
                                            order.setOrderNumber(orderNumber);
                                            stDatabase.stDao().updateOrder(order);
                                            //ac.updatePaymentFromOrder(order.getOrderId(), stDatabase);
                                            stDatabase.stDao().createInvoice(ac.parseInvoiceJson(jsonObject));
                                            break;
                                        }
                                    } else {
                                        order.setOrderStatus(Utils.DOC_STATUS_UNATTEMPTED);
                                        stDatabase.stDao().updateOrder(order);
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();

                                }
                            }
                        }
                        //Toast.makeText(getApplicationContext(), "Invoice Status Updated", Toast.LENGTH_SHORT).show();
                        listener.onInvoiceStatusUpdated(Utils.SUCCESS, requestCode, Utils.VOLLEY_SUCCESS, orders);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                listener.onInvoiceStatusUpdated(Utils.FAILURE, requestCode, Utils.VOLLEY_ERROR, orders);
                //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);

    }

}
