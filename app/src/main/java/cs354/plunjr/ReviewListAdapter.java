package cs354.plunjr;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewViewHolder> {
    private List<ReviewInfo> reviews;

    public ReviewListAdapter(List<ReviewListAdapter.ReviewInfo> reviews) {
        this.reviews = reviews;
        setHasStableIds(true);
    }

    public boolean add(ReviewInfo r) {
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

    @Override
    public long getItemId(int position) {
        return reviews.get(position).id;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View restroomView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.review_item, parent, false);

        return new ReviewViewHolder(restroomView);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        final ReviewInfo review = reviews.get(position);

        setTextAndVisibility(holder.user, review.user);
        setTextAndVisibility(holder.date, review.date);
        setTextAndVisibility(holder.title, review.title);
        setTextAndVisibility(holder.description, review.description);
        holder.rating.setRating(review.rating);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {

        protected TextView user;
        protected TextView date;
        protected RatingBar rating;
        protected TextView title;
        protected TextView description;

        public ReviewViewHolder(View v) {
            super(v);
            user = (TextView) v.findViewById(R.id.reviewerName);
            date = (TextView) v.findViewById(R.id.reviewDate);
            rating = (RatingBar) v.findViewById(R.id.reviewRating);
            title = (TextView) v.findViewById(R.id.reviewTitle);
            description = (TextView) v.findViewById(R.id.reviewDescription);
        }
    }

    public static class ReviewInfo {
        protected String user;
        protected String date;
        protected float rating;
        protected String title;
        protected String description;
        protected int id;
    }
}
