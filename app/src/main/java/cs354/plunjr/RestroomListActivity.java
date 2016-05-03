package cs354.plunjr;

import android.content.Context;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RestroomListActivity extends AppCompatActivity implements OnMapReadyCallback, WriteReviewDialogFragment.WriteReviewDialogListener {

    private RestroomListAdapter mRestroomListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WriteReviewDialogFragment mDialog;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restroom_list);

        // Set up toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorWhite));

        final CollapsingToolbarLayout ctl = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        ctl.setTitleEnabled(false);

        // Fade toolbar in/out on scroll
        final AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);
        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            private int mStatusBarHeight;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float curHeight = ctl.getHeight() + verticalOffset - mStatusBarHeight;
                float scrimThreshold = 2 * ViewCompat.getMinimumHeight(ctl);

                // Calculate status bar height if frame insets are supported (Kitkat+)
                if(Build.VERSION.SDK_INT >= 19 && mStatusBarHeight <= 0) {
                    Rect displayRect = new Rect();
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRect);
                    mStatusBarHeight = displayRect.top;
                }
                // Start fading from alpha=0 at the scrim threshold, reach alpha=1 at min height
                if(curHeight < scrimThreshold) {
                    toolbar.setAlpha(2 * (1 - curHeight / scrimThreshold));
                } else {
                    toolbar.setAlpha(0);
                }
            }
        });
        // Refresh restroom list on swipe gesture
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.restroomListSwipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRestrooms();
            }
        });
        // Begin map initialization
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDialog = new WriteReviewDialogFragment();
        mRestroomListAdapter = new RestroomListAdapter(new ArrayList<RestroomListAdapter.RestroomInfo>());
        initRestroomList();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        centerMapCamera();
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

    @Override
    public void onDialogPositiveClick() {
        refreshRestrooms();
    }

    private LatLng getUserLatLng() {
        LatLng myPosition = null;

        // Get user location
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        Location location = locationManager.getLastKnownLocation(provider);

        // Zoom in on user's current location
        if(location != null) {
            myPosition = new LatLng(location.getLatitude(), location.getLongitude());
        }
        return myPosition;
    }

    private void centerMapCamera() {
        LatLng myPosition = getUserLatLng();
        if(myPosition != null && mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15.5f));
        }
    }

    private void initRestroomList() {
        RecyclerView restroomListView = (RecyclerView) findViewById(R.id.restroomList);
        restroomListView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        restroomListView.setLayoutManager(llm);
        restroomListView.setAdapter(mRestroomListAdapter);
        refreshRestrooms();
    }

    private void refreshRestrooms() {
        new LoadRestroomsTask().execute(this);
    }

    /**
     * Used by the RestroomListActivity to load the restroom list on a separate thread
     */
    private class LoadRestroomsTask extends AsyncTask<Context, Void, Void> {

        @Override
        protected Void doInBackground(Context... params) {
            try {
                mRestroomListAdapter.clear();
                LatLng myPosition = getUserLatLng();
                if(myPosition != null) {
                    JSONArray restrooms = new PlunjrAPIClient().getRestrooms(params[0], myPosition.latitude, myPosition.longitude);

                    for (int i = 0; i < restrooms.length(); i++) {
                        JSONObject restroom = restrooms.getJSONObject(i);
                        RestroomListAdapter.RestroomInfo rrInfo = new RestroomListAdapter.RestroomInfo();

                        rrInfo.name = restroom.optString("name");
                        rrInfo.address = restroom.optString("address");
                        rrInfo.rating = (float) restroom.optDouble("averageRating");
                        rrInfo.reviewCount = restroom.optInt("reviewCount");
                        rrInfo.id = restroom.optInt("id");

                        mRestroomListAdapter.add(rrInfo);
                    }
                }
            } catch (JSONException e) {
                Log.e("Restroom List", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mSwipeRefreshLayout.setRefreshing(false);
            mRestroomListAdapter.notifyDataSetChanged();
        }
    }
}
