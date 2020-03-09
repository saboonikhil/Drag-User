package com.drag.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

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
    private TextView titleView, tripsTitleView, accountTitleView;
    private ImageButton notificationsView, bookRideView, tripsButtonView, accountButtonView;
    private LinearLayout tripsView, accountView;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        String view = getIntent().getStringExtra("view");
        displaySelectedScreen(view);

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
                displaySelectedScreen("Book Ride");
                tripsButtonView.setColorFilter(getResources().getColor(R.color.warm_grey));
                tripsTitleView.setTextColor(getResources().getColor(R.color.warm_grey));
                accountButtonView.setColorFilter(getResources().getColor(R.color.warm_grey));
                accountTitleView.setTextColor(getResources().getColor(R.color.warm_grey));
            }
        });

        tripsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySelectedScreen("Trips");
                tripsButtonView.setColorFilter(getResources().getColor(R.color.lightish_blue));
                tripsTitleView.setTextColor(getResources().getColor(R.color.lightish_blue));
                accountButtonView.setColorFilter(getResources().getColor(R.color.warm_grey));
                accountTitleView.setTextColor(getResources().getColor(R.color.warm_grey));
            }
        });

        accountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySelectedScreen("Account");
                accountButtonView.setColorFilter(getResources().getColor(R.color.lightish_blue));
                accountTitleView.setTextColor(getResources().getColor(R.color.lightish_blue));
                tripsButtonView.setColorFilter(getResources().getColor(R.color.warm_grey));
                tripsTitleView.setTextColor(getResources().getColor(R.color.warm_grey));
            }
        });

        getAuthLocations();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getExtras() != null) {
            String value = intent.getExtras().getString("view");
            displaySelectedScreen(value != null ? value : "Book Ride");
        }
    }

    private void getAuthLocations() {
        EndPointInterface service = APIUtils.getAPIService(MainActivity.this);
        service.authLocation(user.getEmail(), token, com.drag.user.BuildConfig.VERSION_CODE).enqueue(new Callback<Location[]>() {
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
                } else if (response.code() == 403) {
                    pref.edit().remove("token").apply();
                    pref.edit().remove("expires").apply();
                    pref.edit().remove("dbObj").apply();
                    Toast.makeText(getApplicationContext(),
                            "Please update the app with the latest version from the play store.", Toast.LENGTH_LONG).show();
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

    public void displaySelectedScreen(String view) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (view) {
            case "Book Ride":
                count = 0;
                titleView.setText(R.string.app_name);
                ft.replace(R.id.main_content_frame, new BookRideFragment(), "Book Ride").commit();
                break;

            case "Trips":
                titleView.setText(R.string.trips);
                ft.replace(R.id.main_content_frame, new TripsFragment(), "Trips").commit();
                break;

            case "Account":
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
            displaySelectedScreen("Book Ride");
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
        tripsButtonView = findViewById(R.id.main_trips_button);
        tripsTitleView = findViewById(R.id.main_trips_title);
        accountButtonView = findViewById(R.id.main_account_button);
        accountTitleView = findViewById(R.id.main_account_title);
        accountView = findViewById(R.id.main_account);
    }
}