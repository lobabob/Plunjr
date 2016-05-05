package cs354.plunjr;

import android.util.Log;

import org.json.JSONArray;
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

public abstract class HttpClient {

    private static final String LOG_TAG = "HTTP_Client";


    // Can be overridden by child classes to set custom headers
    void setPostHeaders(HttpURLConnection conn) {}
    void setGetHeaders(HttpURLConnection conn)  {}

    String getFromURL(String urlString) {
        String res = null;
        try {
            URL url = new URL(urlString);
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
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            setGetHeaders(conn);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder build = new StringBuilder();
            String line;

            // Read response into buffer
            while((line = reader.readLine()) != null) {
                build.append(line);
            }
            reader.close();
            conn.disconnect();
            res = build.toString();
        } catch(IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return res;
    }

    String postJSONToURL(JSONObject req, String urlString) {
        return postJSONToURL(req.toString(), urlString);
    }

    String postJSONToURL(JSONArray req, String urlString) {
        return postJSONToURL(req.toString(), urlString);
    }

    String postJSONToURL(String jsonString, String urlString) {
        String res = null;
        try {
            URL url = new URL(urlString);
            res = post(url, jsonString);
        } catch(MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return res;
    }

    private String post(URL url, String postData) {
        String res = null;
        try {
            // Open connection and prepare to send postData
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length()));
            conn.setUseCaches(false);

            // Set child class headers if overridden
            setPostHeaders(conn);

            // Send postData
            Writer wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            wr.write(postData);
            wr.close();

            // Receive Response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder build = new StringBuilder();
            String line;

            // Read response into buffer
            while ((line = reader.readLine()) != null) {
                build.append(line);
            }
            reader.close();
            conn.disconnect();
            res = build.toString();
        } catch(IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return res;
    }
}
