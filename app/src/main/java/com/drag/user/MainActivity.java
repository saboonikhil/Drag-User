package com.drag.user;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

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

        displaySelectedScreen(R.id.main_book_ride);
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