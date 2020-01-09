package com.alainsaris.stitcha;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Time;

public class Feed {
    private String station;
    private boolean safe;
    private Timestamp timestamp;
    private String userId;


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Feed() {
        //needed for firestore
    }

    public Feed(String station, boolean stationVote, Timestamp timestamp, String userId) {

        this.safe = stationVote;
        this.timestamp = timestamp;
        this.station = station;
        this.userId = userId;
    }


    public String getSafe() {
        return String.valueOf(safe);
    }

    public String getTimestamp() {
        return String.valueOf(timestamp.toDate());
    }

    public String getUserId() {
        return userId;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public boolean getSafeBool() {
        return safe;
    }
}
