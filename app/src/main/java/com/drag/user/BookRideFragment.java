package com.drag.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.drag.user.model.Cab;
import com.drag.user.model.Location;
import com.drag.user.util.ObjectSerializer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class BookRideFragment extends Fragment {

    private Activity parentActivity;
    private View rootView;
    private String[] cities;
    private Calendar DateCalendar, TimeCalendar;
    private InputMethodManager imm;
    private ImageButton swapLocationView;
    private AutoCompleteTextView pickupView, dropView;
    private EditText dateView, timeView;
    private Button selectView;
    private boolean route = true;
    private long thirtyDays = 2592000000L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentActivity = getActivity();
        rootView = inflater.inflate(R.layout.fragment_book_ride, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parentActivity.setTitle("Drag");
        initViews();

        SharedPreferences pref = parentActivity.getSharedPreferences("AppPref", MODE_PRIVATE);
        Location[] locations = (Location[]) ObjectSerializer.deserialize(pref.getString("locations",
                ObjectSerializer.serialize(new Location[10])));
        cities = new String[locations.length];
        for (int i = 0; i < locations.length; i++)
            cities[i] = locations[i].getCity();

        imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

        GradientDrawable pickupGrad = (GradientDrawable) pickupView.getBackground();
        pickupGrad.setStroke(2, getResources().getColor(R.color.white_two));
        GradientDrawable dropGrad = (GradientDrawable) dropView.getBackground();
        dropGrad.setStroke(2, getResources().getColor(R.color.white_two));

        setupCitySpinner();

        pickupView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                GradientDrawable pickupGrad = (GradientDrawable) pickupView.getBackground();
                pickupGrad.setStroke(1, getResources().getColor(R.color.white_two));
            }
        });

        dropView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                GradientDrawable dropGrad = (GradientDrawable) dropView.getBackground();
                dropGrad.setStroke(1, getResources().getColor(R.color.white_two));
            }
        });

        swapLocationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((TextUtils.isEmpty(pickupView.getText().toString())) && (TextUtils.isEmpty(dropView.getText().toString()))) {
                    GradientDrawable myGrad = (GradientDrawable) pickupView.getBackground();
                    myGrad.setStroke(2, Color.RED);
                } else {
                    route = !route;
                    Editable location = pickupView.getText();
                    pickupView.setText(dropView.getText());
                    dropView.setText(location);
                }
            }
        });

        setupDateTimePicker();
        DateCalendar.setTimeInMillis(System.currentTimeMillis() + 86400000);
        dateView.setText(new SimpleDateFormat("EEE, MMM d", Locale.US).format(DateCalendar.getTime()));
        TimeCalendar.setTimeInMillis(System.currentTimeMillis() + 86400000);
        timeView.setText(new SimpleDateFormat("hh:mm a", Locale.US).format(TimeCalendar.getTime()));

        selectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCab();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCitySpinner() {
        ArrayAdapter<String> citySpinnerAdapter = new ArrayAdapter<>(parentActivity,
                R.layout.support_simple_spinner_dropdown_item, cities);
        pickupView.setAdapter(citySpinnerAdapter);
        pickupView.setKeyListener(null);
        pickupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ((AutoCompleteTextView) view).showDropDown();
                return false;
            }
        });
        dropView.setAdapter(citySpinnerAdapter);
        dropView.setKeyListener(null);
        dropView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ((AutoCompleteTextView) view).showDropDown();
                return false;
            }
        });
    }

    private void setupDateTimePicker() {
        DateCalendar = Calendar.getInstance();
        DateCalendar.add(Calendar.DATE, 1);
        TimeCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                DateCalendar.set(Calendar.YEAR, year);
                DateCalendar.set(Calendar.MONTH, monthOfYear);
                DateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateView.setText(new SimpleDateFormat("EEE, MMM d", Locale.US).format(DateCalendar.getTime()));
            }
        };

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(dateView.getWindowToken(), 0);
                DatePickerDialog datePickerDialog = new DatePickerDialog(parentActivity, date,
                        DateCalendar.get(Calendar.YEAR), DateCalendar.get(Calendar.MONTH), DateCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() + 86400000);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() + thirtyDays);
                datePickerDialog.show();
            }
        });

        final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                TimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                TimeCalendar.set(Calendar.MINUTE, minute);
                timeView.setText(new SimpleDateFormat("hh:mm a", Locale.US).format(TimeCalendar.getTime()));
            }
        };

        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(timeView.getWindowToken(), 0);
                TimePickerDialog mTimePicker = new TimePickerDialog(parentActivity, time,
                        TimeCalendar.get(Calendar.HOUR_OF_DAY), TimeCalendar.get(Calendar.MINUTE), false);
                mTimePicker.show();
            }
        });
    }

    private void selectCab() {
        String pickup = pickupView.getText().toString();
        String drop = dropView.getText().toString();
        Date calendarDate = DateCalendar.getTime();
        Date calendarTime = TimeCalendar.getTime();
        String startTime = formatDateTime(calendarDate, calendarTime);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(pickup)) {
            focusView = pickupView;
            cancel = true;
        } else if (TextUtils.isEmpty(drop)) {
            focusView = dropView;
            cancel = true;
        }

        if (cancel) {
            GradientDrawable myGrad = (GradientDrawable) focusView.getBackground();
            myGrad.setStroke(2, Color.RED);
        } else {
            if (isConnectedToInternet()) {
                Cab travelDetails = new Cab(pickup, drop, startTime);
                SelectCabTypeFragment selectCab = new SelectCabTypeFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("travel_details", travelDetails);
                selectCab.setArguments(bundle);
                selectCab.show(getChildFragmentManager(), "Select Cab Type");
            } else {
                Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String formatDateTime(Date date, Date time) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String isoDate = df.format(date);
        DateFormat df1 = new SimpleDateFormat("HH:mm:ss.SSS");
        String isoTime = df1.format(time);

        String startDateTime = isoDate + 'T' + isoTime + 'Z';
        try {
            Calendar calendar = Calendar.getInstance();
            Date startTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(startDateTime);
            calendar.setTime(startTime);
            calendar.add(Calendar.HOUR, -5);
            calendar.add(Calendar.MINUTE, -30);
            startDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return startDateTime;
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connMgr = (ConnectivityManager) parentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnected();
    }

    private void initViews() {
        pickupView = rootView.findViewById(R.id.book_ride_pickup);
        dropView = rootView.findViewById(R.id.book_ride_drop);
        swapLocationView = rootView.findViewById(R.id.book_ride_swap_location);
        dateView = rootView.findViewById(R.id.book_ride_date);
        timeView = rootView.findViewById(R.id.book_ride_time);
        selectView = rootView.findViewById(R.id.book_ride_select);
    }
}