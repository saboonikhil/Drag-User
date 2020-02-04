package com.drag.user;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.drag.user.adapter.TripsAdapter;
import com.drag.user.model.Cab;
import com.drag.user.model.User;
import com.drag.user.network.APIUtils;
import com.drag.user.network.EndPointInterface;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class TripsFragment extends Fragment implements TripsAdapter.ListItemClickListener {

    private String TAG = TripsFragment.class.getSimpleName();
    private Activity parentActivity;
    private View rootView;
    private String token;
    private User user;
    private TripsAdapter tripsAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentActivity = getActivity();
        rootView = inflater.inflate(R.layout.fragment_trips, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();

        SharedPreferences pref = parentActivity.getSharedPreferences("AppPref", MODE_PRIVATE);
        token = pref.getString("token", "");
        String json = pref.getString("dbObj", "");
        user = new Gson().fromJson(json, User.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tripsAdapter == null)
            progressBar.setVisibility(View.VISIBLE);
        updateTripsData();
    }

    private void updateTripsData() {
        EndPointInterface service = APIUtils.getAPIService(parentActivity);
        service.userTrips(user.get_id(), user.getEmail(), token).enqueue(new Callback<Cab[]>() {
            @Override
            public void onResponse(@NonNull Call<Cab[]> call, @NonNull Response<Cab[]> response) {
                if (response.body() != null) {
                    progressBar.setVisibility(View.GONE);
                    generateArrayData(response.body());
                    if (response.body().length == 0)
                        emptyView.setVisibility(View.VISIBLE);
                    else
                        emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Cab[]> call, @NonNull Throwable t) {
                Log.e(TAG + " On Failure", t.getMessage());
                progressBar.setVisibility(View.GONE);
                if (tripsAdapter != null) {
                    if (tripsAdapter.getItemCount() == 0)
                        emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void generateArrayData(Cab[] trips) {
        tripsAdapter = new TripsAdapter(trips, this);
        recyclerView.setAdapter(tripsAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
    }

    @Override
    public void onListItemClick(Cab[] trips, int itemPosition) {
        String tripStatus = trips[itemPosition].getRiders()[0].getTripStatus();
        if (!tripStatus.equals("Payment Failed") && !tripStatus.equals("Cancelled")) {
            TripDetailsFragment tripDetails = new TripDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("trip_details", trips);
            bundle.putInt("position", itemPosition);
            tripDetails.setArguments(bundle);
            tripDetails.show(getChildFragmentManager(), "Trip Details");
        }
    }

    private void initViews() {
        recyclerView = rootView.findViewById(R.id.trips_recycler_view);
        emptyView = rootView.findViewById(R.id.trips_empty_view);
        progressBar = rootView.findViewById(R.id.trips_progress_bar);
    }
}