package cs354.plunjr;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RatingBar;

import java.util.List;

public class ReviewListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private WriteReviewDialogFragment mDialog;

    private List<ReviewItem> reviews;
    private final double lat;
    private final double lng;
    private final Activity context;

    public ReviewListAdapter(Activity context, double lat, double lng, List<ReviewListAdapter.ReviewItem> reviews) {
        this(context, lat, lng, reviews, new ReviewHeader());
    }

    public ReviewListAdapter(Activity context, double lat, double lng, List<ReviewListAdapter.ReviewItem> reviews, ReviewHeader header) {
        reviews.add(0, header);
        this.context = context;
        this.reviews = reviews;
        this.lat = lat;
        this.lng = lng;
        mDialog = new WriteReviewDialogFragment();
        setHasStableIds(true);
    }

    public boolean add(ReviewItem r) {
        return reviews.add(r);
    }

    public void setTextAndVisibility(TextView v, String text) {
        if (text.equals("")) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }

        v.setText(text);
    }

    public void clear() {
        reviews.subList(1, reviews.size()).clear();
    }

    @Override
    public long getItemId(int position) {
        return reviews.get(position).id;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_ITEM) {
            return new ReviewItemViewHolder(inflater.inflate(R.layout.review_item, parent, false));
        }

        return new ReviewHeaderViewHolder(inflater.inflate(R.layout.review_header, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ReviewItemViewHolder) {
            ReviewItemViewHolder item = (ReviewItemViewHolder) holder;
            final ReviewInfo review = (ReviewInfo) reviews.get(position);

            setTextAndVisibility(item.user, review.user);
            setTextAndVisibility(item.date, review.date);
            setTextAndVisibility(item.title, review.title);
            setTextAndVisibility(item.description, review.description);
            item.rating.setRating(review.rating);
        } else if (holder instanceof ReviewHeaderViewHolder) {
            ReviewHeaderViewHolder item = (ReviewHeaderViewHolder) holder;
            final ReviewHeader header = (ReviewHeader) reviews.get(position);

            item.rb.setOnRatingChangeListener(new com.whinc.widget.ratingbar.RatingBar.OnRatingChangeListener() {
                @Override
                public void onChange(com.whinc.widget.ratingbar.RatingBar ratingBar, int preCount, int curCount) {
                    if (curCount > 0) {
                        Bundle args = new Bundle();
                        args.putInt("rating", curCount);
                        args.putDouble("lat", lat);
                        args.putDouble("lng", lng);

                        mDialog.setArguments(args);
                        mDialog.show(context.getFragmentManager(), "dialog");
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewHeaderViewHolder extends RecyclerView.ViewHolder {

        protected LinearLayout header;
        protected com.whinc.widget.ratingbar.RatingBar rb;

        public ReviewHeaderViewHolder(View v) {
            super(v);
            this.header = (LinearLayout) v.findViewById(R.id.reviewListHeader);
            this.rb = (com.whinc.widget.ratingbar.RatingBar)
                    header.findViewById(R.id.newRating);
        }
    }

    public static class ReviewItemViewHolder extends RecyclerView.ViewHolder {

        protected TextView user;
        protected TextView date;
        protected RatingBar rating;
        protected TextView title;
        protected TextView description;

        public ReviewItemViewHolder(View v) {
            super(v);
            user = (TextView) v.findViewById(R.id.reviewerName);
            date = (TextView) v.findViewById(R.id.reviewDate);
            rating = (RatingBar) v.findViewById(R.id.reviewRating);
            title = (TextView) v.findViewById(R.id.reviewTitle);
            description = (TextView) v.findViewById(R.id.reviewDescription);
        }
    }

    public interface ReviewItem {
        int id = -1;
    }

    public static class ReviewInfo implements ReviewItem {
        protected String user;
        protected String date;
        protected float rating;
        protected String title;
        protected String description;
        protected int id;
    }

    public static class ReviewHeader implements ReviewItem {
    }
}
