package com.drag.user;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.drag.user.model.Cab;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TripDetailsFragment extends DialogFragment {

    private Activity parentActivity;
    private Cab[] trips;
    private int position;
    private View rootView;
    private ImageButton backView;
    private Calendar startTime;
    private LinearLayout cabInfoView, driverInfoView;
    private Button callView;
    private TextView startTimeView, driverNameView, driverContactView, carNameView, pickupView, dropView,
            idView, seatsView, carNumberView, fareView, statusView, amountPaidView, amountPendingView;
    private String driverContact;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentActivity = getActivity();

        if (getArguments() != null) {
            trips = (Cab[]) getArguments().getSerializable("trip_details");
            position = getArguments().getInt("position");
        }

        rootView = inflater.inflate(R.layout.fragment_trip_details, container, false);
        return rootView;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();

        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        try {
            startTime = Calendar.getInstance();
            Date displayTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(trips[position].getStartTime());
            startTime.setTime(displayTime);
            startTime.add(Calendar.HOUR, 5);
            startTime.add(Calendar.MINUTE, 30);
            startTimeView.setText(new SimpleDateFormat("EEE, MMM d, hh:mm a").format(startTime.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if ((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()) > -18000000 &&
                (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()) < 86400000) {

            String driverName = trips[position].getDriverName();
            driverContact = trips[position].getDriverContact();
            if (driverName != null && driverContact != null && driverName.length() >= 1 && driverContact.length() >= 1) {
                driverInfoView.setVisibility(View.VISIBLE);
                driverNameView.setText(driverName);
                driverContactView.setText(driverContact);
            }

            callView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callAction();
                }
            });

            String carName = trips[position].getCarName();
            String carNumber = trips[position].getCarNumber();
            if (carName != null && carNumber != null && carName.length() >= 1 && carNumber.length() >= 1) {
                cabInfoView.setVisibility(View.VISIBLE);
                carNameView.setText(carName);
                carNumberView.setText(carNumber);
            }
        } else {
            driverInfoView.setVisibility(View.GONE);
            cabInfoView.setVisibility(View.GONE);
        }

        String displayFare = "₹" + trips[position].getFare();
        fareView.setText(displayFare);

        float fare = Float.parseFloat(trips[position].getFare());
        float amountPaid = Float.parseFloat(trips[position].getRiders()[0].getFare());

        if (!trips[position].isShared()) {
            idView.setText(trips[position].getRiders()[0].getTripId());
            statusView.setText(trips[position].getRiders()[0].getTripStatus());
            pickupView.setText(trips[position].getRiders()[0].getPickup());
            dropView.setText(trips[position].getRiders()[0].getDrop());
            seatsView.setText(trips[position].getRiders()[0].getSeats());

            String displayAmountPaid = "- ₹" + trips[position].getRiders()[0].getFare();
            amountPaidView.setText(displayAmountPaid);
            String displayAmountPending = "₹" + String.format(java.util.Locale.US, "%.2f", (fare - amountPaid));
            amountPendingView.setText(displayAmountPending);
        }
    }

    private void callAction() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + driverContact));

        if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Log.v("TAG", "Calling permission is revoked");
            ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.CALL_PHONE}, 1);
        } else {
            Log.v("TAG", "Calling permission is granted");
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(parentActivity, "Permission granted", Toast.LENGTH_SHORT).show();
                callAction();
            } else {
                Toast.makeText(parentActivity, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        backView = rootView.findViewById(R.id.trip_details_back);
        startTimeView = rootView.findViewById(R.id.trip_details_start_time);
        cabInfoView = rootView.findViewById(R.id.trip_details_cab_info);
        driverInfoView = rootView.findViewById(R.id.trip_details_driver_info);
        driverNameView = rootView.findViewById(R.id.trip_details_driver_name);
        driverContactView = rootView.findViewById(R.id.trip_details_driver_contact);
        callView = rootView.findViewById(R.id.trip_details_call);
        carNameView = rootView.findViewById(R.id.trip_details_car_name);
        carNumberView = rootView.findViewById(R.id.trip_details_car_number);
        pickupView = rootView.findViewById(R.id.trip_details_pickup);
        dropView = rootView.findViewById(R.id.trip_details_drop);
        statusView = rootView.findViewById(R.id.trip_details_status);
        idView = rootView.findViewById(R.id.trip_details_id);
        seatsView = rootView.findViewById(R.id.trip_details_seats);
        fareView = rootView.findViewById(R.id.trip_details_fare);
        amountPaidView = rootView.findViewById(R.id.trip_details_amount_paid);
        amountPendingView = rootView.findViewById(R.id.trip_details_amount_pending);
    }
}