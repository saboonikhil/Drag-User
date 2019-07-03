package com.drag.user;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drag.user.model.Cab;
import com.drag.user.model.Trip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TripDetailsFragment extends DialogFragment {

    private Activity parentActivity;
    private Trip[] trips;
    private int position;
    private View rootView;
    private ImageButton backView;
    private LinearLayout driverInfoView;
    private Button callView;
    private TextView startTimeView, driverNameView, driverContactView, carNameView, pickupView, dropView,
            idView, seatsView, carNumberView, fareView, statusView;
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
            trips = (Trip[]) getArguments().getSerializable("trip_details");
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

        Cab trip = trips[position].getCab();
        try {
            Calendar calendar = Calendar.getInstance();
            String startTime = trip.getStartTime();
            Date displayTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(startTime);
            calendar.setTime(displayTime);
            calendar.add(Calendar.HOUR, 5);
            calendar.add(Calendar.MINUTE, 30);
            startTimeView.setText(new SimpleDateFormat("EEE, MMM d, hh:mm a").format(calendar.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String driverName = trip.getDriverName();
        driverContact = trip.getDriverContact();
        if (driverName != null && driverContact != null && driverName.length() >= 1 && driverContact.length() >= 1) {
            driverNameView.setText(driverName);
            driverContactView.setText(driverContact);
        } else {
            driverInfoView.setVisibility(View.GONE);
        }

        callView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAction();
            }
        });

        carNameView.setText(trip.getCarName());
        String carNumber = trip.getCarNumber();
        if (carNumber == null || carNumber.length() < 1)
            carNumberView.setVisibility(View.GONE);
        else
            carNumberView.setText(carNumber);

        pickupView.setText(trip.getPickup());
        dropView.setText(trip.getDrop());
        statusView.setText(trips[position].getStatus());

        if (trips[position].getStatus().equals("Completed")) {
            idView.setText(trip.getTripId());
            seatsView.setText(trip.getSeats());
            String displayFare = "₹ " + trip.getFare();
            fareView.setText(displayFare);
        } else if (trips[position].getStatus().equals("Sharing")) {
            idView.setText(trips[position].getTravelDetails().getTripId());
            seatsView.setText(trips[position].getTravelDetails().getSeats());
            String displayFare = "₹ " + trips[position].getTravelDetails().getFare();
            fareView.setText(displayFare);
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
    }
}