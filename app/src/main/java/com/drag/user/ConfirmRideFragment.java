package com.drag.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.drag.user.model.Cab;
import com.drag.user.model.User;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class ConfirmRideFragment extends DialogFragment {

    private Activity parentActivity;
    private View rootView;
    private Cab selectedCab;
    private String pickup, drop, startTime;
    private ImageButton backView;
    private TextView pickupView, dropView, startTimeView, typeView, seatsView, nameView, contactView,
            fullAmountView, advanceAmountView;
    private CardView advancePaymentView, fullPaymentView;
    private String paymentMode = "Full";
    private Button confirmRideView;
    private DialogInterface.OnDismissListener onDismissListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentActivity = getActivity();

        if (getArguments() != null) {
            selectedCab = (Cab) getArguments().getSerializable("selected_cab_type");
            pickup = getArguments().getString("pickup");
            drop = getArguments().getString("drop");
            startTime = getArguments().getString("startTime");
        }

        rootView = inflater.inflate(R.layout.fragment_confirm_ride, container, false);
        return rootView;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews();

        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        SharedPreferences pref = parentActivity.getSharedPreferences("AppPref", MODE_PRIVATE);
        String json = pref.getString("dbObj", "");
        User user = new Gson().fromJson(json, User.class);

        try {
            Calendar calendar = Calendar.getInstance();
            Date displayTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(startTime);
            calendar.setTime(displayTime);
            calendar.add(Calendar.HOUR, 5);
            calendar.add(Calendar.MINUTE, 30);
            startTimeView.setText(new SimpleDateFormat("EEE, MMM d, hh:mm a").format(calendar.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        pickupView.setText(pickup);
        dropView.setText(drop);
        typeView.setText(selectedCab.getType());

        if (selectedCab.getType().equals("Sedan")) {
            seatsView.setText("4");
        } else if (selectedCab.getType().equals("SUV")) {
            seatsView.setText("6");
        }

        nameView.setText(user.getName());
        contactView.setText(user.getContact());

        float fare = Float.parseFloat(selectedCab.getCarNumber());
        String displayAdvanceAmt = "₹" + String.format(java.util.Locale.US, "%.2f", (0.2 * fare));
        advanceAmountView.setText(displayAdvanceAmt);
        String displayFullAmt = "₹" + String.format(java.util.Locale.US, "%.2f", fare);
        fullAmountView.setText(displayFullAmt);

        fullPaymentView.setBackgroundColor(ContextCompat.getColor(parentActivity, R.color.bg_select_cab_pressed));
        String paymentAmount = "PROCEED TO PAY " + fullAmountView.getText();
        confirmRideView.setText(paymentAmount);

        /*String sedanDistanceLimit = "", suvDistanceLimit = "";

         *//*for (Cab cab : cabFareList) {
            if (cab.getType().equals("Sedan"))
                sedanDistanceLimit = cab.getDriverName();
            else if (cab.getType().equals("SUV"))
                suvDistanceLimit = cab.getDriverName();
        }

        "✪  Distance exceeding " + cabFareList[0].getCarName() + " km is chargeable at ₹" +
                sedanDistanceLimit + "/km for Sedan and ₹" + suvDistanceLimit + "/km for SUV.\n\n" +*/

        advancePaymentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                advancePaymentView.setBackgroundColor(ContextCompat.getColor(parentActivity, R.color.bg_select_cab_pressed));
                fullPaymentView.setBackgroundColor(ContextCompat.getColor(parentActivity, R.color.white));
                String paymentAmount = "PROCEED TO PAY " + advanceAmountView.getText();
                confirmRideView.setText(paymentAmount);
                paymentMode = "Advance";
            }
        });

        fullPaymentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullPaymentView.setBackgroundColor(ContextCompat.getColor(parentActivity, R.color.bg_select_cab_pressed));
                advancePaymentView.setBackgroundColor(ContextCompat.getColor(parentActivity, R.color.white));
                String paymentAmount = "PROCEED TO PAY " + fullAmountView.getText();
                confirmRideView.setText(paymentAmount);
                paymentMode = "Full";
            }
        });

        confirmRideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(parentActivity, PaymentActivity.class);
                intent.putExtra("confirmed_ride_details", selectedCab);
                intent.putExtra("pickup", pickup);
                intent.putExtra("drop", drop);
                intent.putExtra("startTime", startTime);
                intent.putExtra("payment_mode", paymentMode);
                parentActivity.startActivity(intent);
                dismiss();
            }
        });
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    private void initViews() {
        backView = rootView.findViewById(R.id.confirm_ride_back);
        pickupView = rootView.findViewById(R.id.confirm_ride_pickup);
        dropView = rootView.findViewById(R.id.confirm_ride_drop);
        startTimeView = rootView.findViewById(R.id.confirm_ride_start_time);
        typeView = rootView.findViewById(R.id.confirm_ride_type);
        seatsView = rootView.findViewById(R.id.confirm_ride_seats);
        nameView = rootView.findViewById(R.id.confirm_ride_name);
        contactView = rootView.findViewById(R.id.confirm_ride_mobile_number);
        advancePaymentView = rootView.findViewById(R.id.confirm_ride_advance);
        advanceAmountView = rootView.findViewById(R.id.confirm_ride_advance_amount);
        fullPaymentView = rootView.findViewById(R.id.confirm_ride_full);
        fullAmountView = rootView.findViewById(R.id.confirm_ride_full_amount);
        confirmRideView = rootView.findViewById(R.id.confirm_ride_button);
    }
}