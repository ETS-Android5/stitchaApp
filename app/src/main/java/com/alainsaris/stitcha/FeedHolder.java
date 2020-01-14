package com.alainsaris.stitcha;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class FeedHolder extends RecyclerView.ViewHolder {

    public TextView stationNameTextView;
    //    public TextView stationVoteTextView;
    public TextView userName;
    public TextView timeStampTextView;
    public ConstraintLayout constraintLayout;
    public ImageView profilePictureImageView;
    public TextView usernameTextView;

    public FeedHolder(@NonNull View itemView) {
        super(itemView);

        stationNameTextView = itemView.findViewById(R.id.feed_element_station_name);
//        stationVoteTextView = itemView.findViewById(R.id.feed_element_station_vote);
        timeStampTextView = itemView.findViewById(R.id.feed_element_timestamp);
        constraintLayout = itemView.findViewById(R.id.feed_element_constraint_layout);
        profilePictureImageView = itemView.findViewById(R.id.map_profile_picture);
        usernameTextView = itemView.findViewById(R.id.feed_profile_name);

    }
}
