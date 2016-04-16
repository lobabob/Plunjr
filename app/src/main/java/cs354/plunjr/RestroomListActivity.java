package cs354.plunjr;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RestroomListActivity extends AppCompatActivity implements OnMapReadyCallback {

    private WriteReviewDialogFragment mDialog;
    private GoogleMap mMap;

    private void initRestroomList() {
        List<RestroomListAdapter.RestroomInfo> restroomList = new ArrayList<>();
        try {
            JSONArray restrooms = new PlunjrAPIClient().getRestrooms(this);

            for(int i = 0; i < restrooms.length(); i++) {
                JSONObject restroom = restrooms.getJSONObject(i);
                RestroomListAdapter.RestroomInfo rrInfo = new RestroomListAdapter.RestroomInfo();

                rrInfo.name = restroom.optString("name");
                rrInfo.address = restroom.optString("address");
                rrInfo.rating = (float) restroom.optDouble("averageRating");
                rrInfo.reviewCount = restroom.optInt("reviewCount");
                rrInfo.id = restroom.optInt("id");

                restroomList.add(rrInfo);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "JSON exception while populating list", Toast.LENGTH_SHORT).show();
            Log.e("Restroom List", e.getMessage(), e);
        }
        RecyclerView restroomListView = (RecyclerView) findViewById(R.id.restroomList);
        restroomListView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        restroomListView.setLayoutManager(llm);
        restroomListView.setAdapter(new RestroomListAdapter(restroomList));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restroom_list);

        // Set up toolbar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // Begin map initialization
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDialog = new WriteReviewDialogFragment();
        initRestroomList();
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15.5f));
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
        int id = item.getItemId();

        switch (id) {
            case R.id.action_review:
                mDialog.show(getFragmentManager(), "dialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
