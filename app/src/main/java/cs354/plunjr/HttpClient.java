package cs354.plunjr;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class HttpClient {

    private static final String LOG_TAG = "HTTP_Client";


    // Can be overridden by child classes to set custom headers
    void setPostHeaders(HttpURLConnection connection) {}
    void setGetHeaders(HttpURLConnection connection)  {}

    String get(URL url) {
        String res = "[]";
        try {
            // Open connection and prepare to receive response
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            setGetHeaders(connection);

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

    String post(URL url, String postData) {
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
            setPostHeaders(connection);

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
