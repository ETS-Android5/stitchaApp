package com.alainsaris.stitcha;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

/**
 * THIS CLASS IS NOT USED FOR THE MOMENT
 */
public class FeedAdapter extends FirestoreRecyclerAdapter<Feed, FeedAdapter.FeedHolder> {

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FeedAdapter(@NonNull FirestoreRecyclerOptions<Feed> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FeedHolder holder, int position, @NonNull Feed model) {
        holder.textViewStationName.setText(model.getStation());
//        holder.textViewStationVote.setText(model.getSafe());
        holder.textViewTimestamp.setText(model.getTimestamp());
    }

    @NonNull
    @Override
    public FeedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_element,
                parent,
                false);

        return new FeedHolder(v);
    }

    class FeedHolder extends RecyclerView.ViewHolder {
        TextView textViewStationName;
//        TextView textViewStationVote;
        TextView textViewTimestamp;

        public FeedHolder(@NonNull View itemView) {
            super(itemView);
            textViewStationName = itemView.findViewById(R.id.feed_element_station_name);
//            textViewStationVote = itemView.findViewById(R.id.feed_element_station_vote);
            textViewTimestamp = itemView.findViewById(R.id.feed_element_timestamp);
        }
    }
}
