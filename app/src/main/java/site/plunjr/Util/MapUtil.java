package site.plunjr.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import site.plunjr.R;

public class MapUtil {

    private static final double MAP_FRAGMENT_VH = 0.4;
    private static final int MAP_MARKER_SIZE_DP = 60;

    private MapUtil() {}

    public static void setupMapFragment(final AppCompatActivity context, boolean doTitleFade) {
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
        if(doTitleFade) {
            final AppBarLayout appBar = (AppBarLayout) context.findViewById(R.id.appbar);
            appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                private int mStatusBarHeight;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    float curHeight = ctl.getHeight() + verticalOffset - mStatusBarHeight;
                    float scrimThreshold = 2 * ViewCompat.getMinimumHeight(ctl);

                    // Calculate status bar height if frame insets are supported (Kitkat+)
                    if(Build.VERSION.SDK_INT >= 19 && mStatusBarHeight <= 0) {
                        Rect displayRect = new Rect();
                        context.getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRect);
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
        }
    }

    public static BitmapDescriptor getPinIcon(Context context) {
        // Scale plunger icon
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAP_MARKER_SIZE_DP, context.getResources().getDisplayMetrics());
        Bitmap icon = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.plunger, null)).getBitmap();
        Bitmap scaledIcon = Bitmap.createScaledBitmap(icon, px, px, false);

        return BitmapDescriptorFactory.fromBitmap(scaledIcon);
    }
}
