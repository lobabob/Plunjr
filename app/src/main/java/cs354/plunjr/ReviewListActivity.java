package cs354.plunjr;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReviewListActivity extends AppCompatActivity {

    private DateFormat parseDatePattern;
    private DateFormat formatDatePattern;
    private List<Map<String, String>> reviewList = new ArrayList<>();
    private WriteReviewDialogFragment mDialog;

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

        mDialog = new WriteReviewDialogFragment();
        new LoadReviewsTask().execute(restroomID);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.show(getFragmentManager(), "dialog");
            }
        });
    }

    private void initReviewListAdapter() {
        String[] from = {"user", "date", "rating", "title", "description"};
        int[] to = {R.id.reviewerName, R.id.reviewDate, R.id.reviewRating, R.id.reviewTitle, R.id.reviewDescription};

        // Bind data in each hash map to a corresponding row in the list view
        ListView reviewListView = (ListView) findViewById(R.id.reviewList);

        SimpleAdapter reviewListViewAdapter = new SimpleAdapter(this, reviewList, R.layout.review_item, from, to);
        reviewListViewAdapter.setViewBinder(new ReviewListViewBinder());
        reviewListView.setAdapter(reviewListViewAdapter);
    }

    private class ReviewListViewBinder implements SimpleAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if(view.getId() == R.id.reviewRating) {
                RatingBar bar = (RatingBar) view;
                bar.setRating(Float.parseFloat(textRepresentation));
            } else {
                TextView text = (TextView) view;

                if(view.getId() == R.id.reviewDate) {
                    try {
                        Date date = parseDatePattern.parse(textRepresentation);
                        textRepresentation = formatDatePattern.format(date);
                    } catch (ParseException e) {
                        Log.e("Date parsing/formatting", e.getMessage(), e);

                        textRepresentation = "Invalid Date";
                    }
                }
                if (textRepresentation.equals("")) {
                    view.setVisibility(View.GONE);
                }
                text.setText(textRepresentation);
            }
            return true;
        }
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
                    HashMap<String, String> rowData = new HashMap<>();

                    Iterator<String> keys = review.keys();
                    while(keys.hasNext()) {
                        String key = keys.next();
                        rowData.put(key, review.optString(key));
                    }
                    reviewList.add(rowData);
                }
            } catch(JSONException e) {
                Log.e("Review List", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            initReviewListAdapter();
        }
    }
}
