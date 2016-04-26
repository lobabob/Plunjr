package cs354.plunjr;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Used for easily getting data from the Plunjr backend
 * The methods in this class can NOT be used on the UI thread
 * It is expected that this class will only be used in the context of an AsyncTask
 */
public class PlunjrAPIClient {

    private static final String LOG_TAG = "API Client";

    public JSONArray getRestrooms(Context context) {
        // Make GET request to API
        String res = "";
        try {
            URL url = new URL(context.getString(R.string.get_restrooms_uri));
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
                                String title, String description) {
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
            obj.put("name", "");
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

    private String get(URL url) {
        String res = "[]";
        try {
            // Open connection and prepare to receive response
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder build = new StringBuilder();
            String line;

            // Read response into buffer
            while((line = reader.readLine()) != null) {
                build.append(line);
            }
            reader.close();
            connection.disconnect();
            res = build.toString();
        } catch(IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return res;
    }

    private String post(URL url, String postData) {
        String res = "[]";
        try {
            // Open connection and prepare to send postData
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length()));
            connection.setUseCaches(false);

            // Send postData
            Writer wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            wr.write(postData);
            wr.close();

            // Receive Response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder build = new StringBuilder();
            String line;

            // Read response into buffer
            while ((line = reader.readLine()) != null) {
                build.append(line);
            }
            reader.close();
            connection.disconnect();
            res = build.toString();
        } catch(IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return res;
    }
}
