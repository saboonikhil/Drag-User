package com.drag.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.drag.user.model.Cab;
import com.drag.user.model.Paytm;
import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.google.gson.Gson;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity implements PaytmPaymentTransactionCallback {

    private String TAG = PaymentActivity.class.getSimpleName();
    private String token, pickup, drop, startTime, orderId, paymentMode;
    private User user;
    private Cab cabTypeSelected;
    private TextView messageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initViews();

        SharedPreferences pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        token = pref.getString("token", "");
        String json = pref.getString("dbObj", "");
        user = new Gson().fromJson(json, User.class);

        Intent intent = getIntent();
        cabTypeSelected = (Cab) intent.getSerializableExtra("confirmed_ride_details");
        pickup = intent.getStringExtra("pickup");
        drop = intent.getStringExtra("drop");
        startTime = intent.getStringExtra("startTime");
        paymentMode = intent.getStringExtra("payment_mode");

        generateCheckSum();
    }

    private void generateCheckSum() {
        EndPointInterface service = APIUtils.getAPIService(PaymentActivity.this);
        service.generateChecksum(user.get_id(), user.getEmail(), token, cabTypeSelected.get_id(), paymentMode).enqueue(new Callback<Paytm>() {
            @Override
            public void onResponse(@NonNull Call<Paytm> call, @NonNull Response<Paytm> response) {
                if (response.body() != null) {
                    initializePaytmPayment(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Paytm> call, @NonNull Throwable t) {
                Log.e(TAG + " On Failure", t.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Please check your internet connection or try again later.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void initializePaytmPayment(Paytm paytm) {
        //PaytmPGService Service = PaytmPGService.getStagingService();
        PaytmPGService Service = PaytmPGService.getProductionService();

        orderId = paytm.getORDER_ID();
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("MID", paytm.getMID());
        paramMap.put("ORDER_ID", paytm.getORDER_ID());
        paramMap.put("CUST_ID", paytm.getCUST_ID());
        paramMap.put("MOBILE_NO", paytm.getMOBILE_NO());
        paramMap.put("EMAIL", paytm.getEMAIL());
        paramMap.put("CHANNEL_ID", paytm.getCHANNEL_ID());
        paramMap.put("TXN_AMOUNT", paytm.getTXN_AMOUNT());
        paramMap.put("WEBSITE", paytm.getWEBSITE());
        paramMap.put("CALLBACK_URL", paytm.getCALLBACK_URL());
        paramMap.put("CHECKSUMHASH", paytm.getCHECKSUMHASH());
        paramMap.put("INDUSTRY_TYPE_ID", paytm.getINDUSTRY_TYPE_ID());

        PaytmOrder order = new PaytmOrder(paramMap);
        Service.initialize(order, null);
        Service.startPaymentTransaction(this, true, true, this);
        messageView.setVisibility(View.GONE);
    }

    @Override
    public void onTransactionResponse(Bundle bundle) {
        verifyTransactionStatus();
    }

    @Override
    public void networkNotAvailable() {
        Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void clientAuthenticationFailed(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void someUIErrorOccurred(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onBackPressedCancelTransaction() {
        finish();
    }

    @Override
    public void onTransactionCancel(String s, Bundle bundle) {
        Toast.makeText(this, s + bundle.toString(), Toast.LENGTH_LONG).show();
        finish();
    }

    private void verifyTransactionStatus() {
        EndPointInterface service = APIUtils.getAPIService(PaymentActivity.this);
        service.createTrip(user.get_id(), user.getEmail(), token, cabTypeSelected.get_id(), pickup, drop,
                startTime, orderId).enqueue(new Callback<Cab>() {
            @Override
            public void onResponse(@NonNull Call<Cab> call, @NonNull Response<Cab> response) {
                if (response.body() != null) {
                    displayTransactionStatus(response.body().getRiders()[0].getTripStatus());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Cab> call, @NonNull Throwable t) {
                Log.e(TAG + " On Failure", t.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Low internet connectivity! Contact help desk with booking details.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void displayTransactionStatus(String status) {
        switch (status) {
            case "Payment Successful":
            case "Trip Confirmed":
                setContentView(R.layout.layout_payment_success);
                ImageButton confirmBackView = findViewById(R.id.payment_success_back);
                confirmBackView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                break;
            case "Payment Failed":
                setContentView(R.layout.layout_payment_failed);
                ImageButton failedBackView = findViewById(R.id.payment_failed_back);
                failedBackView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                break;
            default:
                setContentView(R.layout.layout_payment_pending);
                ImageButton pendingBackView = findViewById(R.id.payment_pending_back);
                pendingBackView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (messageView.getVisibility() != View.GONE) {
            finish();
        } else
            super.onBackPressed();
    }

    private void initViews() {
        messageView = findViewById(R.id.payment_message);
    }
}