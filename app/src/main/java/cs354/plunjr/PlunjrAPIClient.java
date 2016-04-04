package cs354.plunjr;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PlunjrAPIClient {

    private static final String LOG_TAG = "API Client";

    public JSONArray getRestrooms(Context context) {
        // Make GET request to API
        String res = "";
        try {
            res = new PlunjrGet().execute(context.getString(R.string.get_restrooms_uri)).get();
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

    public JSONArray getReviews(Context context, int restroomID) {
        String res = "";
        try {
            res = new PlunjrGet().execute(String.format(context.getString(R.string.get_reviews_uri), restroomID)).get();
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

    public JSONArray postReview(Context context, String address, String user, String rating,
                                String title, String description) {
        String res = "";
        JSONArray arr = new JSONArray();
        PlunjrPost post = new PlunjrPost();

        // Transform parameters into a correctly formatted string
        String postData = post.combineParams(
                new String[]{"address", address},
                new String[]{"user", user},
                new String[]{"rating", rating},
                new String[]{"title", title},
                new String[]{"description", description});

        // Make POST request to API
        try {
            res = post.execute(context.getString(R.string.post_review_uri), postData).get();
        } catch(Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        // Convert response to JSON array
        try {
            arr = new JSONArray(res);
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return arr;
    }

    private class PlunjrGet extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            try {
                URL url = new URL(params[0]);
                res = get(url);
            } catch(MalformedURLException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return res;
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
    }

    private class PlunjrPost extends AsyncTask<String, Void, String> {

        public String combineParams(String[]... params) {
            StringBuilder combined = new StringBuilder();

            try {
                for (String[] p : params) {
                    combined.append(URLEncoder.encode(p[0], "utf-8"));
                    combined.append("=");
                    combined.append(URLEncoder.encode(p[1], "utf-8"));
                    combined.append("&");
                }

                if (combined.length() > 1) {
                    combined.setLength(combined.length() - 1);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            return combined.toString();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            try {
                URL url = new URL(params[0]);
                res = post(url, params[1]);
            } catch(MalformedURLException e) {
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
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("Content-Length", Integer.toString(postData.length()));
                connection.setUseCaches(false);

                // Send postData
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.write(postData.getBytes("utf-8"));
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
}
