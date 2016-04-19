package cs354.plunjr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

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
    public void onBindViewHolder(RestroomViewHolder rrHolder, int position) {
        final RestroomInfo rr = restrooms.get(position);

        // Bind data to view
        rrHolder.name.setText(rr.name);
        rrHolder.address.setText(rr.address);
        rrHolder.rating.setRating(rr.rating);
        rrHolder.reviewCount.setText(
                String.format(rrHolder.reviewCount.getContext().getString(R.string.review_count_format), rr.reviewCount)
        );
        // TODO: calculate distance and bind it here
        rrHolder.distance.setText("DIST");

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
    }

    @Override
    public int getItemCount() {
        return restrooms.size();
    }

    public static class RestroomViewHolder extends RecyclerView.ViewHolder {

        protected TextView name;
        protected TextView address;
        protected TextView distance;
        protected TextView reviewCount;
        protected RatingBar rating;

        public RestroomViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.listRowTitle);
            address = (TextView) v.findViewById(R.id.listRowAddress);
            distance = (TextView) v.findViewById(R.id.listRowDistance);
            reviewCount = (TextView) v.findViewById(R.id.listRowReviewCount);
            rating = (RatingBar) v.findViewById(R.id.listRowRatingBar);
        }
    }

    public static class RestroomInfo {
        protected String name;
        protected String address;
        protected String distance;
        protected int reviewCount;
        protected float rating;
        protected int id;
    }

    public static class Divider extends RecyclerView.ItemDecoration {

        private final int mHeight;

        public Divider(Context context, int height) {
            // Convert height value from dp to px
            height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, context.getResources().getDisplayMetrics());
            this.mHeight = height;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = mHeight;
        }

    }
}
