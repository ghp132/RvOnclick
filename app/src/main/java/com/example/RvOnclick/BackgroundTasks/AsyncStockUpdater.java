package com.example.RvOnclick.BackgroundTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.RvOnclick.Product;
import com.example.RvOnclick.Rv1Item;
import com.example.RvOnclick.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.RvOnclick.AllFragements.ProductListFragment.TAG;

public class AsyncStockUpdater {

    private List<Rv1Item> rv1ItemList = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();
    private int inputType;
    private String stockBalanceReport;
    private AsyncStockUpdateListener listener;
    private Context ctx;

    public AsyncStockUpdater(Context ctx, List<Rv1Item> rv1ItemList, List<Product> productList, int inputType,
                             String stockBalanceReport, AsyncStockUpdateListener listener) {
        this.rv1ItemList = rv1ItemList;
        this.productList = productList;
        this.inputType = inputType;
        this.stockBalanceReport = stockBalanceReport;
        this.listener = listener;
        this.ctx = ctx;
    }

    public interface AsyncStockUpdateListener {
        void onStockUpdatedAsync(String[] values);
    }

    public class StockBalanceUpdater extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String[] prodCodeArray;
            final int indexOfStockBalance = 13;
            final int indexOfProdCode = 0;
            switch (inputType) {
                case Utils.STOCK_BAL_PRODUCT:
                    prodCodeArray = new String[productList.size()];
                    int counter = 0;
                    for (Product product : productList) {
                        prodCodeArray[counter] = product.getProductCode();
                    }
                    break;

                case Utils.STOCK_BAL_RVITEM:
                    prodCodeArray = new String[rv1ItemList.size()];
                    counter = 0;
                    for (Rv1Item item : rv1ItemList) {
                        prodCodeArray[counter] = item.getStrId();
                        // Log.d(TAG, "doInBackground: " + item.getStrId());
                        counter++;
                    }
                    break;
                default:
                    prodCodeArray = new String[0];
                    Log.d(TAG, "doInBackground: default prodCodeArray");

            }
            try {
                Log.d(TAG, "doInBackground: prodcodearray " + prodCodeArray.toString());
                JSONObject stockBalanceJson = new JSONObject(stockBalanceReport);
                JSONObject stockJson = stockBalanceJson.getJSONObject("message");
                JSONArray result = stockJson.getJSONArray("result");
                int positionOfProd = 0;
                outerLoop:
                for (String prodCode : prodCodeArray) {
                    int rowCount = result.length() - 2;
                    if (isCancelled()) break;
                    for (int i = 0; i <= rowCount; i++) {
                        JSONArray itemRow = result.getJSONArray(i);
                        Log.d(TAG, "doInBackground: itemRow " + itemRow.toString());
                        String stockProdCode = itemRow.getString(indexOfProdCode);
                        Log.d(TAG, "doInBackground: stockProdCode " + i + stockProdCode);
                        Log.d(TAG, "doInBackground: prodCode " + i + prodCode);
                        if (isCancelled()) break outerLoop;
                        if (prodCode.equals(stockProdCode)) {
                            double prodStockBalance = itemRow.getDouble(indexOfStockBalance);
                            String[] values = new String[2];
                            values[0] = String.valueOf(positionOfProd);
                            values[1] = String.valueOf(prodStockBalance);
                            Log.d(TAG, "doInBackground: prodCode equals stockProdCode : " + positionOfProd + ": " + ": " + prodStockBalance + ": " + prodCode);
                            if (isCancelled()) break outerLoop;
                            publishProgress(values);
                            break;
                        }
                    }
                    positionOfProd++;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            Log.d(TAG, "onProgressUpdate: values" + values[0] + ";" + values[1]);
            listener.onStockUpdatedAsync(values);
            super.onProgressUpdate(values);
        }
    }

}