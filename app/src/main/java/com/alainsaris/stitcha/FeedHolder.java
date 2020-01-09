package com.alainsaris.stitcha;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class FeedHolder extends RecyclerView.ViewHolder {

    public TextView stationNameTextView;
//    public TextView stationVoteTextView;
    public TextView timeStampTextView;
    public ConstraintLayout constraintLayout;

    public FeedHolder(@NonNull View itemView) {
        super(itemView);

        stationNameTextView = itemView.findViewById(R.id.feed_element_station_name);
//        stationVoteTextView = itemView.findViewById(R.id.feed_element_station_vote);
        timeStampTextView = itemView.findViewById(R.id.feed_element_timestamp);
        constraintLayout = itemView.findViewById(R.id.feed_element_constraint_layout);
    }
}
