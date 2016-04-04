package cs354.plunjr;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

    private DateFormat PARSE_DATE_PATTERN = new SimpleDateFormat(
            getResources().getString(R.string.review_parse_date), Locale.ROOT);
    private DateFormat FORMAT_DATE_PATTERN = new SimpleDateFormat(
            getResources().getString(R.string.review_format_date), Locale.ROOT);

    private List<Map<String, String>> reviewList = new ArrayList<Map<String, String>>();

    ReviewListActivity self;

    private void populateReviewList(int restroomID) {
        try {
            // TODO: get JSON object through http request instead, use restroomID in request
            JSONObject res = new JSONObject(getResources().getString(R.string.debug_review_json));
            JSONArray reviews = res.optJSONArray("reviews");

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
            Toast.makeText(this, "JSON exception while populating list", Toast.LENGTH_SHORT).show();
            Log.e("Review List", e.getMessage(), e);
        }
    }

    private void initReviewListAdapter() {
        String[] from = {"name", "date", "rating", "text"};
        int[] to = {R.id.reviewerName, R.id.reviewDate, R.id.reviewRating, R.id.reviewText};

        // Bind data in each hash map to a corresponding row in the list view
        ListView reviewListView = (ListView) findViewById(R.id.reviewList);

        SimpleAdapter reviewListViewAdapter = new SimpleAdapter(this, reviewList, R.layout.review_item, from, to);
        reviewListViewAdapter.setViewBinder(new ReviewListViewBinder());
        reviewListView.setAdapter(reviewListViewAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        int restroomID = extras.getInt("restroomID");

        populateReviewList(restroomID);
        initReviewListAdapter();

        self = this;

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
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
                        Date date = PARSE_DATE_PATTERN.parse(textRepresentation);
                        textRepresentation = FORMAT_DATE_PATTERN.format(date);
                    } catch (ParseException e) {
                        Toast.makeText(self, "Exception while parsing/formatting date", Toast.LENGTH_SHORT).show();
                        Log.e("Date parsing/formatting", e.getMessage(), e);

                        textRepresentation = "Invalid Date";
                    }
                }

                text.setText(textRepresentation);
            }

            return true;
        }
    }
}
