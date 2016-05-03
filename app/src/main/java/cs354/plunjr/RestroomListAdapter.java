package cs354.plunjr;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class RestroomListAdapter extends RecyclerView.Adapter<RestroomListAdapter.RestroomViewHolder> {

    private List<RestroomInfo> restrooms;

    public RestroomListAdapter(List<RestroomInfo> restrooms) {
        this.restrooms = restrooms;
    }

    @Override
    public RestroomListAdapter.RestroomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View restroomView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.restroom_list_row, parent, false);

        return new RestroomViewHolder(restroomView);
    }

    @Override
    public void onBindViewHolder(final RestroomViewHolder rrHolder, int position) {
        final RestroomInfo rr = restrooms.get(position);

        // Bind data to view
        rrHolder.name.setText(rr.name);
        rrHolder.address.setText(rr.address);
        rrHolder.rating.setRating(rr.rating);
        rrHolder.reviewCount.setText(
                String.format(rrHolder.reviewCount.getContext().getString(R.string.review_count_format), rr.reviewCount)
        );
        rrHolder.distance.setText(rr.distance);

        // Set holder on click listener
        rrHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ReviewListActivity.class);
                intent.putExtra("restroomID", rr.id);
                intent.putExtra("restroomName", rr.name);
                view.getContext().startActivity(intent);
            }
        });
        // Set directions button on click listener
        rrHolder.directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = rrHolder.itemView.getContext();
                Uri gmmIntentUri = Uri.parse(String.format(context.getString(R.string.google_maps_directions_uri), rr.address));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                context.startActivity(mapIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return restrooms.size();
    }

    public void clear() {
        restrooms.clear();
    }

    public boolean add(RestroomInfo rr) {
        return restrooms.add(rr);
    }

    public RestroomInfo get(int idx) {
        return restrooms.get(idx);
    }

    public static class RestroomViewHolder extends RecyclerView.ViewHolder {

        protected TextView name;
        protected TextView address;
        protected TextView distance;
        protected TextView reviewCount;
        protected RatingBar rating;
        protected ImageButton directionsButton;

        public RestroomViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.listRowTitle);
            address = (TextView) v.findViewById(R.id.listRowAddress);
            distance = (TextView) v.findViewById(R.id.listRowDistance);
            reviewCount = (TextView) v.findViewById(R.id.listRowReviewCount);
            rating = (RatingBar) v.findViewById(R.id.listRowRatingBar);
            directionsButton = (ImageButton) v.findViewById(R.id.listRowDirectionsButton);
        }
    }

    public static class RestroomInfo {
        protected String name;
        protected String address;
        protected String distance;
        protected int reviewCount;
        protected float rating;
        protected LatLng latLng;
        protected int id;
    }
}
