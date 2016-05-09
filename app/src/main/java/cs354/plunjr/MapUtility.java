package cs354.plunjr;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public class MapUtility {

    private static final double MAP_FRAGMENT_VH = 0.4;
    private static final int MAP_MARKER_SIZE_DP = 60;
    private AppCompatActivity context;

    MapUtility(AppCompatActivity context) {
        this.context = context;
    }

    public void setupMapFragment(AppBarLayout.OnOffsetChangedListener appBarListener) {
        // Begin map initialization
        SupportMapFragment mapFragment = (SupportMapFragment) context.getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) context);

        // Recalculate map height to be a percentage of the viewport height
        ViewGroup.LayoutParams mapParams = mapFragment.getView().getLayoutParams();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mapParams.height = (int) (metrics.heightPixels * MAP_FRAGMENT_VH);
        mapFragment.getView().setLayoutParams(mapParams);

        // Set up toolbar
        final Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        context.setSupportActionBar((Toolbar) context.findViewById(R.id.toolbar));
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.colorWhite));

        final CollapsingToolbarLayout ctl = (CollapsingToolbarLayout) context.findViewById(R.id.collapsingToolbar);
        ctl.setTitleEnabled(false);

        // Fade toolbar in/out on scroll
        final AppBarLayout appBar = (AppBarLayout) context.findViewById(R.id.appbar);
        appBar.addOnOffsetChangedListener(appBarListener);
    }

    public BitmapDescriptor getPinIcon() {
        // Scale plunger icon
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAP_MARKER_SIZE_DP, context.getResources().getDisplayMetrics());
        Bitmap icon = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.plunger, null)).getBitmap();
        Bitmap scaledIcon = Bitmap.createScaledBitmap(icon, px, px, false);

        return BitmapDescriptorFactory.fromBitmap(scaledIcon);
    }
}
