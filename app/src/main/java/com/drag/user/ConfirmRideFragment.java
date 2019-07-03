package com.drag.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.drag.user.model.Cab;
import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class ConfirmRideFragment extends DialogFragment {

    private String TAG = "ConfirmRideFragment";
    private Activity parentActivity;
    private View rootView;
    private String token, carName, pickup, drop, startTime, seats, fare;
    private User user;
    private ImageButton backView;
    private TextView collegeNameView, pickupView, dropView, startTimeView, carNameView, seatsView, fareView, nameView, contactView;
    private Button confirmRideView;
    private Cab selectedCab;
    private ProgressDialog pd;
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
            selectedCab = (Cab) getArguments().getSerializable("selected_cab");
            pickup = getArguments().getString("pickup");
            drop = getArguments().getString("drop");
            startTime = getArguments().getString("startTime");
            carName = selectedCab.getCarName();
            seats = selectedCab.getSeats();
            fare = selectedCab.getFare();
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
        token = pref.getString("token", "");
        String json = pref.getString("dbObj", "");
        user = new Gson().fromJson(json, User.class);

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

        collegeNameView.setText(selectedCab.getCollegeName());
        pickupView.setText(pickup);
        dropView.setText(drop);
        carNameView.setText(carName);
        seatsView.setText(seats);
        nameView.setText(user.getName());
        contactView.setText(user.getContact());

        String displayFare = "₹ " + fare;
        fareView.setText(displayFare);

        String displayPayment = "PROCEED TO PAY " + "₹" + fare;
        confirmRideView.setText(displayPayment);

        confirmRideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd = new ProgressDialog(getContext());
                pd.setMessage("Checking cab availability...");
                pd.show();

                EndPointInterface service = APIUtils.getAPIService(parentActivity);
                service.cabCheckAvailable(selectedCab.get_id(), user.getEmail(), token).enqueue(new Callback<Cab>() {
                    @Override
                    public void onResponse(@NonNull Call<Cab> call, @NonNull Response<Cab> response) {
                        if (response.errorBody() != null) {
                            pd.cancel();
                            dismiss();
                            Toast.makeText(getContext(), "Sorry, cab unavailable now! Please select another cab.", Toast.LENGTH_LONG).show();
                        } else if (response.body() != null) {
                            if (response.body().isAvailable()) {
                                Intent intent = new Intent(parentActivity, PaymentActivity.class);
                                intent.putExtra("confirmed_ride_details", selectedCab);
                                intent.putExtra("pickup", pickup);
                                intent.putExtra("drop", drop);
                                intent.putExtra("startTime", startTime);
                                intent.putExtra("seats", seats);
                                intent.putExtra("fare", fare);
                                parentActivity.startActivity(intent);
                                dismiss();
                                pd.cancel();
                            } else {
                                pd.cancel();
                                dismiss();
                                Toast.makeText(getContext(), "Sorry, cab unavailable now! Please select another cab.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Cab> call, @NonNull Throwable t) {
                        pd.cancel();
                        Log.e(TAG + " On Failure", t.getMessage());
                        Snackbar.make(rootView, "Please check your data connection or try again later.", Snackbar.LENGTH_LONG).show();
                    }
                });
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
        collegeNameView = rootView.findViewById(R.id.confirm_ride_college_name);
        pickupView = rootView.findViewById(R.id.confirm_ride_pickup);
        dropView = rootView.findViewById(R.id.confirm_ride_drop);
        startTimeView = rootView.findViewById(R.id.confirm_ride_start_time);
        carNameView = rootView.findViewById(R.id.confirm_ride_car_name);
        seatsView = rootView.findViewById(R.id.confirm_ride_seats);
        fareView = rootView.findViewById(R.id.confirm_ride_fare);
        nameView = rootView.findViewById(R.id.confirm_ride_name);
        contactView = rootView.findViewById(R.id.confirm_ride_mobile_number);
        confirmRideView = rootView.findViewById(R.id.confirm_ride_button);
    }
}