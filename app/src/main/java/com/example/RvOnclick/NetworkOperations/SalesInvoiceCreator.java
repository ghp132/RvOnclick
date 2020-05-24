package com.example.RvOnclick.NetworkOperations;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.RvOnclick.ApplicationController;
import com.example.RvOnclick.Invoice;
import com.example.RvOnclick.MySingleton;
import com.example.RvOnclick.Order;
import com.example.RvOnclick.OrderProduct;
import com.example.RvOnclick.R;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.User;
import com.example.RvOnclick.UserConfig;
import com.example.RvOnclick.Utils;
import com.example.RvOnclick.VolleyErrorRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SalesInvoiceCreator {

    private static String TAG = "SalesInvoiceCreator";

    private int noOfProcessedInvoices = 0;

    public interface IOnInvoicePostedListener {
        void onInvoicePosted(int postedStatus, int requestCode, int resultCode, long orderId,
                             int noOfInvoices, int invoiceCount, Invoice postedInvoice,
                             String info);
    }

    public void postInvoices(final Context ctx, final StDatabase stDatabase, final int requestCode,
                             final IOnInvoicePostedListener listener, final Order orderToPost) {
        JSONObject params = new JSONObject();
        final ApplicationController ac = new ApplicationController();

        //getting values from preferences
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(Utils.MAIN_SETTINGS, Context.MODE_PRIVATE);
        int defDocStatus = Integer.valueOf(sharedPreferences.getString("pref_def_invoiceStatus", "1"));

        List<Order> orderList = new ArrayList<>();
        //adding order to ArrayList because of having already coded with ArrayList
        orderList.add(orderToPost);

        JSONArray taxes = new JSONArray();
        UserConfig uc = stDatabase.stDao().getAllUserConfig().get(0);

        List<Order> splitOrderList = new ArrayList<>();

        //splitting items in the Order by company and creating a new arraylist of orders
        for (Order order : orderList) {
            List<Long> splitOrderIdList = ac.splitProductListByCompany(order, stDatabase);
            splitOrderList.addAll(ac.createSplitOrderList(splitOrderIdList, stDatabase));
        }
        orderList.clear();
        orderList.addAll(splitOrderList);
        final int noOfInvoices = orderList.size();
        //int invoiceCount = 0;

        for (final Order order : orderList) {
            noOfProcessedInvoices += 1;
            JSONArray salesTeamArray = new JSONArray();

            String appOrderId = order.getAppOrderId();
            //todo: add naming series from company
            final Long orderId = order.getOrderId();
            String priceListName = order.getPriceListName();
            if (priceListName == null || priceListName.equals("null")) {
                priceListName = "Standard Selling";
            }
            String territory = order.getTerritory();
            //CustomField sales person to be added in User Doctype
            //UserConfig contains only one row as of 15/05/2019


            String userEmail = uc.getUserId();
            User u = stDatabase.stDao().getUserByEmailId(userEmail);
            String salesPerson = u.getSalesPerson();
            String company = order.getCompanyName();

            String custCode = order.getCustomerCode();
            Calendar calendar = Calendar.getInstance();
            String deliveryDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + (calendar.get(Calendar.DAY_OF_MONTH));

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
                String info = "Error while creating salesPerson JSONObject";
                listener.onInvoicePosted(Utils.UNKNOWN, requestCode, Utils.POST_JSON_CREATION_ERROR,
                        orderId, noOfInvoices, noOfProcessedInvoices, null, info);
            }


            List<OrderProduct> orderProducts = stDatabase.stDao().getOrderProductsById(orderId);
            int noOfOrderProducts = orderProducts.size();
            int countOfOrderProducts = 0;
            JSONArray jsonArray = new JSONArray();
            for (OrderProduct orderProduct : orderProducts) {
                countOfOrderProducts += 1;
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
                    jsonObject.put("cost_center", costCenter);
                } catch (JSONException e) {
                    e.printStackTrace();
                    String info = "Error while creating Item JSON Object at position :" + countOfOrderProducts + " ItemCode: " + itemCode;
                    listener.onInvoicePosted(Utils.UNKNOWN, requestCode,
                            Utils.POST_JSON_CREATION_ERROR, orderId, noOfInvoices, noOfProcessedInvoices,
                            null, info);
                }
                jsonArray.put(jsonObject);
            }
            order.setOrderStatus(0);
            stDatabase.stDao().updateOrder(order);
            try {
                params.put("customer", custCode);
                params.put("territory", territory);
                params.put("selling_price_list", priceListName);
                params.put("company", company);
                params.put("selling_price_list", priceListName);
                params.put("items", jsonArray);
                params.put("app_invoice_id", appOrderId);
                params.put("update_stock", "1");
                params.put("docstatus", defDocStatus);


                taxes = ac.getTaxArray(orderId, stDatabase);
                params.put("taxes", taxes);
                params.put("sales_team", salesTeamArray);


            } catch (JSONException e) {
                e.printStackTrace();
                String info = "Error while creating Invoice JSON Object";
                listener.onInvoicePosted(Utils.UNKNOWN, requestCode, Utils.POST_JSON_CREATION_ERROR,
                        orderId, noOfInvoices, noOfProcessedInvoices, null, info);

            }
            Log.d(TAG, "postInvoices: params: " + params.toString());
            final String loginUrl = uc.getLoginUrl();
            String url = loginUrl + "/api/resource/Sales%20Invoice/";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    url, params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //adding erpnext Order number to the order database table
                            JSONObject rJson;
                            try {
                                rJson = response.getJSONObject("data");
                                String orderNumber = rJson.getString("name");
                                stDatabase.stDao().updateOrderNumber(orderNumber, orderId);
                                stDatabase.stDao().updateOrderStatus(1, orderId);
                                Toast.makeText(ctx, orderNumber, Toast.LENGTH_SHORT).show();

                                //populating invoices table
                                Invoice newInvoice = ac.parseInvoiceJson(rJson);
                                stDatabase.stDao().createInvoice(newInvoice);
                                //ac.updatePaymentFromOrder(orderId, stDatabase);
                                String info = "Invoice done";

                                //updating outstanding invoice info
                                ac.updateInvoiceAndPayment(rJson, orderId, stDatabase);

                                listener.onInvoicePosted(Utils.SUCCESS, requestCode,
                                        Utils.VOLLEY_SUCCESS, orderId, noOfInvoices,
                                        noOfProcessedInvoices, newInvoice, info);

                            } catch (JSONException je) {
                                je.printStackTrace();
                                String info = "Error while parsing Posted Invoice JSON Response";
                                listener.onInvoicePosted(Utils.SUCCESS, requestCode,
                                        Utils.ON_RESPONSE_JSON_ERROR, orderId, noOfInvoices,
                                        noOfProcessedInvoices, null, info);
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    String info = "Volley error";
                    stDatabase.stDao().updateOrderStatus(Utils.UNKNOWN, orderId);
                    listener.onInvoicePosted(Utils.UNKNOWN, requestCode,
                            Utils.VOLLEY_ERROR, orderId, noOfInvoices,
                            noOfProcessedInvoices, null, info);
                    List<Order> orderList1 = new ArrayList<>();
                    orderList1.add(order);
                    //Toast.makeText(ctx, "Updating Invoice Status" + error.toString(), Toast.LENGTH_SHORT).show();

                    if (error.networkResponse != null) {
                        if (error.networkResponse.data != null) {
                            String body;
                            try {
                                body = new String(error.networkResponse.data, "UTF-8");
                                //tvResponseDisplay.setText(body);

                                VolleyErrorRecord errorRecord = new VolleyErrorRecord();
                                errorRecord.setOrgin(ctx.getString(R.string.salesInvoiceCreation));
                                errorRecord.setErrorBody(body);
                                errorRecord.setTimeStamp(ac.createTimeStamp("yyyy:MM:dd-HH:mm:ss:SSS"));
                                stDatabase.stDao().addErrorRecord(errorRecord);

                                listener.onInvoicePosted(Utils.FAILURE, requestCode,
                                        Utils.VOLLEY_ERROR_RESPONSE_BODY, orderId, noOfInvoices,
                                        noOfProcessedInvoices, null, body);

                                Log.d(TAG, "onErrorResponse: postInvoices" + body);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                info = "Unsupported Encoding Exception while parsing error response body";
                                listener.onInvoicePosted(Utils.UNKNOWN, requestCode,
                                        Utils.VOLLEY_ERROR_BODY_PARSING_ERROR, orderId, noOfInvoices,
                                        noOfProcessedInvoices, null, info);
                                Toast.makeText(ctx, "UnsupportedEncodingException", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
            );//jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(80000,
            //3,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
        }

    }
}
