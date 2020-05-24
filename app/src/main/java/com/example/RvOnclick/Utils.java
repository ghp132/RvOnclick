package com.example.RvOnclick;

public class Utils {
    public static boolean UNSYNCED_PAYMENTS_PRESENT = false;

    //DOCTYPES
    public static final String SALES_INVOICE = "Sales Invoice";
    public static final String DELIVERY_NOTE = "Delivery Note";
    public static final String PAYMENT_ENTRY = "Payment Entry";
    public static final String STOCK_ENTRY = "Stock Entry";

    //KEYS FOR KEY-VALUE PAIRS
    public static final String KEY_FROM_WAREHOUSE = "fromWarehouse";
    public static final String KEY_TO_WAREHOUSE = "toWarehouse";
    public static final String KEY_WAREHOUSE = "warehouse";
    public static final String KEY_TRANSACTION_TYPE = "transactionType";
    public static final String KEY_PRODUCT_LIST_ID = "productListId";
    public static final String KEY_PREVIOUS_QTY = "previousQty";
    public static final String KEY_LIST_ITEM_ID = "listItemId";
    public static final String KEY_DOC_ID = "docId";

    //ASYNC STOCK UPDATER INPUT TYPE CONSTANTS
    public static final int STOCK_BAL_RVITEM = 1;
    public static final int STOCK_BAL_PRODUCT = 2;

    //REQUEST CODES
    public static final int REQ_STOCK_TRANSFER_FROM_LISTITEMFRAGMENT = 1001;
    public static final int REQ_LOGIN_FROM_LISTITEMFRAGMENT = 1002;
    public static final int REQ_POST_INVOICE_FROM_LOGIN_ACTIVITY = 1003;

    //VOLLEY ERROR DESCRIPTION
    public static final String VOLLEY_ERROR_AUTH_FAILURE = "com.android.volley.AuthFailureError";


    //STOCK TRANSACTIONS
    public static final String STOCK_MATERIAL_TRANSFER = "Material Transfer";


    //FRAGMENTS
    public static final String CUSTOMER_OUTSTANDING_LIST_FRAGMENT = "CustomerOutstandingListFragment";
    public static final String ERROR_DISPLAY_FRAGMENT = "ErrorDisplayFragment";
    public static final String VOLLEY_ERROR_LIST_FRAGMENT = "VolleyErrorList";
    public static final String SETTINGS_FRAGMENT = "SettingsFragment";
    public static final String WAREHOUSE_SELECTION_FRAGMENT = "WarehouseSelectionFragment";
    public static final String ADD_PRODUCT_DIALOG_FRAGMENT = "AddProductDialogFragment";
    public static final String PRODUCT_LIST_FRAGMENT = "ProductListFragment";
    public static final String LIST_ITEM_FRAGMENT = "ListItemFragment";
    public static final String STOCK_TRANSFER_WAREHOUSE_FRAGMENT = "StockTransferWarehouseFragment";


    //VOLLEY RESULT CODES
    public static final int VOLLEY_SUCCESS = 1;
    //JSON PARSING ERROR
    public static final int ON_RESPONSE_JSON_ERROR = 2;
    public static final int VOLLEY_ERROR = 3;
    public static final int VOLLEY_ERROR_RESPONSE_BODY = 4;
    public static final int POST_JSON_CREATION_ERROR = 5;
    public static final int VOLLEY_ERROR_BODY_PARSING_ERROR = 6;
    public static final int ARG_LIST_SIZE_0 = 7;
    public static final int NETWORKRESPONSE_IS_NULL = 8;


    //GENERAL STATUS SUCCESS FAILURE CONSTANTS
    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;
    public static final int UNKNOWN = -1;

    //DOC STATUSES
    public static final int DOC_STATUS_UNATTEMPTED = -1;
    public static final int DOC_STATUS_UNKNOWN = 0;
    public static final int DOC_STATUS_DRAFT = 1;
    public static final int DOC_STATUS_SUBMITTED = 2;

    //PAGE LIMIT LENGTH
    public static final int PAYMENT_PAGE_LIMIT_LENGTH = 500;
    public static final int SALES_INVOICE_PAGE_LIMIT_LENGTH = 1000;
    public static final int TERRITORY_PAGE_LIMIT_LENGTH = 50;
    public static final int BRAND_PAGE_LIMIT_LENGTH = 50;
    public static final int WAREHOUSE_PAGE_LIMIT_LENGTH = 50;

    // MISC
    public static final boolean isForEgg = true;
    public static final String PREFS = "prefs";
    public static final String MAIN_SETTINGS = "main_settings";


}
