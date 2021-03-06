package site.plunjr.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import site.plunjr.R;

public class ReviewListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private WriteReviewDialogFragment mDialog;

    private List<ReviewItem> reviews;
    private final double lat;
    private final double lng;
    private final Activity context;

    public ReviewListAdapter(Activity context, double lat, double lng, ReviewHeader header, List<ReviewItem> reviews) {
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
        } else {
            return new ReviewHeaderViewHolder(inflater.inflate(R.layout.review_header, parent, false), context);
        }
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

            item.title.setText(header.title);

            if (header.imgUrls.length == 0) {
                item.imgGallery.setVisibility(View.GONE);
            } else {
                item.imgGallery.setVisibility(View.VISIBLE);
            }
            RecyclerView imgGallery = item.imgGallery;
            imgGallery.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(imgGallery.getContext());
            llm.setOrientation(LinearLayoutManager.HORIZONTAL);
            imgGallery.setLayoutManager(llm);
            List<String> list = new ArrayList<>();
            ImageGalleryAdapter a = new ImageGalleryAdapter(list);
            imgGallery.setAdapter(a);

            for(int i = 0; i < header.imgUrls.length; i++) {
                list.add(header.imgUrls[i]);
            }
            item.rb.setOnRatingChangeListener(new com.whinc.widget.ratingbar.RatingBar.OnRatingChangeListener() {
                @Override
                public void onChange(com.whinc.widget.ratingbar.RatingBar ratingBar, int preCount, int curCount) {
                    if (curCount > 0 && !mDialog.isAdded()) {
                        Bundle args = new Bundle();
                        args.putInt("rating", curCount);
                        args.putDouble("lat", lat);
                        args.putDouble("lng", lng);

                        mDialog.setArguments(args);
                        mDialog.show(context.getFragmentManager(), "dialog");
                    }
                }
            });
            // Set up "Add Photo" button
            item.addPhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    context.startActivityForResult(Intent.createChooser(intent, "Select a restroom photo"), 0);
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
        protected TextView title;
        protected RecyclerView imgGallery;
        protected ImageButton addPhotoButton;

        public ReviewHeaderViewHolder(View v, Activity context) {
            super(v);
            this.header = (LinearLayout) v.findViewById(R.id.reviewListHeader);
            this.title = (TextView) v.findViewById(R.id.title);
            this.rb = (com.whinc.widget.ratingbar.RatingBar)
                    header.findViewById(R.id.newRating);

            this.imgGallery = (RecyclerView) v.findViewById(R.id.imageGallery);
            this.imgGallery.setHasFixedSize(false);
            this.addPhotoButton = (ImageButton) v.findViewById(R.id.addPhotoButton);

            LinearLayoutManager llm = new LinearLayoutManager(context);
            llm.setOrientation(LinearLayoutManager.HORIZONTAL);
            imgGallery.setLayoutManager(llm);
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
        public String user;
        public String date;
        public float rating;
        public String title;
        public String description;
        public int id;
    }

    public static class ReviewHeader implements ReviewItem {
        public String title;
        public String[] imgUrls;
    }
}
