package com.mnm.ewash.adapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnm.ewash.R;
import com.mnm.ewash.models.PendingRequest;

import java.util.List;

public class HistoryCardViewAdapter extends RecyclerView.Adapter<HistoryCardViewHolder>{
    Context context;
    List<PendingRequest> itemList;

    public HistoryCardViewAdapter(Context context, List<PendingRequest> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public HistoryCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_card, parent, false);
        return new HistoryCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(HistoryCardViewHolder holder, int position) {
        PendingRequest request = itemList.get(position);
        holder.description.setText("Order Details:");
        holder.amount.setText(request.amount);
        holder.location.setText(request.address);
        holder.wash.setText(request.wash);
        holder.vehicle.setText(request.vehicle);
        holder.vehicleAmount.setText("Vehicles Amount: "+request.vehicleAmount);
        holder.washerName.setText("Washer Name: "+request.washerName);
        holder.dateTime.setText(DateUtils.formatDateTime(context, Long.parseLong(request.time), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE));
        holder.status.setText(request.status);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
