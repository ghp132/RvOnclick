package com.example.RvOnclick;


import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.RvOnclick.BluetoothPrinter.DeviceList;
import com.example.RvOnclick.BluetoothPrinter.Print;
import com.example.RvOnclick.BluetoothPrinter.PrinterCommands;
import com.example.RvOnclick.BluetoothPrinter.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import static com.example.RvOnclick.CustomerTransactionProductFragment.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerTransactionOrderFragment extends Fragment
        implements OrderProductAdapter.OnItemClickListener,
        ApplicationController.OnInvoiceStatusUpdatedListener,
        ApplicationController.OnInvoicePostedListener {

    public static StDatabase stDatabase;
    public Long orderId;
    public double orderTotalValue;
    public List<OrderProduct> productList = new ArrayList<>();
    protected String custCode;
    RecyclerView recyclerView;
    private OrderProductAdapter.OnItemClickListener listener;
    private ApplicationController.OnInvoicePostedListener invoicePostedListener;
    private ApplicationController.OnInvoiceStatusUpdatedListener statusUpdatedListener;
    ApplicationController.PostAndUpdateInvoices postInvoices;
    OrderProductAdapter productAdapter;
    TextView tvOrderInfo;
    private int orderStatus;
    ApplicationController ac = new ApplicationController();
    boolean shouldBeRefreshed, invoiceUpdated;
    Button btBtpPrint, btPay;
    private Print print;
    private static OutputStream outputStream;
    //BluetoothSocket btsocket = ApplicationController.btsocket;
    boolean dbCreated = false;
    ConstraintLayout pbPrintProgress;


    public CustomerTransactionOrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_transaction_order, container, false);

        custCode = getArguments().getString("custCode");
        Log.d("TransactionActivity", "onCreateView: ");
        tvOrderInfo = view.findViewById(R.id.tv_customerOrderInfo);
        btBtpPrint = view.findViewById(R.id.bt_btpPrint);
        btPay = view.findViewById(R.id.bt_pay);
        pbPrintProgress = view.findViewById(R.id.pb_printProgress);

        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();
        dbCreated = true;
        orderTotalValue = stDatabase.stDao().getOrderTotalValueByOrderId(orderId);
        tvOrderInfo.setText(String.valueOf(orderTotalValue));


        try {
            try {

                orderId = Long.parseLong(getActivity().getIntent().getStringExtra("orderId"));
                //Toast.makeText(getActivity().getApplicationContext(),orderId.toString(),Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException ee) {
                orderId = Long.parseLong(String.valueOf(-1));
            }
        } catch (NullPointerException e) {

            int temp = -1;
            //Toast.makeText(getActivity().getApplicationContext(),orderId.toString(),Toast.LENGTH_SHORT).show();
            orderId = Long.parseLong(String.valueOf(-1));
        }


        if (orderId != -1 && orderId != null) {
            orderTotalValue = stDatabase.stDao().getOrderTotalValueByOrderId(orderId);
            productList = stDatabase.stDao().getOrderProductsById(orderId);
            orderStatus = stDatabase.stDao().getOrderStatusByOrderId(orderId);
            recyclerView = (RecyclerView) view.findViewById(R.id.OrderProduct_recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            listener = this;
            invoicePostedListener = this;
            statusUpdatedListener = this;
            productAdapter = new OrderProductAdapter(listener, productList, getActivity());
            postInvoices = ac.new PostAndUpdateInvoices(invoicePostedListener, statusUpdatedListener);
            recyclerView.setAdapter(productAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }

        if (shouldBeRefreshed) {
            refreshFragment();
            shouldBeRefreshed = false;
        }


        btPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderStatus == -1) {
                    pbPrintProgress.setVisibility(View.VISIBLE);
                    List<Order> pOrderList = new ArrayList<>();
                    pOrderList.add(stDatabase.stDao().getOrderByOrderId(orderId));
                    postInvoices.postOrders(getActivity(), pOrderList, stDatabase, 1);


                }

            }
        });

        if (orderId == -1) {
            btBtpPrint.setVisibility(View.GONE);
            btPay.setVisibility(View.GONE);
        } else {
            btBtpPrint.setVisibility(View.VISIBLE);
            btPay.setVisibility(View.VISIBLE);
            btBtpPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pbPrintProgress.setVisibility(View.VISIBLE);
                    List<Order> pOrderList = new ArrayList<>();
                    pOrderList.add(stDatabase.stDao().getOrderByOrderId(orderId));
                    if (orderStatus == -1) {
                        postInvoices.postOrders(getActivity(), pOrderList, stDatabase, 2);
                    } else if (orderStatus == 0) {
                        postInvoices.updateUnknownOrderStatuses(getActivity(), pOrderList, stDatabase, 3);
                    } else {
                        pbPrintProgress.setVisibility(View.GONE);
                        printBill();
                    }


                }
            });
        }

        return view;


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != DeviceList.REQUEST_CONNECT_BT) {
            productList.clear();
            productList.addAll(stDatabase.stDao().getOrderProductsById(orderId));
            productAdapter.notifyDataSetChanged();
        /*productList = stDatabase.stDao().getOrderProductsById(orderId);
        OrderProductAdapter productAdapter = new OrderProductAdapter(listener, productList, getActivity());
        recyclerView.setAdapter(productAdapter);*/
            orderTotalValue = stDatabase.stDao().getOrderTotalValueByOrderId(orderId);
            tvOrderInfo.setText(String.valueOf(orderTotalValue));
        } else {
            try {
                ApplicationController.btsocket = DeviceList.getSocket();
                /*if(btsocket != null){
                    printText(message.getText().toString());
                }*/

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    @Override
    public void onItemClicked(View v, int position) {

        if (orderStatus == -1) {
            String prodCode = productList.get(position).getProductCode();
            OrderProduct orderProduct = productList.get(position);
            double freeQty = 0;
            long parentId = orderProduct.getParentId();
            long childId = orderProduct.getChildId();
            if (childId != 0) {
                for (OrderProduct prod : productList) {
                    if (prod.getOrderProductId() == orderProduct.getChildId()) {
                        freeQty = prod.getQty();
                    }
                }
            }
            Double qty = productList.get(position).getQty();
            Double rate = productList.get(position).getRate();

            if (parentId != 0) {
                for (OrderProduct prod : productList) {
                    if (prod.getOrderProductId() == orderProduct.getParentId()) {
                        qty = prod.getQty();
                        freeQty = orderProduct.getQty();
                    }
                }
            }

            // data passed to product rate info dialog fragment
            Bundle args = new Bundle();
            args.putString("prodCode", prodCode);
            args.putString("custCode", custCode);
            args.putString("orderId", String.valueOf(orderId));
            args.putDouble("qty", qty);
            args.putDouble("freeQty", freeQty);
            args.putDouble("rate", rate);


            DialogFragment_ProductRateInfo df = new DialogFragment_ProductRateInfo();
            df.setArguments(args);
            df.setCancelable(false);
            df.setTargetFragment(this, 2);

            df.show(getFragmentManager(), "Dialog");
            setUserVisibleHint(false);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "This order cannot be editted.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onLongClick(View view, final int position) {
        // Toast.makeText(getActivity(),"clicked at position" + position,Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getActivity());
        }
        builder.setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        /*if (stDatabase.stDao().countOrderProduct(orderId) > 1) {
                            stDatabase.stDao().deleteOrderProduct(productList.get(position));
                            updateProductAdapterfromDB();
                        } else if (stDatabase.stDao().countOrderProduct(orderId) == 1) {
                            stDatabase.stDao().deleteOrderProduct(productList.get(position));
                            stDatabase.stDao().deleteOrderByOrderId(orderId);
                            orderId = Long.valueOf(-1);
                            getActivity().getIntent().putExtra("orderId", orderId);
                            productList.clear();
                            productAdapter.notifyDataSetChanged();
                            getActivity().finish();
                        }*/

                        ac.deleteOrderProduct(productList.get(position).getOrderProductId(), stDatabase);
                        int countOfOrderProduct = stDatabase.stDao().countOrderProduct(orderId);
                        updateProductAdapterfromDB();
                        if (countOfOrderProduct == 0) {
                            stDatabase.stDao().deleteOrderByOrderId(orderId);
                            orderId = Long.valueOf(-1);
                            getActivity().getIntent().putExtra("orderId", orderId);
                            productList.clear();
                            productAdapter.notifyDataSetChanged();
                            getActivity().finish();
                        }

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (dbCreated) {
                refreshFragment();
            }
            shouldBeRefreshed = true;
        }
    }

    private void updateProductAdapterfromDB() {
        productList.clear();
        productList.addAll(stDatabase.stDao().getOrderProductsById(orderId));
        productAdapter.notifyDataSetChanged();
    }


    private void refreshFragment() {
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        orderTotalValue = stDatabase.stDao().getOrderTotalValueByOrderId(orderId);
        tvOrderInfo.setText(String.valueOf(Math.round(orderTotalValue)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       /* try {
            if(btsocket!= null){
                outputStream.close();
                btsocket.close();
                btsocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    protected void printBill() {
        if (ApplicationController.btsocket == null) {
            Intent BTIntent = new Intent(getActivity(), DeviceList.class);
            this.startActivityForResult(BTIntent, DeviceList.REQUEST_CONNECT_BT);
        } else {
            Order order = stDatabase.stDao().getOrderByOrderId(orderId);
            List<Long> orderIdList = ac.splitProductListByCompany(order, stDatabase);
            for (Long pOid : orderIdList) {
                Order pOrder = stDatabase.stDao().getOrderByOrderId(pOid);
                String companyName = pOrder.getCompanyName();
                Company company = stDatabase.stDao().getCompanyByCompanyName(companyName);
                String addressLine1 = company.getAddress_line1();
                String addressLine2 = company.getAddress_line2();
                String city = company.getCity();
                String phone = company.getPhone();
                String companyGstin = company.getGstin();
                String orderNumber = "NA";
                if (invoiceUpdated) {
                    if (pOrder.getOrderNumber() != null) {
                        orderNumber = pOrder.getOrderNumber();
                    }
                }


                OutputStream opstream = null;
                try {
                    opstream = ApplicationController.btsocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputStream = opstream;

                //print command
                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outputStream = ApplicationController.btsocket.getOutputStream();
                    byte[] printformat = new byte[]{0x1B, 0x21, 0x03};
                    outputStream.write(printformat);


                    printCustom(companyName, 2, 1);
                    printCustom(addressLine1, 0, 1);
                    if (addressLine2 != null || !addressLine2.equals("null")) {
                        printCustom(addressLine2 + ", " + city, 0, 1);
                    }
                    printCustom("Ph.: " + phone, 0, 1);
                    String dateTime[] = getDateTime();
                    printText("Date: " + dateTime[0] +
                            textRepeater(" ", getSpaceCount("Date: " +
                                    dateTime[0] + "Time: " + dateTime[1])) + "Time: " + dateTime[1]);
                    printText(lineFormatter("Invoice No.: " + orderNumber));
                    Log.d(TAG, "printBill: orderNumber" + lineFormatter("Invoice No.: " + orderNumber));
                    printText(textRepeater("-", Utils.LINE_LENGTH));

                    printText(lineFormatter("Customer:"));
                    Customer customer = stDatabase.stDao().getCustomerbyCustomerCode(custCode);
                    String customerName = customer.getCustomer_name();
                    printCustom(customerName + textRepeater(" ", getSpaceCount(customerName)),
                            1, 0);
                    printText(textRepeater("-", Utils.LINE_LENGTH));
                    List<OrderProduct> pOrderProdList = stDatabase.stDao().getOrderProductsById(pOid);
                    int counter = 1;
                    double grandTotal = 0.00;
                    for (OrderProduct pOrderProd : pOrderProdList) {
                        printText(lineFormatter(pOrderProd.getProductCode()));
                        printText("Rate" + textRepeater(" ", 6) + "Qty" +
                                textRepeater(" ", 7) + "Amt" + textRepeater(" ", 9));
                        double rate = pOrderProd.getRate();
                        double qty = pOrderProd.getQty();
                        double amt = Math.round(rate * 100 * qty) / 100.00;
                        grandTotal = grandTotal + amt;
                        String strRate = String.valueOf(rate);
                        String strQty = String.valueOf(qty);
                        String strAmt = String.valueOf(amt);
                        int rateLen = strRate.length();
                        int qtyLen = strQty.length();
                        int amtLen = strAmt.length();

                        printText(strRate + textRepeater(" ", 10 - rateLen) + strQty +
                                textRepeater(" ", 10 - qtyLen) +
                                strAmt + textRepeater(" ", 12 - amtLen));
                        printText(textRepeater(".", Utils.LINE_LENGTH));
                    }
                    grandTotal = Math.round(grandTotal * 100) / 100;
                    printText("Total" + textRepeater(" ",
                            getSpaceCount("Total" + grandTotal)) + grandTotal);
                    printText(textRepeater(".", Utils.LINE_LENGTH));


                    printCustom(String.valueOf(Math.round(grandTotal)), 2, 1);
                    printText(textRepeater(" ", Utils.LINE_LENGTH));
                    printNewLine();
                    printNewLine();

                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void printCustom(String msg, int size, int align) {
        //Print config "mode"
        byte[] cc = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
        byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text
        try {
            switch (size) {
                case 0:
                    outputStream.write(cc);
                    break;
                case 1:
                    outputStream.write(bb);
                    break;
                case 2:
                    outputStream.write(bb2);
                    break;
                case 3:
                    outputStream.write(bb3);
                    break;
            }

            switch (align) {
                case 0:
                    //left align
                    outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }
            outputStream.write(msg.getBytes());
            outputStream.write(PrinterCommands.LF);
            //outputStream.write(cc);
            //printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void printUnicode() {
        try {
            outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
            printText(Utils.UNICODE_TEXT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //print new line
    private void printNewLine() {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void resetPrint() {
        try {
            outputStream.write(PrinterCommands.ESC_FONT_COLOR_DEFAULT);
            outputStream.write(PrinterCommands.FS_FONT_ALIGN);
            outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
            outputStream.write(PrinterCommands.ESC_CANCEL_BOLD);
            outputStream.write(PrinterCommands.LF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printText(String msg) {
        try {
            // Print normal text
            outputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printText(byte[] msg) {
        try {
            // Print normal text
            outputStream.write(msg);
            printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String leftRightAlign(String str1, String str2) {
        String ans = str1 + str2;
        if (ans.length() < 31) {
            int n = (31 - str1.length() + str2.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return ans;
    }

    private String[] getDateTime() {
        final Calendar c = Calendar.getInstance();
        String dateTime[] = new String[2];
        dateTime[0] = c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR);
        dateTime[1] = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
        return dateTime;
    }

    private String lineFormatter(String textToPrint) {
        int lineLen = Utils.LINE_LENGTH;
        int spaceCount = 0;
        int textLen = textToPrint.length();
        int extraCharacCount = textLen % lineLen;
        spaceCount = lineLen - extraCharacCount;
        if (extraCharacCount > 0) {
            textToPrint = textToPrint + textRepeater(" ", spaceCount);
        }
        return textToPrint;
    }

    private String textRepeater(String character, int count) {
        String textToPrint = "";
        for (int i = 1; i <= count; i++) {
            textToPrint = textToPrint + character;
        }
        return textToPrint;
    }

    private int getSpaceCount(String text) {
        int extraSpace = 0;
        int lineLen = Utils.LINE_LENGTH;
        int textCount = text.length();
        extraSpace = lineLen - textCount;
        return extraSpace;

    }


    @Override
    public void onInvoiceStatusUpdated(boolean success, int resultCode, int requestCode, List<Order> orders) {
        invoiceUpdated = success;
        orderStatus = stDatabase.stDao().getOrderStatusByOrderId(orderId);

        if (requestCode == 2) {
            pbPrintProgress.setVisibility(View.GONE);

            printBill();
        }
        if (requestCode == 1) {
            //request from pay button through postInvoices method
            double paymentAmt = 0.0;
            if (!success) {
                pbPrintProgress.setVisibility(View.GONE);
                paymentAmt = stDatabase.stDao().getOrderTotalValueByOrderId(orderId);
                makeOfflinePayment(Math.round(paymentAmt));
            }
        }


    }

    @Override
    public void onInvoicePosted(boolean posted, long postedOrderId, int resultCode, int noOfInvoices, int invoiceCount, int requestCode, Invoice postedInvoice) {
        if (requestCode == 2) {
            //request from printBill button
            invoiceUpdated = posted;

            if (noOfInvoices == invoiceCount) {
                pbPrintProgress.setVisibility(View.GONE);
            }
            if (resultCode != 2) {
                //volley error response
                printBill();
            }
            if (resultCode == 0) {
                orderStatus = stDatabase.stDao().getOrderStatusByOrderId(orderId);
            }
        }

        if (requestCode == 1) {
            //request from pay button
            double paymentAmt = 0.00;
            List<Invoice> createdInvoices = new ArrayList<>();
            if (resultCode == 2) {
                //volley error response
            }
            if (resultCode == 0) {
                //success
                if (postedOrderId == orderId) {
                    orderStatus = 1;
                }
                if (noOfInvoices == invoiceCount) {
                    pbPrintProgress.setVisibility(View.GONE);
                    showNewPaymentFragment();
                }
                //TODO:handle pay on partial success of postorder incase of single order split
                // into multiple and some orders not posted

            }
        }

    }

    private void showNewPaymentFragment() {
        Intent m2aIntent = new Intent(getActivity(), Main2Activity.class);
        m2aIntent.putExtra("custCode", custCode);
        m2aIntent.putExtra("fragment", com.example.RvOnclick.Utils.CUSTOMER_OUTSTANDING_LIST_FRAGMENT);
        getActivity().startActivity(m2aIntent);
    }

    private void makeOfflinePayment(final double paidAmt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Amount");
        final EditText etPaymentAmt = new EditText(getActivity());
        List<OrderProduct> orderProducts = stDatabase.stDao().getOrderProductsById(orderId);
        boolean containsMultipleCompanies = false;
        HashSet<String> companySet = new HashSet<>();
        for (OrderProduct orderProduct : orderProducts) {
            companySet.add(orderProduct.getCompanyName());
        }
        etPaymentAmt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPaymentAmt.setText(String.valueOf(paidAmt));
        if (companySet.size() > 1) {
            disableEditText(etPaymentAmt);
        }
        builder.setView(etPaymentAmt);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                double paidAmt1 = Double.valueOf(etPaymentAmt.getText().toString());
                Order order = stDatabase.stDao().getOrderByOrderId(orderId);
                order.setPaidAmt(paidAmt1);
                stDatabase.stDao().updateOrder(order);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundColor(Color.TRANSPARENT);
    }



}