package cs354.plunjr;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;


public class PlunjrAPIClient extends HttpClient {

    private static final String LOG_TAG = "Plunjr_Client";
    private Context mContext;

    public PlunjrAPIClient(Context context) {
        mContext = context;
    }

    public void loadRestrooms(RestroomListAdapter adapter) {
        new LoadRestroomsTask(adapter).execute();
    }

    public void loadRestrooms(RestroomListAdapter adapter, Callable onTaskCompleteCallback) {
        new LoadRestroomsTask(adapter, onTaskCompleteCallback).execute();
    }

    public JSONArray getReviews(int restroomID) {
        String res = getFromURL(String.format(mContext.getString(R.string.get_reviews_uri), restroomID));

        JSONArray resArr = new JSONArray();
        try {
            resArr = new JSONArray(res);
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return resArr;
    }

    public JSONObject postReview(String address, String user, String rating,
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
        String res = postJSONToURL(obj, mContext.getString(R.string.post_review_uri));

        // Convert response to JSON array
        try {
            resObj = new JSONObject(res);
        } catch(JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return resObj;
    }

    public JSONObject addImages(String imgUrl, int restroomID) {
        JSONObject resObj = null;

        // Transform parameters into a correctly formatted string
        JSONObject req = new JSONObject();
        try {
            JSONArray urlsArr = new JSONArray();
            urlsArr.put(imgUrl);
            req.put("imagesUrl", urlsArr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        String res = patchJSONToURL(req, String.format(mContext.getString(R.string.patch_photo_uri), restroomID));

        // Convert response to JSON object
        if(res != null) {
            try {
                resObj = new JSONObject(res);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
        return resObj;
    }

    private class LoadRestroomsTask extends AsyncTask<Void, Void, Void> {

        private static final double METERS_PER_MILE = 1609.344;
        private RestroomListAdapter mAdapter;
        private Callable mOnTaskCompleteCallback;

        public LoadRestroomsTask(RestroomListAdapter adapter) {
            this(adapter, null);
        }

        public LoadRestroomsTask(RestroomListAdapter adapter, Callable onTaskCompleteCallback) {
            mAdapter = adapter;
            mOnTaskCompleteCallback = onTaskCompleteCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mAdapter.clear();
                LatLng myPos = AddressUtils.getUserLatLng(mContext);
                if(myPos != null) {
//                    JSONArray restrooms = new JSONArray(getResources().getString(R.string.debug_restroom_json));
                    String restroomsString = getFromURL(String.format(mContext.getString(R.string.get_restrooms_uri), myPos.latitude, myPos.longitude));
                    JSONArray restrooms;
                    try {
                        restrooms = new JSONArray(restroomsString);
                    } catch(JSONException e) {
                        restrooms = new JSONArray();
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                    for (int i = 0; i < restrooms.length(); i++) {
                        JSONObject restroom = restrooms.getJSONObject(i);
                        RestroomListAdapter.RestroomInfo rrInfo = new RestroomListAdapter.RestroomInfo();

                        // Calculate restroom distance from user
                        LatLng rrPos = new LatLng(restroom.optDouble("lat"), restroom.optDouble("lng"));
                        float res[] = {0};
                        Location.distanceBetween(rrPos.latitude, rrPos.longitude, myPos.latitude, myPos.longitude, res);

                        rrInfo.latLng = rrPos;
                        rrInfo.distance = res[0] / METERS_PER_MILE;
                        rrInfo.name = restroom.optString("name");
                        rrInfo.address = restroom.optString("address");
                        rrInfo.rating = (float) restroom.optDouble("averageRating");
                        rrInfo.reviewCount = restroom.optInt("reviewCount");
                        rrInfo.id = restroom.optInt("id");

                        JSONArray imgUrlsJSON = restroom.optJSONArray("imagesUrl");
                        if(imgUrlsJSON != null) {
                            String[] imgUrls = new String[imgUrlsJSON.length()];
                            for(int j = 0; j < imgUrlsJSON.length(); j++) {
                                imgUrls[j] = imgUrlsJSON.get(j).toString();
                            }
                            rrInfo.imgUrls = imgUrls;
                        }
                        mAdapter.add(rrInfo);
                    }
                }
            } catch (JSONException e) {
                Log.e("Restroom List", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(mOnTaskCompleteCallback != null) {
                try {
                    mOnTaskCompleteCallback.call();
                } catch(Exception e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }
    }
}
