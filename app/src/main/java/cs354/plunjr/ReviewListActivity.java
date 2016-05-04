package cs354.plunjr;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.whinc.widget.ratingbar.RatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReviewListActivity extends AppCompatActivity implements WriteReviewDialogFragment.WriteReviewDialogListener {

    private DateFormat parseDatePattern;
    private DateFormat formatDatePattern;
    private ReviewListAdapter mReviewListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        parseDatePattern = new SimpleDateFormat(
                getResources().getString(R.string.review_parse_date), Locale.ROOT);
        formatDatePattern = new SimpleDateFormat(
                getResources().getString(R.string.review_format_date), Locale.ROOT);

        Bundle extras = getIntent().getExtras();
        int restroomID = extras.getInt("restroomID");
        setTitle(extras.getString("restroomName"));
        final double lat = extras.getDouble("restroomLat");
        final double lng = extras.getDouble("restroomLng");

        mReviewListAdapter = new ReviewListAdapter(this, lat, lng, new ArrayList<ReviewListAdapter.ReviewItem>());

        initReviewListAdapter();
        new LoadReviewsTask().execute(restroomID);
    }

    @Override
    public void onDialogPositiveClick() {
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

    private class LoadReviewsTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            try {
//                JSONArray reviews = new JSONArray(getResources().getString(R.string.debug_review_json));
                JSONArray reviews = new PlunjrAPIClient().getReviews(getApplicationContext(), params[0]);

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
}
