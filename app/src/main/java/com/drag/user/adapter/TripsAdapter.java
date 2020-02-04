package com.drag.user.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.drag.user.R;
import com.drag.user.model.Cab;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.TripsCardViewHolder> {

    private Cab[] trips;
    private ListItemClickListener mOnClickListener;

    public TripsAdapter(Cab[] trips, ListItemClickListener listener) {
        this.trips = trips;
        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public TripsCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_trips, parent, false);
        return new TripsCardViewHolder(view);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull TripsCardViewHolder holder, int position) {
        try {
            Calendar calendar = Calendar.getInstance();
            Date displayTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(trips[position].getStartTime());
            calendar.setTime(displayTime);
            calendar.add(Calendar.HOUR, 5);
            calendar.add(Calendar.MINUTE, 30);
            holder.startTimeView.setText(new SimpleDateFormat("EEE, MMM d, hh:mm a").format(calendar.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.pickupView.setText(trips[position].getPickup());
        holder.dropView.setText(trips[position].getDrop());
        String displayFare = "₹" + trips[position].getFare();
        holder.fareView.setText(displayFare);

        if (!trips[position].isShared()) {
            holder.idView.setText(trips[position].getRiders()[0].getTripId());
            holder.pickupView.setText(trips[position].getRiders()[0].getPickup());
            holder.dropView.setText(trips[position].getRiders()[0].getDrop());

            switch (trips[position].getRiders()[0].getTripStatus()) {
                case "Payment Successful":
                    holder.statusView.setBackgroundColor(Color.parseColor("#507dff"));
                    holder.statusView.setText(R.string.payment_successful);
                    break;
                case "Trip Confirmed":
                    holder.statusView.setBackgroundColor(Color.parseColor("#edaf02"));
                    holder.statusView.setText(R.string.trip_confirmed);
                    break;
                case "Payment Failed":
                    holder.statusView.setBackgroundColor(Color.parseColor("#ff4c4c"));
                    holder.statusView.setText(R.string.payment_failed);
                    break;
                case "Cancelled":
                    holder.statusView.setBackgroundColor(Color.parseColor("#ff4c4c"));
                    holder.statusView.setText(R.string.trip_cancelled);
                    break;
                default:
                    holder.statusView.setBackgroundColor(Color.parseColor("#edaf02"));
                    holder.statusView.setText(R.string.payment_in_process);
                    break;
            }
        }

            /*if (riders[position].getCab().isAvailable()) {
                switch (riders[position].getCab().getSeats()) {
                    case "1":
                        holder.statusView.setBackgroundColor(Color.parseColor("#26888888"));
                        holder.statusView.setTextColor(Color.parseColor("#2ecc71"));
                        String displayStatus1 = "1 seat left";
                        holder.statusView.setText(displayStatus1);
                        break;
                    case "2":
                        holder.statusView.setBackgroundColor(Color.parseColor("#26888888"));
                        holder.statusView.setTextColor(Color.parseColor("#edaf02"));
                        String displayStatus2 = "2 seats left";
                        holder.statusView.setText(displayStatus2);
                        break;
                    case "3":
                        holder.statusView.setBackgroundColor(Color.parseColor("#26888888"));
                        holder.statusView.setTextColor(Color.parseColor("#ff4c4c"));
                        String displayStatus3 = "3 seats left";
                        holder.statusView.setText(displayStatus3);
                        break;
                }
            } else {
                if (riders[position].getCab().getCarName() == null) {
                    holder.statusView.setBackgroundColor(Color.parseColor("#ff8247"));
                    String displayStatus = "Allocating Cab";
                    holder.statusView.setText(displayStatus);
                } else {
                    holder.statusView.setBackgroundColor(Color.parseColor("#2ecc71"));
                    String displayStatus = "Ride Confirmed";
                    holder.statusView.setText(displayStatus);
                }
            } */
    }

    @Override
    public int getItemCount() {
        return trips.length;
    }

    /*@SuppressLint("SimpleDateFormat")
    private void getRideTime(String selectedRideTime) {
        try {
            if (selectedRideTime != null) {
                Calendar calendar = Calendar.getInstance();
                Date rideTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(selectedRideTime);
                calendar.setTime(rideTime);
                calendar.add(Calendar.HOUR, 5);
                calendar.add(Calendar.MINUTE, 30);
                startTime = new SimpleDateFormat("MMM d, hh:mm a").format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void shareViaWhatsapp(String pickup, String drop) {
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Probably the last time I'll be buzzing around my flight timings.  " +
                "*" + startTime + "*" + " - Join me in my journey from " + pickup + " to " +
                drop + " on play.google.com/store/apps/details?id=com.drag.user \uD83D\uDC4B");
        try {
            parentActivity.startActivity(whatsappIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(parentActivity, "Whatsapp is not installed.", Toast.LENGTH_SHORT).show();
        }
    }*/

    public interface ListItemClickListener {
        void onListItemClick(Cab[] trips, int position);
    }

    class TripsCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView idView, pickupView, dropView, startTimeView, fareView, statusView;

        TripsCardViewHolder(View itemView) {
            super(itemView);
            idView = itemView.findViewById(R.id.trips_id);
            pickupView = itemView.findViewById(R.id.trips_pickup);
            dropView = itemView.findViewById(R.id.trips_drop);
            startTimeView = itemView.findViewById(R.id.trips_start_time);
            fareView = itemView.findViewById(R.id.trips_fare);
            statusView = itemView.findViewById(R.id.trips_status);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int itemPosition = getLayoutPosition();
            mOnClickListener.onListItemClick(trips, itemPosition);
            //getRideTime(riders[itemPosition].getCab().getStartTime());
            //shareViaWhatsapp(riders[itemPosition].getCab().getPickup(), riders[itemPosition].getCab().getDrop());
        }
    }
}