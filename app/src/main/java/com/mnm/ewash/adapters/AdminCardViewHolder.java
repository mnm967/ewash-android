package com.mnm.ewash.adapters;


import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mnm.ewash.R;

public class AdminCardViewHolder extends RecyclerView.ViewHolder{
    TextView description;
    TextView amount;
    TextView location;
    TextView wash;
    TextView vehicle;
    TextView vehicleAmount;
    TextView washerName;
    TextView dateTime;
    TextView status;
    Button userButton;
    Button shareButton;
    Button acceptButton;
    Button rejectButton;
    public AdminCardViewHolder(View itemView) {
        super(itemView);
        description = itemView.findViewById(R.id.history_description);
        amount = itemView.findViewById(R.id.history_amount);
        location = itemView.findViewById(R.id.history_location);
        wash = itemView.findViewById(R.id.history_wash);
        vehicle = itemView.findViewById(R.id.history_vehicle);
        vehicleAmount = itemView.findViewById(R.id.vehicle_amount);
        washerName = itemView.findViewById(R.id.washer_name);
        dateTime = itemView.findViewById(R.id.history_date_time);
        status = itemView.findViewById(R.id.history_status);
        userButton = itemView.findViewById(R.id.admin_user_details_button);
        shareButton = itemView.findViewById(R.id.admin_share_button);
        acceptButton = itemView.findViewById(R.id.admin_accept_button);
        rejectButton = itemView.findViewById(R.id.admin_reject_button);
    }
}
