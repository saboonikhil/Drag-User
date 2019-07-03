package com.drag.user.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SelectCabAdapter extends RecyclerView.Adapter<SelectCabAdapter.SelectCabHolder> {

    private Context context;
    private List<Cab> cabList;
    private ListItemClickListener mOnClickListener;
    private Cab travelDetails;
    private int itemCount = 0;
    private View clickedItem;
    private String[] startTime;

    public SelectCabAdapter(Context context, List<Cab> cabList, Cab travelDetails, String[] startTime, ListItemClickListener listener) {
        this.context = context;
        this.cabList = cabList;
        this.travelDetails = travelDetails;
        this.startTime = startTime;
        mOnClickListener = listener;
    }

    public void refreshData(List<Cab> dataSet, String[] startTimeData) {
        cabList = dataSet;
        startTime = startTimeData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SelectCabHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_select_cab, parent, false);
        return new SelectCabHolder(view);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull SelectCabHolder holder, int position) {
        holder.rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));

        try {
            if (cabList.get(position).getStartTime() != null) {
                Calendar cabCalendar = Calendar.getInstance();
                Date cabStartTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(cabList.get(position).getStartTime());
                cabCalendar.setTime(cabStartTime);
                cabCalendar.add(Calendar.HOUR, 5);
                cabCalendar.add(Calendar.MINUTE, 30);
                String cabTime = new SimpleDateFormat("hh:mm a").format(cabCalendar.getTime());
                if (cabTime.equals("12:00 PM")) {
                    Calendar travelCalendar = Calendar.getInstance();
                    Date travelStartTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(travelDetails.getStartTime());
                    travelCalendar.setTime(travelStartTime);

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String isoDate = df.format(cabStartTime);
                    DateFormat df1 = new SimpleDateFormat("HH:mm:ss.SSS");
                    String isoTime = df1.format(travelStartTime);
                    String startDateTime = isoDate + 'T' + isoTime + 'Z';
                    startTime[position] = startDateTime;
                    try {
                        Calendar calendar = Calendar.getInstance();
                        Date startTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(startDateTime);
                        calendar.setTime(startTime);
                        calendar.add(Calendar.HOUR, 5);
                        calendar.add(Calendar.MINUTE, 30);
                        holder.startTimeView.setText(new SimpleDateFormat("hh:mm a, d MMM YY").format(calendar.getTime()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    holder.startTimeView.setText(new SimpleDateFormat("hh:mm a, d MMM YY").format(cabCalendar.getTime()));
                    startTime[position] = cabList.get(position).getStartTime();
                }
            } else {
                Calendar travelCalendar = Calendar.getInstance();
                Date travelStartTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(travelDetails.getStartTime());
                travelCalendar.setTime(travelStartTime);
                travelCalendar.add(Calendar.HOUR, 5);
                travelCalendar.add(Calendar.MINUTE, 30);
                holder.startTimeView.setText(new SimpleDateFormat("hh:mm a, d MMM YY").format(travelCalendar.getTime()));
                startTime[position] = travelDetails.getStartTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.carNameView.setText(cabList.get(position).getCarName());
        holder.seatsView.setText(cabList.get(position).getSeats());

        String displayFare = "₹ " + cabList.get(position).getFare();
        holder.fareView.setText(displayFare);
    }

    @Override
    public int getItemCount() {
        if (cabList != null) {
            itemCount = cabList.size();
        } else
            Toast.makeText(context, "Can't connect to Drag servers", Toast.LENGTH_LONG).show();
        return itemCount;
    }

    public interface ListItemClickListener {
        void onListItemClick(Cab selectedCab, String pickup, String drop, String startTime);
    }

    class SelectCabHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView rootView;
        private TextView startTimeView, carNameView, seatsView, fareView;

        SelectCabHolder(View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.select_cab_layout);
            carNameView = itemView.findViewById(R.id.select_cab_car_name);
            seatsView = itemView.findViewById(R.id.select_cab_seats);
            startTimeView = itemView.findViewById(R.id.select_cab_start_time);
            fareView = itemView.findViewById(R.id.select_cab_fare);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickedItem != null)
                clickedItem.setBackgroundColor(ContextCompat.getColor(context, R.color.white));

            view.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_select_cab_pressed));
            clickedItem = view;
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(cabList.get(clickedPosition), travelDetails.getPickup(),
                    travelDetails.getDrop(), startTime[clickedPosition]);
        }
    }
}