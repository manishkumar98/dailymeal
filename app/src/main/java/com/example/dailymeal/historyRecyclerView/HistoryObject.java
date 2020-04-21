package com.example.dailymeal.historyRecyclerView;

public class HistoryObject {
    private String rideId;
    private String time;
    public HistoryObject(String rideId){
        this.rideId=rideId;
    }

    public HistoryObject(String rideId, String date) {
    }

    public String getRideId(){
        return rideId;
    }
    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTime(){return time;}
    public void setTime(String time) {
        this.time = time;
    }
}
