package cs354.plunjr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;

public class ReviewListActivity extends AppCompatActivity implements OnMapReadyCallback, WriteReviewDialogFragment.WriteReviewDialogListener {

    private static final int SELECT_PHOTO = 0;

    private ReviewListAdapter mReviewListAdapter;
    private GoogleMap mMap;
    private MapUtility mapUtil;
    private int restroomID;
    private double lat;
    private double lng;
    private PlunjrAPIClient mPlunjrClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);
        setTitle("");
        mPlunjrClient = new PlunjrAPIClient(this);

        Bundle extras = getIntent().getExtras();
        this.restroomID     = extras.getInt("restroomID");
        String restroomName = extras.getString("restroomName");
        this.lat            = extras.getDouble("restroomLat");
        this.lng            = extras.getDouble("restroomLng");
        String[] imgUrls    = extras.getStringArray("imgUrls");

        if (imgUrls == null) {
            imgUrls = new String[0];
        }
        ReviewListAdapter.ReviewHeader headerInfo = new ReviewListAdapter.ReviewHeader();
        headerInfo.title   = restroomName;
        headerInfo.imgUrls = imgUrls;
        mReviewListAdapter = new ReviewListAdapter(this, lat, lng, headerInfo, new ArrayList<ReviewListAdapter.ReviewItem>());

        this.mapUtil = new MapUtility(this);
        mapUtil.setupMapFragment(new AppBarLayout.OnOffsetChangedListener() {
            private int mStatusBarHeight;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // Calculate status bar height if frame insets are supported (Kitkat+)
                if(Build.VERSION.SDK_INT >= 19 && mStatusBarHeight <= 0) {
                    Rect displayRect = new Rect();
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRect);
                    mStatusBarHeight = displayRect.top;
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initReviewListAdapter();
        loadReviews();
    }

    @Override
    public void onDialogPositiveClick() {
        loadReviews();
        onDialogNegativeClick();
    }

    @Override
    public void onDialogNegativeClick() {
        RecyclerView list = (RecyclerView) findViewById(R.id.reviewList);
        LinearLayout headerView = (LinearLayout) list.findViewById(R.id.reviewListHeader);

        ((com.whinc.widget.ratingbar.RatingBar) headerView.findViewById(R.id.newRating)).setCount(0);
    }

    private void initReviewListAdapter() {
        RecyclerView reviewListView = (RecyclerView) findViewById(R.id.reviewList);
        reviewListView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        reviewListView.setLayoutManager(llm);
        reviewListView.setAdapter(mReviewListAdapter);
    }

    private void loadReviews() {
        mPlunjrClient.loadReviews(mReviewListAdapter, restroomID);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);

        // Center map on user's location before placing map pins
        LatLng pos = AddressUtils.getUserLatLng(this);
        if(pos != null && mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15.5f));
        }

        placeMapPin(new LatLng(lat, lng));
    }

    private void placeMapPin(LatLng pos) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        mMap.clear();

        BitmapDescriptor icon = mapUtil.getPinIcon();

        MarkerOptions marker = new MarkerOptions()
                .position(pos)
                .title(getTitle().toString())
                .icon(icon);
        mMap.addMarker(marker);
        builder.include(pos);
        builder.include(AddressUtils.getUserLatLng(this));

        try {
            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);
            mMap.animateCamera(update);
        } catch(IllegalStateException e) {
            // Location of point not found, should never happen!
            Log.e("LATLNG", "Cannot find latitude/longitude!");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PHOTO) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                    bitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, false);
                    new UploadImageToImgurTask(this).execute(bitmap);
                } catch(IOException e) {
                    Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    Log.e("IMGUR UPLOAD", e.getMessage(), e);
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Do nothing
            }
        }
    }

    private class UploadImageToImgurTask extends AsyncTask<Bitmap, Void, String> {

        private Context mContext;

        public UploadImageToImgurTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            return new ImgurAPIClient(mContext).uploadImage(params[0]);
        }

        @Override
        protected void onPostExecute(String res) {
            if(res != null) {
                Toast.makeText(mContext, "Upload Succeeded!", Toast.LENGTH_SHORT).show();
                new AddImageUrlsToRestroomTask(mContext).execute(res);
            } else {
                Toast.makeText(mContext, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AddImageUrlsToRestroomTask extends AsyncTask<String, Void, Void> {

        private Context mContext;

        public AddImageUrlsToRestroomTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(String... params) {
            new PlunjrAPIClient(mContext).addImages(params[0], restroomID);
            return null;
        }
    }
}
