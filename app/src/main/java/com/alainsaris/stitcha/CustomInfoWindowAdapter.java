package com.alainsaris.thisisit;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;
    private Context mContext;
    private FirebaseFirestore db;
    private String selectedStation;
    private long safeVotes;
    private long unsafeVotes;
    private Marker marker;
    private String title;

    public CustomInfoWindowAdapter(Context mContext) {
        db = FirebaseFirestore.getInstance();
        this.mContext = mContext;
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window, null);
    }

    private void readData(final Marker marker) {
        renderWindowText(marker, mWindow);
        //        HashMap stationInfo = (HashMap) marker.getTag();
//        int safeVotes = (int) stationInfo.get("safe votes");
//        int unsafeVotes = (int) stationInfo.get("unsafe votes");
        //getting the number of votes to display
        db.collection("cities").document("lille").collection("lille stations").whereEqualTo("location", marker.getPosition())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            selectedStation = document.getId();
                            DocumentReference currentStationRef = db.collection("cities").document("lille").collection("lille stations").document(selectedStation);
                            currentStationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    long safeVotes = 0;
                                    long unsafeVotes = 0;
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                                            Toast.makeText(MapActivity.this, "" + document.getData(), Toast.LENGTH_SHORT).show();
                                            Map<String, Object> stationHashmap = document.getData();
                                            safeVotes = (long) stationHashmap.get("safe votes");
                                            CustomInfoWindowAdapter.this.safeVotes = safeVotes;
                                            unsafeVotes = (long) stationHashmap.get("unsafe votes");
                                            CustomInfoWindowAdapter.this.unsafeVotes = unsafeVotes;
//                                            Toast.makeText(MapActivity.this, "safe: " + safeVotes + "; unsafe votes: " + unsafeVotes, Toast.LENGTH_SHORT).show();
                                            //putting info in the custom info window
                                            title = marker.getTitle();


//                                            String snippet = marker.getSnippet();
                                        } else {
                                            Log.d(TAG, "No such document");
                                        }
                                    } else {
                                        Log.d(TAG, "get failed with ", task.getException());
                                    }
                                }
                            });
                        }

//                        Toast.makeText(MapActivity.this, "SELECTED: " + selectedStation, Toast.LENGTH_SHORT).show();
//                        selectedStationTextView.setText(selectedStation);

                    }
                });

    }

    private void renderWindowText(final Marker marker, final View view) {
        //gui options
        TextView titleTextView = view.findViewById(R.id.station_name);
        titleTextView.setText(title);
        TextView safeVotesTextView = view.findViewById(R.id.safe_votes);
        safeVotesTextView.setText("SafeVotes: " + safeVotes);
        TextView unsafeVotesTextView = view.findViewById(R.id.unsafe_votes);
        unsafeVotesTextView.setText("Unsafe Votes: " + unsafeVotes);

    }

    @Override
    public View getInfoWindow(Marker marker) {
        this.marker = marker;
        readData(marker);
        renderWindowText(marker, mWindow);
//        return mWindow;
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        this.marker = marker;
        readData(marker);
        renderWindowText(marker, mWindow);
//        return mWindow;
        return null;
    }

    private interface FirestoreCallback{
        void onCallback(List<String> list);
    }
}


