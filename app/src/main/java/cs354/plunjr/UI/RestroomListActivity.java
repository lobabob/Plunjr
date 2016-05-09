package cs354.plunjr.UI;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import cs354.plunjr.Util.AddressUtil;
import cs354.plunjr.HTTP.PlunjrAPIClient;
import cs354.plunjr.Util.MapUtil;
import cs354.plunjr.R;

public class RestroomListActivity extends AppCompatActivity implements OnMapReadyCallback, WriteReviewDialogFragment.WriteReviewDialogListener {

    private static AtomicInteger mAsyncTaskCounter = new AtomicInteger(2);

    private RestroomListAdapter mRestroomListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WriteReviewDialogFragment mDialog;
    private GoogleMap mMap;
    private MapUtil mapUtil;
    private PlunjrAPIClient mPlunjrClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restroom_list);
        mPlunjrClient = new PlunjrAPIClient(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final CollapsingToolbarLayout ctl = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);

        this.mapUtil = new MapUtil(this);
        mapUtil.setupMapFragment(new AppBarLayout.OnOffsetChangedListener() {
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
                loadRestrooms();
            }
        });
        // Set up sort options spinner
        Spinner sortOptionsSpinner = (Spinner) findViewById(R.id.sort_options_spinner);
        ArrayAdapter<CharSequence> sortOptionsAdapter = ArrayAdapter.createFromResource(this, R.array.sort_options, R.layout.support_simple_spinner_dropdown_item);
        sortOptionsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sortOptionsSpinner.setAdapter(sortOptionsAdapter);
        sortOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortRestrooms();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        mDialog = new WriteReviewDialogFragment();
        loadRestrooms();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mDialog.isAdded()) {
                    mDialog.show(getFragmentManager(), "dialog");
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Center map on user's location before placing map pins
        LatLng myPosition = AddressUtil.getUserLatLng(this);
        if(myPosition != null && mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15.5f));
        }
        placeMapPins();

        // Prevent vertical scrolls on map from scrolling the Recycler view
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) findViewById(R.id.appbar).getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);
    }

    @Override
    public void onDialogPositiveClick() {
        loadRestrooms();
    }

    @Override
    public void onDialogNegativeClick() {}

    private void placeMapPins() {
        if(mAsyncTaskCounter.decrementAndGet() <= 0 && mRestroomListAdapter.getItemCount() > 0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            mMap.clear();

            BitmapDescriptor icon = mapUtil.getPinIcon();

            // Place map pins
            for(int i = 0; i < mRestroomListAdapter.getItemCount(); i++) {
                RestroomListAdapter.RestroomInfo rrInfo = mRestroomListAdapter.get(i);
                MarkerOptions marker = new MarkerOptions()
                        .position(rrInfo.latLng)
                        .title(rrInfo.name)
                        .icon(icon);
                mMap.addMarker(marker);
                builder.include(rrInfo.latLng);
            }
            builder.include(AddressUtil.getUserLatLng(this));

            try {
                CameraUpdate update = CameraUpdateFactory.newLatLngBounds(builder.build(), 10);
                mMap.animateCamera(update);
            } catch(IllegalStateException e) {
                // No points nearby, nothing too bad should happen
                Toast.makeText(this, "No nearby restrooms found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sortRestrooms() {
        String sortOption = ((Spinner) findViewById(R.id.sort_options_spinner)).getSelectedItem().toString();
        if(sortOption.equals(getString(R.string.sort_options_rating))) {
            mRestroomListAdapter.sortByRating();
        } else {
            mRestroomListAdapter.sortByCloseness();
        }
    }

    private void loadRestrooms() {
        if(mRestroomListAdapter != null) {
            mPlunjrClient.loadRestrooms(mRestroomListAdapter, new Callable() {
                @Override
                public Object call() throws Exception {
                    mSwipeRefreshLayout.setRefreshing(false);
                    sortRestrooms();
                    placeMapPins();
                    return null;
                }
            });
        } else {
            mRestroomListAdapter = new RestroomListAdapter(new ArrayList<RestroomListAdapter.RestroomInfo>());
            RecyclerView restroomListView = (RecyclerView) findViewById(R.id.restroomList);
            restroomListView.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            restroomListView.setLayoutManager(llm);
            restroomListView.setAdapter(mRestroomListAdapter);
            loadRestrooms();
        }
    }
}
