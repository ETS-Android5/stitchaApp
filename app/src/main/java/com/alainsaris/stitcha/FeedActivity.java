package com.alainsaris.stitcha;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;

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
    private Location lastLocation;
    private double latitude;
    private double longitude;

    private boolean currentVote;

    //    private FeedAdapter adapter;
    private FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            latitude = extras.getDouble("LATITUDE");
            longitude = extras.getDouble("LONGITUDE");
        }

        feedRef = db.collection("/markers/");
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        //this is what i want to use
        GeoFirestore geoFirestore = new GeoFirestore(feedRef);
        GeoQuery geoQuery = geoFirestore.queryAtLocation(
                new GeoPoint(latitude, longitude),
                15);// 15 kms

        // this is the query i'm using currently
        Query query = feedRef
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        FirestoreRecyclerOptions<Feed> options = new FirestoreRecyclerOptions.Builder<Feed>()
                .setQuery(query, Feed.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<Feed, FeedHolder>(options) {
            @Override
            public FeedHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.feed_element, group, false);
                return new FeedHolder(view);
            }

            @Override
            public void onBindViewHolder(FeedHolder holder, int position, Feed model) {
                // Bind the marker object to the ChatHolder
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

                currentVote = model.getSafeBool();

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

    /**
     * slide the view from down to up and don't fill the whole screen
     * (it will show the map in the background)
     */

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * 1), (int) (height * 0.8));

        View view = getWindow().getDecorView();
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
        lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
        getWindowManager().updateViewLayout(view, lp);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        // Use exiting animations specified by the parent activity if given
        // Translate left if not specified.
        overridePendingTransition(R.anim.down_in, R.anim.down_out);
    }
}
