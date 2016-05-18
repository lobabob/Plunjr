package site.plunjr.Util;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressUtil {

    private AddressUtil() {}

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

    public static LatLng getAddressLatLng(Context context, String addressString) {
        Geocoder coder = new Geocoder(context);
        try {
            List<Address> addresses = coder.getFromLocationName(addressString, 1);
            if(addresses.size() > 0) {
                return new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
            }
        } catch (IOException e) {
            Log.e("Address Utils", e.getMessage(), e);
        }
        return null;
    }

    public static String getAddressFeatureName(Context context, String addressString) {
        Geocoder coder = new Geocoder(context);
        try {
            List<Address> addresses = coder.getFromLocationName(addressString, 1);
            if(addresses.size() > 0) {
                return addresses.get(0).getFeatureName();
            }
        } catch (IOException e) {
            Log.e("Address Utils", e.getMessage(), e);
        }
        return null;
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

    public static boolean isAddressValid(Context context, String addressString) {
        if(addressString.length() <= 0) {
            return false;
        }
        boolean valid = false;

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 5);
            for (Address a : addresses) {
                if (formatAsString(a).equals(addressString)) {
                    valid = true;
                    break;
                }
            }
        } catch(IOException e) {

        }
        return valid;
    }

    public static LatLng getUserLatLng(Context context) {
        LatLng myPosition = null;

        // Get user location
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        Location location = locationManager.getLastKnownLocation(provider);

        if(location != null) {
            myPosition = new LatLng(location.getLatitude(), location.getLongitude());
        }
        return myPosition;
    }
}
