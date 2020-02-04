package com.drag.user.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.drag.user.R;
import com.drag.user.model.Cab;

public class SelectCabAdapter extends RecyclerView.Adapter<SelectCabAdapter.SelectCabHolder> {

    private Context context;
    private Cab[] cabFareList;
    private ListItemClickListener mOnClickListener;
    private Cab travelDetails;
    private int itemCount = 0;
    private View clickedItem;

    public SelectCabAdapter(Context context, Cab[] cabFareList, Cab travelDetails, ListItemClickListener listener) {
        this.context = context;
        this.cabFareList = cabFareList;
        this.travelDetails = travelDetails;
        mOnClickListener = listener;
    }

    /*public void refreshData(Cab[] dataSet) {
        cabFareList = dataSet;
        notifyDataSetChanged();
    }*/

    @NonNull
    @Override
    public SelectCabHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_select_cab, parent, false);
        return new SelectCabHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectCabHolder holder, int position) {

        switch (cabFareList[position].getType()) {
            case "Compact":
                holder.typeView.setText(R.string.compact);
                holder.seatsView.setText("4");
                holder.iconView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sedan));
                break;
            case "Sedan":
                holder.typeView.setText(R.string.sedan);
                holder.seatsView.setText("4");
                holder.iconView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sedan));
                holder.item.callOnClick();
                break;
            case "SUV":
                holder.typeView.setText(R.string.suv);
                holder.seatsView.setText("6");
                holder.iconView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_suv));
                break;
            case "SUV+":
                holder.typeView.setText(R.string.suv_plus);
                holder.seatsView.setText("6");
                holder.iconView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_suv));
                break;
        }

        String displayFare = "₹" + cabFareList[position].getCarNumber();
        holder.fareView.setText(displayFare);
    }

    @Override
    public int getItemCount() {
        if (cabFareList != null) {
            itemCount = cabFareList.length;
        } else
            Toast.makeText(context, "Can't connect to Drag servers", Toast.LENGTH_LONG).show();
        return itemCount;
    }

    public interface ListItemClickListener {
        void onListItemClick(Cab selectedCab, String pickup, String drop, String startTime);
    }

    class SelectCabHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView iconView;
        private TextView typeView, seatsView, fareView;
        private View item;

        SelectCabHolder(View itemView) {
            super(itemView);
            item = itemView;
            iconView = itemView.findViewById(R.id.select_cab_icon);
            typeView = itemView.findViewById(R.id.select_cab_type);
            seatsView = itemView.findViewById(R.id.select_cab_seats);
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
            mOnClickListener.onListItemClick(cabFareList[clickedPosition], travelDetails.getPickup(),
                    travelDetails.getDrop(), travelDetails.getStartTime());
        }
    }
}