package com.example.RvOnclick.NetworkOperations;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.RvOnclick.Company;
import com.example.RvOnclick.MySingleton;
import com.example.RvOnclick.Payment;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.UserConfig;
import com.example.RvOnclick.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.android.volley.VolleyLog.TAG;

public class PaymentCreator {
    public interface IOnPaymentPostedListener {
        void onPaymentPosted(int postedStatus, int requestCode, int resultCode, long paymentId,
                             int paymentCount, com.example.RvOnclick.Payment payment, String info);
    }

    public void postPayment(Context ctx, final StDatabase stDatabase, final int requestCode,
                            final IOnPaymentPostedListener listener,
                            List<com.example.RvOnclick.Payment> payments) {
        UserConfig uc = new UserConfig();
        String url = uc.getLoginUrl();
        String info = "";
        int paymentCount = 0;
        if (payments.isEmpty()) {
            info = "No payments in List";
            listener.onPaymentPosted(Utils.UNKNOWN, requestCode, Utils.ARG_LIST_SIZE_0, 0,
                    paymentCount, null, info);
        } else {
            String companyName, companyAbbreviation;
            com.example.RvOnclick.Company company = new Company();

            for (final com.example.RvOnclick.Payment payment : payments) {
                paymentCount += 1;
                JSONObject jsonObjPayment = new JSONObject();
                JSONArray jsonArrReferences = new JSONArray();
                JSONObject jsonObjInvoices = new JSONObject();
                final Long paymentId = payment.getPaymentId();
                Double totalAllocatedAmount = payment.getPaymentAmt();
                String modeOfPayment;
                if (payment.getChequePayment()) {
                    break;
                } else {
                    modeOfPayment = "Cash";

                }

                stDatabase.stDao().updatePaymentStatus(Utils.DOC_STATUS_UNKNOWN, payment.getPaymentId());
                int allocatePayment = 1;
                companyName = payment.getCompany();
                company = stDatabase.stDao().getCompanyByName(companyName);
                companyAbbreviation = company.getAbbr();
                String paidTo = "Cash - " + companyAbbreviation;
                String partyType = "Customer";
                String paidFrom = "Debtors - " + companyAbbreviation;
                String party = payment.getCustomerCode();
                Double receivedAmount = payment.getPaymentAmt();
                String refernceName = payment.getInvoiceNo();
                Double allocatedAmount = payment.getPaymentAmt();
                String referenceDocType = "Sales Invoice";
                String paymentType = "Receive";
                Double paidAmount = payment.getPaymentAmt();
                String appPaymentId = payment.getAppPaymentId();

                try {
                    jsonObjInvoices.put("reference_name", refernceName);
                    jsonObjInvoices.put("allocated_amount", allocatedAmount);
                    jsonObjInvoices.put("reference_doctype", referenceDocType);

                    jsonArrReferences.put(jsonObjInvoices);

                    jsonObjPayment.put("total_allocated_amount", totalAllocatedAmount);
                    jsonObjPayment.put("mode_of_payment", modeOfPayment);
                    jsonObjPayment.put("allocate_payment_amount", allocatePayment);
                    jsonObjPayment.put("paid_to", paidTo);
                    jsonObjPayment.put("party_type", partyType);
                    jsonObjPayment.put("company", companyName);
                    jsonObjPayment.put("paid_from", paidFrom);
                    jsonObjPayment.put("party", party);
                    jsonObjPayment.put("received_amount", receivedAmount);
                    jsonObjPayment.put("references", jsonArrReferences);
                    jsonObjPayment.put("payment_type", paymentType);
                    jsonObjPayment.put("paid_amount", paidAmount);
                    jsonObjPayment.put("app_payment_id", appPaymentId);

                } catch (JSONException e) {
                    e.printStackTrace();
                    info = "Error while creating Payment Json Object";
                    listener.onPaymentPosted(Utils.UNKNOWN, requestCode,
                            Utils.POST_JSON_CREATION_ERROR, paymentId, paymentCount, null, info);
                    //Toast.makeText(getApplicationContext(), "Error in jsonObjPayment", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "postPayments: \n" + jsonObjPayment.toString());
                final int fPaymentCount = paymentCount;
                url = url + "/api/resource/Payment%20Entry/";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        url, jsonObjPayment,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                //Toast.makeText(ctx, "payment done!", Toast.LENGTH_SHORT).show();
                                JSONObject rJson;
                                try {
                                    rJson = response.getJSONObject("Data");
                                    String paymentNumber = rJson.getString("name");
                                    payment.setPaymentNumber(paymentNumber);
                                    payment.setPaymentStatus(Utils.DOC_STATUS_DRAFT);
                                    stDatabase.stDao().updatePayment(payment);
                                    //stDatabase.stDao().updatePaymentNumber(paymentNumber, paymentId);
                                    //stDatabase.stDao().updatePaymentStatus(1, paymentId);
                                    String fInfo = "Successfully posted Payment";
                                    listener.onPaymentPosted(Utils.SUCCESS, requestCode,
                                            Utils.VOLLEY_SUCCESS, paymentId, fPaymentCount,
                                            payment, fInfo);
                                    //Toast.makeText(getApplicationContext(), paymentNumber, Toast.LENGTH_SHORT).show();


                                } catch (JSONException je) {
                                    je.printStackTrace();
                                    String fInfo = "Error while parsing Payment response Json";
                                    listener.onPaymentPosted(Utils.SUCCESS, requestCode,
                                            Utils.ON_RESPONSE_JSON_ERROR, paymentId, fPaymentCount,
                                            payment, fInfo);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        String fInfo = "Volley response error";
                        listener.onPaymentPosted(Utils.UNKNOWN, requestCode, Utils.VOLLEY_ERROR, paymentId, fPaymentCount, payment, fInfo);
                        //Toast.makeText(getApplicationContext(),
                        //      "Cannot post payments!\n" + error.toString(), Toast.LENGTH_SHORT).show();


                    }
                }
                );
                //jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000
                //      , 0, 3000));
                MySingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);

            }
        }
    }

    public void postAllPayments(Context ctx, StDatabase stDatabase, int requestCode,
                                IOnPaymentPostedListener listener) {
        List<Payment> payments = stDatabase.stDao().getUnsyncedPayments();
        postPayment(ctx, stDatabase, requestCode, listener, payments);
    }

}
