package cs354.plunjr;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by Christian on 4/3/2016.
 */
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
                StringBuffer buffer = new StringBuffer("");
                String line;

                // Read response into buffer
                while((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                reader.close();
                connection.disconnect();
                res = buffer.toString();
            } catch(IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return res;
        }
    }
}
