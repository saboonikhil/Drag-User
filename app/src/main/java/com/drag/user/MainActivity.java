package com.drag.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drag.user.model.Location;
import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.drag.user.util.ObjectSerializer;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    private SharedPreferences pref;
    private String token;
    private User user;
    private TextView titleView;
    private ImageButton notificationsView;
    private ImageButton bookRideView;
    private LinearLayout tripsView, accountView;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        token = pref.getString("token", "");
        String json = pref.getString("dbObj", "");
        user = new Gson().fromJson(json, User.class);

        notificationsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationsFragment notifications = new NotificationsFragment();
                notifications.show(getSupportFragmentManager(), "Notifications");
            }
        });

        bookRideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySelectedScreen(R.id.main_book_ride);
            }
        });

        tripsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySelectedScreen(R.id.main_trips);
            }
        });

        accountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySelectedScreen(R.id.main_account);
            }
        });

        getAuthLocations();
        displaySelectedScreen(R.id.main_book_ride);
    }

    private void getAuthLocations() {
        EndPointInterface service = APIUtils.getAPIService(MainActivity.this);
        service.authLocation(user.getEmail(), token).enqueue(new Callback<Location[]>() {
            @Override
            public void onResponse(@NonNull Call<Location[]> call, @NonNull Response<Location[]> response) {
                if (response.code() == 401) {
                    pref.edit().remove("token").apply();
                    pref.edit().remove("expires").apply();
                    pref.edit().remove("dbObj").apply();
                    Toast.makeText(getApplicationContext(),
                            "Your account is blocked. Please contact help desk for recovery.", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                } else if (response.body() != null) {
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("locations", ObjectSerializer.serialize(response.body()));
                    edit.apply();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Location[]> call, @NonNull Throwable t) {
                Log.e(TAG + " On Failure", t.getMessage());
            }
        });
    }

    public void displaySelectedScreen(int itemId) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (itemId) {
            case R.id.main_book_ride:
                count = 0;
                titleView.setText(R.string.app_name);
                ft.replace(R.id.main_content_frame, new BookRideFragment(), "Book Ride").commit();
                break;

            case R.id.main_trips:
                titleView.setText(R.string.trips);
                ft.replace(R.id.main_content_frame, new TripsFragment(), "Trips").commit();
                break;

            case R.id.main_account:
                titleView.setText(R.string.account);
                ft.replace(R.id.main_content_frame, new AccountFragment(), "Account").commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        BookRideFragment currentFragment = (BookRideFragment) getSupportFragmentManager().findFragmentByTag("Book Ride");
        if (currentFragment != null && currentFragment.isVisible()) {
            count = count + 1;
            if (count == 1)
                Toast.makeText(MainActivity.this, "Tap again to exit Drag", Toast.LENGTH_SHORT).show();
            else if (count == 2)
                finish();
        } else {
            displaySelectedScreen(R.id.main_book_ride);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initViews() {
        titleView = findViewById(R.id.main_title);
        notificationsView = findViewById(R.id.main_notifications);
        bookRideView = findViewById(R.id.main_book_ride);
        tripsView = findViewById(R.id.main_trips);
        accountView = findViewById(R.id.main_account);
    }
}