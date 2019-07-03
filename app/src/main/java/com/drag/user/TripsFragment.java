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
import com.drag.user.model.Trip;
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
    private SharedPreferences pref;
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

        pref = parentActivity.getSharedPreferences("AppPref", MODE_PRIVATE);
        token = pref.getString("token", "");
        String json = pref.getString("dbObj", "");
        user = new Gson().fromJson(json, User.class);

        generateArrayData(user.getTrips());
        if (user.getTrips().length == 0)
            progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTripsData();
    }

    private void updateTripsData() {
        EndPointInterface service = APIUtils.getAPIService(parentActivity);
        service.userDetail(user.get_id(), user.getEmail(), token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.body() != null) {
                    progressBar.setVisibility(View.GONE);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("dbObj", new Gson().toJson(response.body()));
                    edit.apply();
                    tripsAdapter.refreshData(response.body().getTrips());
                    if (response.body().getTrips().length == 0)
                        emptyView.setVisibility(View.VISIBLE);
                    else if (response.body().getTrips().length > 0)
                        emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG + " On Failure", t.getMessage());
                progressBar.setVisibility(View.GONE);
                if (tripsAdapter.getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void generateArrayData(Trip[] trips) {
        tripsAdapter = new TripsAdapter(parentActivity, trips, this);
        recyclerView.setAdapter(tripsAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
    }

    @Override
    public void onListItemClick(Trip[] trips, int itemPosition) {
        TripDetailsFragment tripDetails = new TripDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("trip_details", trips);
        bundle.putInt("position", itemPosition);
        tripDetails.setArguments(bundle);
        tripDetails.show(getChildFragmentManager(), "Trip Details");
    }

    private void initViews() {
        recyclerView = rootView.findViewById(R.id.trips_recycler_view);
        emptyView = rootView.findViewById(R.id.trips_empty_view);
        progressBar = rootView.findViewById(R.id.trips_progress_bar);
    }
}