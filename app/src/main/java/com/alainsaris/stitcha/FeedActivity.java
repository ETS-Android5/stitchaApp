package com.alainsaris.stitcha;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class FeedActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String currentCity;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;
    private String station;
    private CollectionReference feedRef;
    private ImageView profilePicture;

    private boolean currentVote;

    //    private FeedAdapter adapter;
    private FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentCity = extras.getString("CURRENT_CITY");
//            userId = extras.getString("USER_ID");
//            station = extras.getString("STATION");
        }

        //        feedRef = db.collection("/cities/" + currentCity + "/" + currentCity + " feed/" + userId + station);
        feedRef = db.collection("/cities/" + currentCity + "/" + currentCity + " feed");

        setUpRecyclerView();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView

//        Toast.makeText(this, "" + currentCity, Toast.LENGTH_SHORT).show();
    }

    private void setUpRecyclerView() {
        Query query = feedRef
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        FirestoreRecyclerOptions<Feed> options = new FirestoreRecyclerOptions.Builder<Feed>()
                .setQuery(query, Feed.class)
                .build();

//        adapter = new FeedAdapter(options);

        adapter = new FirestoreRecyclerAdapter<Feed, FeedHolder>(options) {
            @Override
            public FeedHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.feed_element, group, false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    view.setClipToOutline(true);
                }
                return new FeedHolder(view);
            }

            @Override
            public void onBindViewHolder(FeedHolder holder, int position, Feed model) {
                // Bind the Chat object to the ChatHolder
                // ...
                TextView stationName = holder.stationNameTextView;
                TextView userName = holder.usernameTextView;
                profilePicture = holder.profilePictureImageView;
                stationName.setText(model.getStation());
                if (model.getProfilPicUri() == null) {
                    Picasso.get().load(R.drawable.default_avatar).into(profilePicture);
                } else if (model.getProfilPicUri() != "") {
                    Picasso.get().load(model.getProfilPicUri()).into(profilePicture);
                }
                if (model.getUserName() == null) {
                    userName.setText("anon");
                } else {
                    userName.setText(model.getUserName());
                }
//                TextView stationVote = holder.stationVoteTextView;
//                stationVote.setText(model.getSafe());
//                stationVote.setVisibility(View.GONE);

                currentVote = model.getSafeBool();

                //to change bg color
                //old
//                if (currentVote) {
//                    holder.constraintLayout.setBackgroundColor(Color.parseColor("#4CA64C"));
//                } else {
//                    holder.constraintLayout.setBackgroundColor(Color.parseColor("#ff4c4c"));
//                }

                holder.constraintLayout.setClipToPadding(true);

                TextView timeStamp = holder.timeStampTextView;
                timeStamp.setText(model.getTimestamp());
            }
        };

        RecyclerView recyclerView = findViewById(R.id.feed_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
