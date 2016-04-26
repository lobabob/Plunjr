package cs354.plunjr;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressUtils {

    private AddressUtils() {}

    public static List<Address> getUserAddresses(Context context) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        String provider = locationManager.getBestProvider(new Criteria(), true);
        Location location = locationManager.getLastKnownLocation(provider);
        List<Address> addresses;
        try{
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 3);
            return addresses;
        } catch(IOException e) {
            Log.e("Dialog", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public static String formatAsString(Address address) {
        StringBuilder mSb = new StringBuilder();
        mSb.setLength(0);
        final int addressLineSize = address.getMaxAddressLineIndex();
        for (int i = 0; i < addressLineSize; i++) {
            mSb.append(address.getAddressLine(i));
            if (i != addressLineSize - 1) {
                mSb.append(", ");
            }
        }
        return mSb.toString();
    }
}
