package cs354.plunjr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RestroomListActivity extends AppCompatActivity {

    private List<Map<String, String>> restroomList = new ArrayList<Map<String, String>>();

    private void initRestroomList() {
        try {
            // TODO: get JSON object through http request instead
            JSONObject res = new JSONObject(getResources().getString(R.string.debug_json));
            JSONArray restrooms = res.optJSONArray("restrooms");

            // Create a hash map for each row's data (one hash map per row)
            for(int i = 0; i < restrooms.length(); i++) {
                JSONObject restroom = restrooms.getJSONObject(i);
                HashMap<String, String> rowData = new HashMap<>();

                Iterator<String> keys = restroom.keys();
                while(keys.hasNext()) {
                    String key = keys.next();
                    rowData.put(key, restroom.optString(key));
                }
                restroomList.add(rowData);
            }
        } catch(JSONException e) {
            Toast.makeText(this, "JSON exception while populating list", Toast.LENGTH_SHORT);
            Log.e("Restroom List", e.getMessage(), e);
        }
        String[] from = {"name", "address", "rating", "ratingCount"};
        int[] to = {R.id.listRowTitle, R.id.listRowAddress, R.id.listRowRatingBar, R.id.listRowReviewCount};

        // Bind data in each hash map to a corresponding row in the list view
        ListView restroomListView = (ListView) findViewById(R.id.restroomList);
        SimpleAdapter restroomListViewAdapter = new SimpleAdapter(this, restroomList, R.layout.restroom_list_row, from, to);
        restroomListViewAdapter.setViewBinder(new RestroomListViewBinder());
        restroomListView.setAdapter(restroomListViewAdapter);
    }

    // TODO: choose either action bar or floating action button, not both
    private void initReviewDialog() {
        final WriteReviewDialogFragment dialog = new WriteReviewDialogFragment();

        // Action bar button
        ImageButton writeReviewButton = (ImageButton) findViewById(R.id.write_review_button);
        writeReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show(getFragmentManager(), "dialog");
            }
        });
        // Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show(getFragmentManager(), "dialog");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restroom_list);

        initRestroomList();
        initReviewDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_restroom_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class RestroomListViewBinder implements SimpleAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if(view.getId() == R.id.listRowRatingBar) {
                RatingBar bar = (RatingBar) view;
                bar.setRating(Float.parseFloat(textRepresentation));
            } else if(view.getId() == R.id.listRowDistance) {
                // TODO: calculate distance from user and bind value to view
            } else {
                TextView text = (TextView) view;

                if(view.getId() == R.id.listRowReviewCount) {
                    textRepresentation = String.format(getResources().getString(R.string.review_count_format), textRepresentation);
                }
                text.setText(textRepresentation);
            }
            return true;
        }
    }
}
