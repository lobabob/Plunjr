package cs354.plunjr.UI;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import cs354.plunjr.R;

public class RestroomListAdapter extends RecyclerView.Adapter<RestroomListAdapter.RestroomViewHolder> {

    private List<RestroomInfo> restrooms;

    public RestroomListAdapter(List<RestroomInfo> restrooms) {
        this.restrooms = restrooms;
        setHasStableIds(true);
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
        rrHolder.distance.setText(String.format(Locale.US, "%.1f mi", rr.distance));

        if(rr.imgUrls != null && rr.imgUrls.length > 0) {
            Picasso.with(rrHolder.itemView.getContext())
                    .load(rr.imgUrls[0])
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .into(rrHolder.img);
        } else {
            Picasso.with(rrHolder.itemView.getContext())
                    .load(R.drawable.placeholder)
                    .fit()
                    .centerCrop()
                    .into(rrHolder.img);
        }

        // Set holder on click listener
        rrHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ReviewListActivity.class);
                intent.putExtra("restroomID", rr.id);
                intent.putExtra("restroomName", rr.name);
                intent.putExtra("restroomLat", rr.latLng.latitude);
                intent.putExtra("restroomLng", rr.latLng.longitude);
                intent.putExtra("imgUrls", rr.imgUrls);

                view.getContext().startActivity(intent);
            }
        });
        // Set directions button on click listener
        rrHolder.getDirections.setOnClickListener(new View.OnClickListener() {
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
    public long getItemId(int position) {
        return restrooms.get(position).id;
    }

    @Override
    public int getItemCount() {
        return restrooms.size();
    }

    public void sortByRating() {
        Collections.sort(restrooms, new RatingComparator());
        notifyDataSetChanged();
    }

    public void sortByCloseness() {
        Collections.sort(restrooms, new ClosenessComparator());
        notifyDataSetChanged();
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
        protected ImageView img;
        protected ImageButton getDirections;

        public RestroomViewHolder(View v) {
            super(v);
            name          = (TextView)    v.findViewById(R.id.listRowTitle);
            address       = (TextView)    v.findViewById(R.id.listRowAddress);
            distance      = (TextView)    v.findViewById(R.id.listRowDistance);
            reviewCount   = (TextView)    v.findViewById(R.id.listRowReviewCount);
            rating        = (RatingBar)   v.findViewById(R.id.listRowRatingBar);
            img           = (ImageView)   v.findViewById(R.id.listRowImage);
            getDirections = (ImageButton) v.findViewById(R.id.listRowDirectionsButton);
        }
    }

    public static class RestroomInfo {
        public String name;
        public String address;
        public double distance;
        public int reviewCount;
        public float rating;
        public LatLng latLng;
        public int id;
        public String[] imgUrls;
    }

    /**
     * Sorts a Collection of RestroomInfo by ascending distance from user
     */
    private class ClosenessComparator implements Comparator<RestroomInfo> {

        @Override
        public int compare(RestroomInfo lhs, RestroomInfo rhs) {
            if(lhs.distance > rhs.distance) {
                return 1;
            } else if(lhs.distance == rhs.distance) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    /**
     * Sorts a Collection of RestroomInfo by descending rating
     */
    private class RatingComparator implements Comparator<RestroomInfo> {

        @Override
        public int compare(RestroomInfo lhs, RestroomInfo rhs) {
            if(lhs.rating < rhs.rating) {
                return 1;
            } else if(lhs.rating == rhs.rating) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
