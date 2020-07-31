package com.mnm.ewash.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class PendingRequest {
    public String requestId;
    public String uid;
    public String latitude;
    public String longitude;
    public String address;
    public String time;
    public String wash;
    public String vehicle;
    public String amount;
    public String status;
    public String vehicleAmount;
    public String washerName;

    public PendingRequest() {
    }

    public PendingRequest(String requestId, String uid, String latitude, String longitude, String address, String time, String wash, String vehicle, String amount, String status,String vehicleAmount, String washerName) {
        this.requestId = requestId;
        this.uid = uid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.time = time;
        this.wash = wash;
        this.vehicle = vehicle;
        this.amount = amount;
        this.status = status;
        this.vehicleAmount = vehicleAmount;
        this.washerName = washerName;
    }
}
