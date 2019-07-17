package com.drag.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drag.user.adapter.SelectRideAdapter;
import com.drag.user.model.Cab;
import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.drag.user.network.SelectRideResponse;
import com.drag.user.network.SelectRideUtils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class SelectRideFragment extends BottomSheetDialogFragment implements SelectRideAdapter.ListItemClickListener {

    private String TAG = SelectRideFragment.class.getSimpleName();
    private Activity parentActivity;
    private View bottomSheetInternal, rootView;
    private ViewGroup container;
    private LinearLayout collapsedBarView, expandedBarView;
    private SelectRideAdapter selectRideAdapter;
    private String token;
    private User user;
    private Cab travelDetails, rideDetails;
    private ImageButton collapsedCloseView, expandedCloseView;
    private TextView pickupView, dropView;
    private ShimmerFrameLayout shimmerView;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private RelativeLayout emptyView;
    private Button joinRideView;
    private int refreshCount = 0;
    private ProgressDialog pd;
    private AlertDialog dialog;
    private FloatingActionButton requestRideView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentActivity = getActivity();

        if (getArguments() != null) {
            travelDetails = (Cab) getArguments().getSerializable("travel_details");
        }

        rootView = inflater.inflate(R.layout.fragment_select_ride, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();

        container = (ViewGroup) rootView.getParent();
        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                bottomSheetInternal = d.findViewById(android.support.design.R.id.design_bottom_sheet);
                if (bottomSheetInternal != null) {
                    BottomSheetBehavior.from(bottomSheetInternal).setSkipCollapsed(true);
                    BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    BottomSheetBehavior.from(bottomSheetInternal).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                        @Override
                        public void onStateChanged(@NonNull View bottomSheet, int newState) {
                            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                                collapsedBarView.setVisibility(View.GONE);
                                expandedBarView.setVisibility(View.VISIBLE);
                            } else {
                                collapsedBarView.setVisibility(View.VISIBLE);
                                expandedBarView.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        }
                    });
                }
            }
        });

        SharedPreferences pref = parentActivity.getSharedPreferences("AppPref", MODE_PRIVATE);
        token = pref.getString("token", "");
        String json = pref.getString("dbObj", "");
        user = new Gson().fromJson(json, User.class);

        pickupView.setText(travelDetails.getPickup());
        dropView.setText(travelDetails.getDrop());

        collapsedCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        expandedCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullAndRefresh();
            }
        });

        requestRideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRide();
            }
        });
    }

    @Override
    public void onResume() {
        shimmerView.startShimmerAnimation();
        super.onResume();
        pullAndRefresh();
    }

    private void pullAndRefresh() {
        requestRideView.show();
        joinRideView.setVisibility(View.GONE);
        refreshCount++;
        if (refreshCount != 1)
            refreshLayout.setRefreshing(true);
        if (isConnectedToInternet())
            getAvailableRideList();
        else
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
    }

    private void getAvailableRideList() {
        EndPointInterface service = APIUtils.getAPIService(parentActivity);
        Call<List<Cab>> call = service.userRideList(
                user.getEmail(), token, travelDetails.getCity(), travelDetails.getPickup(),
                travelDetails.getDrop(), travelDetails.getSeats(), travelDetails.getStartTime());

        call.enqueue(new Callback<List<Cab>>() {
            @Override
            public void onResponse(@NonNull Call<List<Cab>> call, @NonNull Response<List<Cab>> response) {
                if (response.body() != null) {
                    if (refreshCount == 1) {
                        shimmerView.stopShimmerAnimation();
                        shimmerView.setVisibility(View.GONE);
                        generateDataList(response.body());
                    } else {
                        selectRideAdapter.refreshData(response.body());
                        if (selectRideAdapter.getItemCount() == 0)
                            emptyView.setVisibility(View.VISIBLE);
                        else
                            emptyView.setVisibility(View.INVISIBLE);
                        refreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Cab>> call, @NonNull Throwable t) {
                Log.e(TAG + " On Failure", t.getMessage());
                if (refreshCount == 1) {
                    shimmerView.stopShimmerAnimation();
                    shimmerView.setVisibility(View.GONE);
                    pullAndRefresh();
                } else {
                    refreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Couldn't refresh rides", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void generateDataList(List<Cab> rideList) {
        selectRideAdapter = new SelectRideAdapter(getContext(), rideList, this);
        recyclerView.setAdapter(selectRideAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        if (layoutManager.getItemCount() == 0) {
            BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            emptyView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onListItemClick(Cab selectedRide) {
        rideDetails = selectedRide;
        requestRideView.hide();
        BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
        View customView = getLayoutInflater().inflate(R.layout.layout_select_ride_prompt, container, false);
        dialog = new AlertDialog.Builder(getContext())
                .setView(customView)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface di) {
                dialog.getButton(di.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        joinRideView.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
                dialog.getButton(di.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pullAndRefresh();
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
        joinRideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinRideView.setVisibility(View.GONE);
                joinRide();
            }
        });
    }

    private void joinRide() {
        pd = new ProgressDialog(getContext());
        pd.setMessage("Joining...");
        pd.show();
        EndPointInterface service = APIUtils.getAPIService(parentActivity);
        service.userJoinRide(user.get_id(), user.getEmail(), token, rideDetails.get_id(),
                travelDetails.getSeats()).enqueue(new Callback<Cab>() {
            @Override
            public void onResponse(@NonNull Call<Cab> call, @NonNull Response<Cab> response) {
                pd.cancel();
                if (response.isSuccessful()) {
                    ((MainActivity) parentActivity).displaySelectedScreen(R.id.main_trips);
                    showSuccess();
                    dismiss();
                } else {
                    SelectRideResponse error = SelectRideUtils.parseError(response, parentActivity);
                    Toast.makeText(getContext(), error.message(), Toast.LENGTH_LONG).show();
                    pullAndRefresh();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Cab> call, @NonNull Throwable t) {
                pd.cancel();
                Log.e(TAG + " On Failure", t.getMessage());
                pullAndRefresh();
            }
        });
    }

    private void showSuccess() {
        View customView = getLayoutInflater().inflate(R.layout.layout_join_success, container, false);
        final Button shareView = customView.findViewById(R.id.join_success_share);

        AlertDialog dialog = new AlertDialog.Builder(parentActivity)
                .setView(customView)
                .create();

        dialog.show();

        shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareViaWhatsapp();
            }
        });
    }

    private void requestRide() {
        View customView = getLayoutInflater().inflate(R.layout.layout_select_request, container, false);
        TextView cityView = customView.findViewById(R.id.select_request_city);
        cityView.setText(travelDetails.getCity());
        TextView pickupView = customView.findViewById(R.id.select_request_pickup);
        pickupView.setText(travelDetails.getPickup());
        TextView dropView = customView.findViewById(R.id.select_request_drop);
        dropView.setText(travelDetails.getDrop());
        TextView timeView = customView.findViewById(R.id.select_request_time);
        timeView.setText(getDisplayTime(travelDetails.getStartTime()));
        TextView seatsView = customView.findViewById(R.id.select_request_seats);
        seatsView.setText(travelDetails.getSeats());
        TextView nameView = customView.findViewById(R.id.select_request_name);
        nameView.setText(user.getName());
        TextView contactView = customView.findViewById(R.id.select_request_mobile_number);
        contactView.setText(user.getContact());

        dialog = new AlertDialog.Builder(parentActivity)
                .setView(customView)
                .setPositiveButton("Submit", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface di) {
                dialog.getButton(di.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendRequest();
                        dialog.dismiss();
                    }
                });
                dialog.getButton(di.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pullAndRefresh();
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    private void sendRequest() {
        pd = new ProgressDialog(getContext());
        pd.setMessage("Sending...");
        pd.show();
        EndPointInterface service = APIUtils.getAPIService(parentActivity);
        Call<User> call = service.requestRide(user.getEmail(), token, travelDetails.getCity(),
                travelDetails.getPickup(), travelDetails.getDrop(), travelDetails.getStartTime(), travelDetails.getSeats());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.body() != null) {
                    pd.cancel();
                    dismiss();
                    Toast.makeText(parentActivity, "Submitted successfully", Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(parentActivity, "Your request will be processed within 6 hours", Toast.LENGTH_LONG).show();
                        }
                    }, 2000);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                pd.cancel();
                Log.e(TAG + " On Failure", t.getMessage());
                Toast.makeText(getContext(), "Please check your internet connection or try again later.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private String getDisplayTime(String startTime) {
        String displayTime = null;
        try {
            Calendar calendar = Calendar.getInstance();
            Date time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(startTime);
            calendar.setTime(time);
            calendar.add(Calendar.HOUR, 5);
            calendar.add(Calendar.MINUTE, 30);
            displayTime = new SimpleDateFormat("MMM d, hh:mm a").format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return displayTime;
    }

    private void shareViaWhatsapp() {
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Probably the last time I'll be buzzing around my flight timings.  " +
                "*" + getDisplayTime(rideDetails.getStartTime()) + "*" + " - Join me in my journey from " + rideDetails.getPickup() + " to " +
                rideDetails.getDrop() + " on play.google.com/store/apps/details?id=com.drag.user \uD83D\uDC4B");
        try {
            parentActivity.startActivity(whatsappIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(parentActivity, "Whatsapp is not installed.", Toast.LENGTH_SHORT).show();
        }
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
        collapsedBarView = rootView.findViewById(R.id.select_ride_collapsed_action_bar);
        expandedBarView = rootView.findViewById(R.id.select_ride_expanded_action_bar);
        collapsedCloseView = rootView.findViewById(R.id.select_ride_collapsed_close);
        expandedCloseView = rootView.findViewById(R.id.select_ride_expanded_close);
        pickupView = rootView.findViewById(R.id.select_ride_pickup);
        dropView = rootView.findViewById(R.id.select_ride_drop);
        shimmerView = rootView.findViewById(R.id.select_ride_shimmer_layout);
        refreshLayout = rootView.findViewById(R.id.select_ride_refresh_layout);
        recyclerView = rootView.findViewById(R.id.select_ride_recycler_view);
        emptyView = rootView.findViewById(R.id.select_ride_empty_layout);
        requestRideView = rootView.findViewById(R.id.select_ride_request);
        joinRideView = rootView.findViewById(R.id.select_ride_join);
    }
}