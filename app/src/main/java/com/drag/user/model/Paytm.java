package com.drag.user.model;

import java.io.Serializable;

public class Paytm implements Serializable {

    private String MID;
    private String ORDER_ID;
    private String CUST_ID;
    private String MOBILE_NO;
    private String EMAIL;
    private String CHANNEL_ID;
    private String TXN_AMOUNT;
    private String WEBSITE;
    private String CALLBACK_URL;
    private String INDUSTRY_TYPE_ID;
    private String CHECKSUMHASH;

    public Paytm(String mid, String order_id, String cust_id, String mobile_no, String email, String channel_id,
                 String txn_amount, String website, String callback_url, String industry_type_id, String checksumhash) {
        MID = mid;
        ORDER_ID = order_id;
        CUST_ID = cust_id;
        MOBILE_NO = mobile_no;
        EMAIL = email;
        CHANNEL_ID = channel_id;
        TXN_AMOUNT = txn_amount;
        WEBSITE = website;
        CALLBACK_URL = callback_url;
        INDUSTRY_TYPE_ID = industry_type_id;
        CHECKSUMHASH = checksumhash;
    }

    public String getMID() {
        return MID;
    }

    public String getORDER_ID() {
        return ORDER_ID;
    }

    public String getCUST_ID() {
        return CUST_ID;
    }

    public String getCHANNEL_ID() {
        return CHANNEL_ID;
    }

    public String getTXN_AMOUNT() {
        return TXN_AMOUNT;
    }

    public String getWEBSITE() {
        return WEBSITE;
    }

    public String getCALLBACK_URL() {
        return CALLBACK_URL;
    }

    public String getINDUSTRY_TYPE_ID() {
        return INDUSTRY_TYPE_ID;
    }

    public String getCHECKSUMHASH() {
        return CHECKSUMHASH;
    }

    public String getMOBILE_NO() {
        return MOBILE_NO;
    }

    public String getEMAIL() {
        return EMAIL;
    }
}