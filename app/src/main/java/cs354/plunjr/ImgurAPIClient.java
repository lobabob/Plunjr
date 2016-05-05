package cs354.plunjr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;

public class ImgurAPIClient extends HttpClient {

    private static final String LOG_TAG = "Imgur_Client";
    private Context mContext;

    public ImgurAPIClient(Context context) {
        mContext = context;
    }

    @Override
    void setPostHeaders(HttpURLConnection conn) {
        conn.setRequestProperty("Authorization", "Client-ID " + mContext.getString(R.string.imgur_client_id));
    }

    public String uploadImage(Bitmap img) {
        String link = null;

        // Convert bitmap to base64 string
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        String imgData = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        // Transform parameters into a correctly formatted JSON string
        JSONObject req = new JSONObject();
        try {
            req.put("image", imgData);
            req.put("key", mContext.getString(R.string.imgur_client_id));
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        String res = postJSONToURL(req, mContext.getString(R.string.imgur_post_uri));

        // Return the image direct link if it was included in the response, null otherwise
        try {
            JSONObject resObj = new JSONObject(res);
            link = resObj.getJSONObject("data").getString("link");
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return link;
    }

}
