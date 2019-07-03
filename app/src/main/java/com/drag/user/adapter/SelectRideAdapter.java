package com.drag.user.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.drag.user.R;
import com.drag.user.model.Cab;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SelectRideAdapter extends RecyclerView.Adapter<SelectRideAdapter.SelectRideHolder> {

    private Context context;
    private List<Cab> rideList;
    private ListItemClickListener mOnClickListener;
    private int itemCount = 0;
    private View clickedItem;

    public SelectRideAdapter(Context context, List<Cab> rideList, ListItemClickListener listener) {
        this.context = context;
        this.rideList = rideList;
        mOnClickListener = listener;
    }

    public void refreshData(List<Cab> dataSet) {
        rideList = dataSet;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SelectRideHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_select_ride, parent, false);
        return new SelectRideHolder(view);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull SelectRideHolder holder, int position) {
        holder.rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));

        try {
            if (rideList.get(position).getStartTime() != null) {
                Calendar calendar = Calendar.getInstance();
                Date startTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(rideList.get(position).getStartTime());
                calendar.setTime(startTime);
                calendar.add(Calendar.HOUR, 5);
                calendar.add(Calendar.MINUTE, 30);
                holder.startTimeView.setText(new SimpleDateFormat("hh:mm a, d MMM YY").format(calendar.getTime()));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (rideList.get(position).getSeats()) {
            case "1":
                holder.statusView.setTextColor(Color.parseColor("#ff4c4c"));
                String displayStatus1 = "1 Seat Left";
                holder.statusView.setText(displayStatus1);
                break;
            case "2":
                holder.statusView.setTextColor(Color.parseColor("#edaf02"));
                String displayStatus2 = "2 Seats Left";
                holder.statusView.setText(displayStatus2);
                break;
            case "3":
                holder.statusView.setTextColor(Color.parseColor("#507dff"));
                String displayStatus3 = "3 Seats Left";
                holder.statusView.setText(displayStatus3);
                break;
            case "4":
                holder.statusView.setTextColor(Color.parseColor("#507dff"));
                String displayStatus4 = "4 Seats Left";
                holder.statusView.setText(displayStatus4);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (rideList != null)
            itemCount = rideList.size();
        else
            Toast.makeText(context, "Can't connect to Drag servers", Toast.LENGTH_LONG).show();
        return itemCount;
    }

    public interface ListItemClickListener {
        void onListItemClick(Cab selectedCab);
    }

    class SelectRideHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView rootView;
        private TextView statusView, startTimeView;

        SelectRideHolder(View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.select_ride_layout);
            statusView = itemView.findViewById(R.id.select_ride_status);
            startTimeView = itemView.findViewById(R.id.select_ride_start_time);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickedItem != null)
                clickedItem.setBackgroundColor(ContextCompat.getColor(context, R.color.white));

            view.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_select_cab_pressed));
            clickedItem = view;
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(rideList.get(clickedPosition));
        }
    }
}