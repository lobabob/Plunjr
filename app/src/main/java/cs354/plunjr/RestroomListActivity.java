package cs354.plunjr;

import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RestroomListActivity extends AppCompatActivity implements OnMapReadyCallback {

    private List<Map<String, String>> restroomList = new ArrayList<>();
    private GoogleMap mMap;

    private void initRestroomList() {
        try {
            JSONArray restrooms = new PlunjrAPIClient().getRestrooms(this);

            // Create a hash map for each row's data (one hash map per row)
            for(int i = 0; i < restrooms.length(); i++) {
                JSONObject restroom = restrooms.getJSONObject(i);
                HashMap<String, String> rowData = new HashMap<>();

                Iterator<String> keys = restroom.keys();
                while(keys.hasNext()) {
                    String key = keys.next();
                    rowData.put(key, restroom.optString(key));
                }
                // Swap address and name if name is null
                if(rowData.get("name").equals("")) {
                    rowData.put("name", rowData.get("address"));
                    rowData.put("address", "");
                }
                restroomList.add(rowData);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "JSON exception while populating list", Toast.LENGTH_SHORT).show();
            Log.e("Restroom List", e.getMessage(), e);
        }
        String[] from = {"name", "address", "averageRating", "reviewCount"};
        int[] to = {R.id.listRowTitle, R.id.listRowAddress, R.id.listRowRatingBar, R.id.listRowReviewCount};

        // Bind data in each hash map to a corresponding row in the list view
        ListView restroomListView = (ListView) findViewById(R.id.restroomList);
        SimpleAdapter restroomListViewAdapter = new SimpleAdapter(this, restroomList, R.layout.restroom_list_row, from, to);
        restroomListViewAdapter.setViewBinder(new RestroomListViewBinder());
        restroomListView.setAdapter(restroomListViewAdapter);

        // Set on click listener to launch review list activity for a chosen restroom
        restroomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ReviewListActivity.class);
                intent.putExtra("restroomID", Integer.parseInt(restroomList.get(position).get("id")));
                intent.putExtra("restroomName", restroomList.get(position).get("name"));
                startActivity(intent);
            }
        });
        restroomListView.setItemsCanFocus(false);
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
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show(getFragmentManager(), "dialog");
            }
        });
        */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restroom_list);

        // Begin map initialization
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initRestroomList();
        initReviewDialog();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        // Get user location
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        Location location = locationManager.getLastKnownLocation(provider);

        // Zoom in on user's current location
        if(location != null) {
            LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15.5f));
        }
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
