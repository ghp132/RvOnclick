package com.example.RvOnclick;

public class Utils {
    public static boolean UNSYNCED_PAYMENTS_PRESENT = false;
    public static final String CUSTOMER_OUTSTANDING_LIST_FRAGMENT = "CustomerOutstandingListFragment";
    public static final String ERROR_DISPLAY_FRAGMENT = "ErrorDisplayFragment";
    public static final String VOLLEY_ERROR_LIST_FRAGMENT = "VolleyErrorList";

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


}
