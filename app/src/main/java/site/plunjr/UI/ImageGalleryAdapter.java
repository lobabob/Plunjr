package site.plunjr.UI;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import site.plunjr.R;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ImageGalleryViewHolder> {

    private List<String> mImgUrls;

    public ImageGalleryAdapter(List<String> imgUrls) {
        mImgUrls = imgUrls;
    }

    @Override
    public ImageGalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View ImageGalleryView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.image_gallery_item, parent, false);

        return new ImageGalleryViewHolder(ImageGalleryView);
    }

    @Override
    public void onBindViewHolder(ImageGalleryViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        Picasso.with(context)
                .load(mImgUrls.get(position))
                .placeholder(R.drawable.placeholder)
                .fit()
                .centerCrop()
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return mImgUrls.size();
    }

    public static class ImageGalleryViewHolder extends RecyclerView.ViewHolder {

        protected ImageView img;

        public ImageGalleryViewHolder(View v) {
            super(v);
            img = (ImageView) v.findViewById(R.id.galleryImage);
        }
    }
}
