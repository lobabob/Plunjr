package cs354.plunjr;

import android.content.Context;
import android.renderscript.Double2;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Used for easily getting data from the Plunjr backend
 * The methods in this class can NOT be used on the UI thread
 * It is expected that this class will only be used in the context of an AsyncTask
 */
public class PlunjrAPIClient extends HttpClient {

    private static final String LOG_TAG = "Plunjr_Client";

    public JSONArray getRestrooms(Context context, double lat, double lng) {
        String res = getFromURL(String.format(context.getString(R.string.get_restrooms_uri), lat, lng));

        JSONArray resArr = new JSONArray();
        try {
            resArr = new JSONArray(res);
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return resArr;
    }

    public JSONArray getReviews(Context context, int restroomID) {
        String res = getFromURL(String.format(context.getString(R.string.get_reviews_uri), restroomID));

        JSONArray resArr = new JSONArray();
        try {
            resArr = new JSONArray(res);
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return resArr;
    }

    public JSONObject postReview(Context context, String address, String user, String rating,
                                String title, String description, String name, String lat, String lng) {
        JSONObject resObj = new JSONObject();

        // Transform parameters into a correctly formatted JSON string
        JSONObject obj = new JSONObject();
        try {
            obj.put("address", address);
            obj.put("user", user);
            obj.put("rating", rating);
            obj.put("title", title);
            obj.put("description", description);
            obj.put("name", name);
            obj.put("lat", Double.parseDouble(lat));
            obj.put("lng", Double.parseDouble(lng));
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        String res = postJSONToURL(obj, context.getString(R.string.post_review_uri));

        // Convert response to JSON array
        try {
            resObj = new JSONObject(res);
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return resObj;
    }

    public JSONObject postImageURL(Context context, String imgUrl, int id) {
        JSONObject resObj = null;

        // Transform parameters into a correctly formatted string
        JSONArray req = new JSONArray();
        try {
            JSONObject obj = new JSONObject();
            obj.put("imageUrl", imgUrl);
            req.put(obj);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        String res = postJSONToURL(req, String.format(context.getString(R.string.patch_photo_uri), id));

        // Convert response to JSON object
        try {
            resObj = new JSONObject(res);
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return resObj;
    }
}
