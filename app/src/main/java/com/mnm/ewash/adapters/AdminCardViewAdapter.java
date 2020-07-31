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

public class AdminCardViewAdapter extends RecyclerView.Adapter<AdminCardViewHolder>{
    Context context;
    List<PendingRequest> itemList;
    RecyclerClickListener recyclerClickListener;

    public static final String USER_BUTTON = "USER_BUTTON";
    public static final String SHARE_BUTTON = "SHARE_BUTTON";
    public static final String ACCEPT_BUTTON = "ACCEPT_BUTTON";
    public static final String REJECT_BUTTON = "REJECT_BUTTON";
    public static final String COMPLETED_BUTTON = "COMPLETED_BUTTON";
    public static final String CANCELLED_BUTTON = "CANCELLED_BUTTON";

    public AdminCardViewAdapter(Context context, List<PendingRequest> itemList, RecyclerClickListener recyclerClickListener) {
        this.context = context;
        this.itemList = itemList;
        this.recyclerClickListener = recyclerClickListener;
    }

    @Override
    public AdminCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_card, parent, false);
        return new AdminCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(final AdminCardViewHolder holder, int position) {
        final PendingRequest request = itemList.get(position);
        holder.description.setText("User Request:");
        holder.amount.setText(request.amount);
        holder.location.setText(request.address);
        holder.wash.setText(request.wash);
        holder.vehicle.setText(request.vehicle);
        holder.vehicleAmount.setText("Vehicles Amount: "+request.vehicleAmount);
        holder.washerName.setText("Washer Name: "+request.washerName);
        holder.dateTime.setText(DateUtils.formatDateTime(context, Long.parseLong(request.time), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE));
        holder.status.setText(request.status);
        if(request.status.equals("COMPLETED")){
            holder.acceptButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
            holder.shareButton.setVisibility(View.GONE);
        }else if(request.status.equals("PENDING")){
            holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerClickListener.onClick(holder.getAdapterPosition(), ACCEPT_BUTTON);
                }
            });
            holder.rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerClickListener.onClick(holder.getAdapterPosition(), REJECT_BUTTON);
                }
            });
        }else if(request.status.equals("ACCEPTED")){
            holder.acceptButton.setText("COMPLETE");
            holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerClickListener.onClick(holder.getAdapterPosition(), COMPLETED_BUTTON);
                }
            });
            holder.rejectButton.setText("CANCEL");
            holder.rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerClickListener.onClick(holder.getAdapterPosition(), CANCELLED_BUTTON);
                }
            });
        }else if(request.status.contains("REJECTED")||request.status.contains("CANCELLED")){
            holder.acceptButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
            holder.shareButton.setVisibility(View.GONE);
        }

        holder.userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerClickListener.onClick(holder.getAdapterPosition(), USER_BUTTON);
            }
        });
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerClickListener.onClick(holder.getAdapterPosition(), SHARE_BUTTON);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
