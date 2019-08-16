package com.drag.user;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drag.user.adapter.SelectCabTypeAdapter;
import com.drag.user.model.Cab;
import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class SelectCabTypeFragment extends BottomSheetDialogFragment implements SelectCabTypeAdapter.ListItemClickListener {

    private String TAG = SelectCabTypeFragment.class.getSimpleName();
    private Activity parentActivity;
    private View bottomSheetInternal, rootView;
    private String token;
    private User user;
    private Cab travelDetails;
    private ProgressBar progressBar;
    private TextView rulesView;
    private SelectCabTypeAdapter selectCabTypeAdapter;
    private RecyclerView recyclerView;
    private RelativeLayout emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentActivity = getActivity();

        if (getArguments() != null) {
            travelDetails = (Cab) getArguments().getSerializable("travel_details");
        }

        rootView = inflater.inflate(R.layout.fragment_select_cab_type, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();

        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                bottomSheetInternal = d.findViewById(android.support.design.R.id.design_bottom_sheet);
                if (bottomSheetInternal != null) {
                    BottomSheetBehavior.from(bottomSheetInternal).setSkipCollapsed(true);
                    BottomSheetBehavior.from(bottomSheetInternal).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                        @Override
                        public void onStateChanged(@NonNull View bottomSheet, int newState) {
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

        /*refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullAndRefresh();
            }
        });*/
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isConnectedToInternet()) {
            getAvailableCabList();
            if (selectCabTypeAdapter == null)
                progressBar.setVisibility(View.VISIBLE);
        } else
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
    }

    /*private void pullAndRefresh() {
        refreshCount++;
        if (refreshCount != 1)
            refreshLayout.setRefreshing(true);
        if (isConnectedToInternet())
            getAvailableCabList();
        else
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
    }*/

    private void getAvailableCabList() {
        EndPointInterface service = APIUtils.getAPIService(parentActivity);
        Call<Cab[]> call = service.cabFareList(
                user.getEmail(), token, travelDetails.getPickup(), travelDetails.getDrop(), travelDetails.getStartTime());

        call.enqueue(new Callback<Cab[]>() {
            @Override
            public void onResponse(@NonNull Call<Cab[]> call, @NonNull Response<Cab[]> response) {
                if (response.body() != null) {
                    progressBar.setVisibility(View.GONE);
                    generateDataList(response.body(), travelDetails);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Cab[]> call, @NonNull Throwable t) {
                Log.e(TAG + " On Failure", t.getMessage());
                progressBar.setVisibility(View.GONE);
                /*if (refreshCount == 1) {
                    shimmerView.stopShimmerAnimation();
                    shimmerView.setVisibility(View.GONE);
                    pullAndRefresh();
                } else {
                    refreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Couldn't refresh cabs", Toast.LENGTH_LONG).show();
                }*/
            }
        });
    }

    private void generateDataList(Cab[] cabFareList, Cab travelDetails) {
        selectCabTypeAdapter = new SelectCabTypeAdapter(getContext(), cabFareList, travelDetails, this);
        recyclerView.setAdapter(selectCabTypeAdapter);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        if (layoutManager.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            emptyView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            String sedanDistanceLimit = "", suvDistanceLimit = "";

            for (Cab cab : cabFareList) {
                if (cab.getType().equals("Sedan"))
                    sedanDistanceLimit = cab.getDriverName();
                else if (cab.getType().equals("SUV"))
                    suvDistanceLimit = cab.getDriverName();
            }

            String selectCabTypeRules = "✪  One way trip of about " + cabFareList[0].getCarName() + " km includes toll charges if any.\n\n" +
                    "✪  Distance exceeding " + cabFareList[0].getCarName() + " km is chargeable at ₹" +
                    sedanDistanceLimit + "/km for Sedan and ₹" + suvDistanceLimit + "/km for SUV.\n\n" +
                    "✪  Fare for extra distance to be paid to the driver in cash.\n";

            rulesView.setText(selectCabTypeRules);
            rulesView.setBackgroundColor(getResources().getColor(R.color.bg_select_cab_pressed));
        }
    }

    @Override
    public void onListItemClick(Cab selectedCabType, String pickup, String drop, String startTime) {
        BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
        ConfirmRideFragment confirmRide = new ConfirmRideFragment();
        confirmRide.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dismiss();
            }
        });

        Bundle bundle = new Bundle();
        bundle.putSerializable("selected_cab_type", selectedCabType);
        bundle.putString("pickup", pickup);
        bundle.putString("drop", drop);
        bundle.putString("startTime", startTime);
        confirmRide.setArguments(bundle);
        confirmRide.show(getChildFragmentManager(), "Confirm Ride");
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
        /*collapsedBarView = rootView.findViewById(R.id.select_cab_collapsed_action_bar);
        expandedBarView = rootView.findViewById(R.id.select_cab_expanded_action_bar);
        collapsedCloseView = rootView.findViewById(R.id.select_cab_collapsed_close);
        expandedCloseView = rootView.findViewById(R.id.select_cab_expanded_close);
        pickupView = rootView.findViewById(R.id.select_cab_pickup);
        dropView = rootView.findViewById(R.id.select_cab_drop);
        shimmerView = rootView.findViewById(R.id.select_cab_shimmer_layout);
        refreshLayout = rootView.findViewById(R.id.select_cab_refresh_layout);*/
        progressBar = rootView.findViewById(R.id.select_cab_type_progress_bar);
        rulesView = rootView.findViewById(R.id.select_cab_type_rules);
        recyclerView = rootView.findViewById(R.id.select_cab_recycler_view);
        emptyView = rootView.findViewById(R.id.select_cab_empty_layout);
    }
}