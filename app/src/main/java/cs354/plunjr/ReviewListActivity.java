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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReviewListActivity extends AppCompatActivity implements OnMapReadyCallback, WriteReviewDialogFragment.WriteReviewDialogListener {

    private static final double MAP_FRAGMENT_VH = 0.4;
    private static final int MAP_MARKER_SIZE_DP = 60;
    private static final int SELECT_PHOTO = 0;

    private DateFormat parseDatePattern;
    private DateFormat formatDatePattern;
    private ReviewListAdapter mReviewListAdapter;
    private GoogleMap mMap;
    private MapUtility mapUtil;
    private int restroomID;
    private double lat;
    private double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);
        setTitle("");

        parseDatePattern = new SimpleDateFormat(
                getResources().getString(R.string.review_parse_date), Locale.ROOT);
        formatDatePattern = new SimpleDateFormat(
                getResources().getString(R.string.review_format_date), Locale.ROOT);

        Bundle extras = getIntent().getExtras();
        this.restroomID = extras.getInt("restroomID");
        String restroomName = extras.getString("restroomName");
        this.lat = extras.getDouble("restroomLat");
        this.lng = extras.getDouble("restroomLng");
        String[] imgUrls = extras.getStringArray("imgUrls");

        if (imgUrls == null) {
            imgUrls = new String[0];
        }

        ReviewListAdapter.ReviewHeader header = new ReviewListAdapter.ReviewHeader();
        header.title = restroomName;
        header.imgUrls = imgUrls;
        mReviewListAdapter = new ReviewListAdapter(this, lat, lng, header, new ArrayList<ReviewListAdapter.ReviewItem>());

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
        loadRestrooms();
    }

    private void loadRestrooms() {
        new LoadReviewsTask().execute(this.restroomID);
    }

    @Override
    public void onDialogPositiveClick() {
        loadRestrooms();
        onDialogNegativeClick();
    }

    @Override
    public void onDialogNegativeClick() {
        RecyclerView list = (RecyclerView) findViewById(R.id.reviewList);
        LinearLayout header = (LinearLayout) list.findViewById(R.id.reviewListHeader);

        ((com.whinc.widget.ratingbar.RatingBar) header.findViewById(R.id.newRating)).setCount(0);
    }

    private void initReviewListAdapter() {
        // Bind data in each hash map to a corresponding row in the list view
        RecyclerView reviewListView = (RecyclerView) findViewById(R.id.reviewList);

        reviewListView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        reviewListView.setLayoutManager(llm);
        reviewListView.setAdapter(mReviewListAdapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);

        // Center map on user's location before placing map pins
        LatLng pos = mapUtil.getUserLatLng();
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
        builder.include(mapUtil.getUserLatLng());

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

    private class LoadReviewsTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            try {
//                JSONArray reviews = new JSONArray(getResources().getString(R.string.debug_review_json));
                JSONArray reviews = new PlunjrAPIClient().getReviews(getApplicationContext(), params[0]);

                mReviewListAdapter.clear();

                // Create a hash map for each row's data (one hash map per row)
                for(int i = 0; i < reviews.length(); i++) {
                    JSONObject review = reviews.getJSONObject(i);
                    ReviewListAdapter.ReviewInfo rowData = new ReviewListAdapter.ReviewInfo();

                    rowData.user = review.optString("user");
                    rowData.rating = (float) review.optDouble("rating");
                    rowData.title = review.optString("title");
                    rowData.description = review.optString("description");
                    rowData.id = review.optInt("id");

                    rowData.date = review.optString("date");

                    try {
                        Date date = parseDatePattern.parse(rowData.date);
                        rowData.date = formatDatePattern.format(date);
                    } catch (ParseException e) {
                        Log.e("Date parsing/formatting", e.getMessage(), e);
                        rowData.date = "Invalid Date";
                    }

                    mReviewListAdapter.add(rowData);
                }
            } catch(JSONException e) {
                Log.e("Review List", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mReviewListAdapter.notifyDataSetChanged();
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
            new PlunjrAPIClient().addImages(mContext, params[0], restroomID);
            return null;
        }
    }
}
