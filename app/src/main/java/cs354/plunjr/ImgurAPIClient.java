package cs354.plunjr;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImgurAPIClient extends HttpClient {

    private static final String LOG_TAG = "Imgur_Client";


    @Override
    void setGetHeaders(HttpURLConnection connection) {
        connection.setRequestProperty("Authentication", "Client-ID ceb4ee981a06089");
    }

    @Override
    void setPostHeaders(HttpURLConnection connection) {
        connection.setRequestProperty("Authentication", "Client-ID ceb4ee981a06089");
    }

    public String getImageURLFromID(Context context, String id) {
        // Make GET request to API
        String res  = "";
        String link = "";
        try {
            URL url = new URL(String.format(context.getString(R.string.imgur_get_uri), id));
            res = get(url);
        } catch(MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        // Convert response to JSON Object and get the link field
        try {
            JSONObject obj = new JSONObject(res);
            link = obj.optString("link");
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return link;
    }

    public String uploadImage(Context context, Byte[] imgData) {
        String res = "";
        JSONObject resObj = new JSONObject();

        // Transform parameters into a correctly formatted string
        JSONObject obj = new JSONObject();
        try {
            obj.put("image", imgData);
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
        return resObj.optString("data");
    }

}
