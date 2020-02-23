package com.drag.user;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drag.user.adapter.SelectCabAdapter;
import com.drag.user.model.Cab;
import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class SelectCabFragment extends BottomSheetDialogFragment implements SelectCabAdapter.ListItemClickListener {

    private String TAG = SelectCabFragment.class.getSimpleName();
    private Activity parentActivity;
    private View bottomSheetInternal, rootView;
    private String token;
    private User user;
    private Cab travelDetails;
    private ProgressBar progressBar;
    private TextView cabRulesView;
    private RecyclerView recyclerView;
    private RelativeLayout emptyView;
    private Button selectCabView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentActivity = getActivity();

        if (getArguments() != null) {
            travelDetails = (Cab) getArguments().getSerializable("travel_details");
        }

        rootView = inflater.inflate(R.layout.fragment_select_cab, container, false);
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
                bottomSheetInternal = d.findViewById(R.id.design_bottom_sheet);
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

        if (isConnectedToInternet()) {
            getCabFareList();
            progressBar.setVisibility(View.VISIBLE);
        } else
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
    }

    /*private void pullAndRefresh() {
        refreshCount++;
        if (refreshCount != 1)
            refreshLayout.setRefreshing(true);
        if (isConnectedToInternet())
            getCabFareList();
        else
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
    }*/

    private void getCabFareList() {
        EndPointInterface service = APIUtils.getAPIService(parentActivity);
        Call<Cab[]> call = service.cabFareList(
                user.getEmail(), token, travelDetails.getPickup(), travelDetails.getDrop(), travelDetails.getStartTime());

        call.enqueue(new Callback<Cab[]>() {
            @Override
            public void onResponse(@NonNull Call<Cab[]> call, @NonNull Response<Cab[]> response) {
                if (response.body() != null) {
                    if (parentActivity != null && isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        generateDataList(response.body(), travelDetails);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Cab[]> call, @NonNull Throwable t) {
                Log.e(TAG + " On Failure", t.getMessage());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(parentActivity, "Please check your internet connection or try again later.", Toast.LENGTH_LONG).show();
                dismiss();
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
        SelectCabAdapter selectCabAdapter = new SelectCabAdapter(getContext(), cabFareList, travelDetails, this);
        recyclerView.setAdapter(selectCabAdapter);
        int count = cabFareList.length != 0 ? cabFareList.length : 1;
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), count);
        recyclerView.setLayoutManager(layoutManager);

        if (layoutManager.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onListItemClick(final Cab selectedCabType, final String pickup, final String drop, final String startTime) {
        cabRulesView.setVisibility(View.VISIBLE);
        selectCabView.setVisibility(View.VISIBLE);
        switch (selectedCabType.getType()) {
            case "Compact":
                String compactRules =
                        "✪  One way trip of about " + selectedCabType.getCarName() + " km\n" +
                                "✪  Model Type: Indica, Swift, Alto, Ford Figo\n" +
                                "✪  Luggage Capacity: 0 big bags + 2 small bag\n" +
                                "✪  ₹100/hr will be charged for additional hours\n" +
                                "✪  Extra km is chargeable at ₹" + selectedCabType.getDriverName() + "/km";
                cabRulesView.setText(compactRules);
                break;
            case "Sedan":
                String sedanRules =
                        "✪  One way trip of about " + selectedCabType.getCarName() + " km\n" +
                                "✪  Model Type: Dzire, Etios, Indigo, Xcent\n" +
                                "✪  Luggage Capacity: 2 big bags + 1 small bag\n" +
                                "✪  ₹100/hr will be charged for additional hours\n" +
                                "✪  Extra km is chargeable at ₹" + selectedCabType.getDriverName() + "/km";
                cabRulesView.setText(sedanRules);
                break;
            case "SUV":
                String suvRules =
                        "✪  One way trip of about " + selectedCabType.getCarName() + " km\n" +
                                "✪  Model Type: Ertiga, Xylo, Innova\n" +
                                "✪  Luggage Capacity: 3 big bags + 2 small bags\n" +
                                "✪  ₹100/hr will be charged for additional hours\n" +
                                "✪  Extra km is chargeable at ₹" + selectedCabType.getDriverName() + "/km";
                cabRulesView.setText(suvRules);
                break;
            case "SUV+":
                String suvPlusRules =
                        "✪  One way trip of about " + selectedCabType.getCarName() + " km\n" +
                                "✪  Model Type: Ertiga, Xylo, Innova\n" +
                                "✪  Luggage Capacity: Equipped with carrier\n" +
                                "✪  ₹100/hr will be charged for additional hours\n" +
                                "✪  Extra km is chargeable at ₹" + selectedCabType.getDriverName() + "/km";
                cabRulesView.setText(suvPlusRules);
                break;
        }

        selectCabView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
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
        progressBar = rootView.findViewById(R.id.select_cab_progress_bar);
        cabRulesView = rootView.findViewById(R.id.select_cab_rules);
        recyclerView = rootView.findViewById(R.id.select_cab_recycler_view);
        emptyView = rootView.findViewById(R.id.select_cab_empty_layout);
        selectCabView = rootView.findViewById(R.id.select_cab_button);
    }
}