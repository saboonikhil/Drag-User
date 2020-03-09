package com.drag.user;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.drag.user.model.Cab;
import com.drag.user.model.Paytm;
import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.google.gson.Gson;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity implements PaytmPaymentTransactionCallback {

    private String TAG = PaymentActivity.class.getSimpleName();
    private String token, pickup, drop, startTime, orderId, paymentMode;
    private User user;
    private Cab cabTypeSelected;
    private TextView messageView;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initViews();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
        PaytmPGService Service;
        if (BuildConfig.DEBUG) Service = PaytmPGService.getStagingService();
        else Service = PaytmPGService.getProductionService();

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
                    displayTransactionStatus(response.body());
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

    private void displayTransactionStatus(Cab cab) {
        switch (cab.getRiders()[0].getTripStatus()) {
            case "Payment Successful":
            case "Trip Confirmed":
                sendNotification(cab);
                setContentView(R.layout.layout_payment_success);
                ImageButton confirmBackView = findViewById(R.id.payment_success_back);
                confirmBackView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                        intent.putExtra("view", "Trips");
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
                        Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                        intent.putExtra("view", "Trips");
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
                        Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                        intent.putExtra("view", "Trips");
                        finish();
                    }
                });
                break;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void sendNotification(Cab cab) {
        String startDate = "", startTime = "";
        try {
            Calendar calendar = Calendar.getInstance();
            if (cab.getStartTime() != null) {
                Date displayTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(cab.getStartTime());
                calendar.setTime(displayTime);
                calendar.add(Calendar.HOUR, 5);
                calendar.add(Calendar.MINUTE, 30);
                startTime = new SimpleDateFormat("h:mm a").format(calendar.getTime());
                startDate = new SimpleDateFormat("MMM d").format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String message = "Trip ID " + cab.getRiders()[0].getTripId() + " confirmed from " + cab.getPickup()
                + " to " + cab.getDrop() + " starting at " + startTime + " on " + startDate +
                ".\n\nDriver and cab details will be shared before 2-8 hours from the pickup time. Cherish the Journey!";

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("view", "Trips");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default")
                .setContentTitle("Your ride is booked!")
                .setTicker("Your ride is booked!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setContentText(message);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Your ride is booked!", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(message);
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channel);
        }
        int id = new Random().nextInt(9999 - 1000) + 1000;
        notificationManager.notify(id, notificationBuilder.build());
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