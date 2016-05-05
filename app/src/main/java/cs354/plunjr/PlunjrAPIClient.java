package cs354.plunjr;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Used for easily getting data from the Plunjr backend
 * The methods in this class can NOT be used on the UI thread
 * It is expected that this class will only be used in the context of an AsyncTask
 */
public class PlunjrAPIClient extends HttpClient {

    private static final String LOG_TAG = "Plunjr_Client";

    public JSONArray getRestrooms(Context context, double lat, double lng) {
        // Make GET request to API
        String res = "";
        try {
            URL url = new URL(String.format(context.getString(R.string.get_restrooms_uri), lat, lng));
            res = get(url);
        } catch(MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        // Convert response to JSON array
        JSONArray arr = new JSONArray();
        try {
            arr = new JSONArray(res);
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return arr;
    }

    public JSONArray getReviews(Context context, int restroomID) {
        String res = "";
        try {
            URL url = new URL(String.format(context.getString(R.string.get_reviews_uri), restroomID));
            res = get(url);
        } catch(Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        // Convert response to JSON array
        JSONArray arr = new JSONArray();
        try {
            arr = new JSONArray(res);
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return arr;
    }

    public JSONObject postReview(Context context, String address, String user, String rating,
                                String title, String description, String name, String lat, String lng) {
        String res = "";
        JSONObject resObj = new JSONObject();

        // Transform parameters into a correctly formatted string
        JSONObject obj = new JSONObject();
        try {
            obj.put("address", address);
            obj.put("user", user);
            obj.put("rating", rating);
            obj.put("title", title);
            obj.put("description", description);
            obj.put("name", name);
            obj.put("lat", Float.parseFloat(lat));
            obj.put("lng", Float.parseFloat(lng));
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        String postData = obj.toString();

        // Make POST request to API
        try {
            URL url = new URL(context.getString(R.string.post_review_uri));
            res = post(url, postData);
        } catch(MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        // Convert response to JSON array
        try {
            resObj = new JSONObject(res);
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return resObj;
    }
}
