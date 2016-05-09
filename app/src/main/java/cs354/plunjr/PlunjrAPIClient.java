package cs354.plunjr;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

    public void loadReviews(ReviewListAdapter adapter, int restroomID) {
        new LoadReviewsTask(adapter, restroomID).execute();
    }

    public void loadReviews(ReviewListAdapter adapter, int restroomID, Callable onTaskCompleteCallback) {
        new LoadReviewsTask(adapter, restroomID, onTaskCompleteCallback).execute();
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

    public void uploadImages(String imgUrl, int restroomID) {
        new AddImageUrlsToRestroomTask(imgUrl, restroomID).execute();
    }

    public void uploadImages(String imgUrl, int restroomID, Callable onTaskCompleteCallback) {
        new AddImageUrlsToRestroomTask(imgUrl, restroomID, onTaskCompleteCallback);
    }

    /**
     * Used for asynchronously populating a restroom list adapter with restroom info
     * based on the user's current location. If this fails the adapter will have an
     * empty list but that should be the worst outcome.
     */
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
        protected void onPostExecute(Void res) {
            if(mOnTaskCompleteCallback != null) {
                try {
                    mOnTaskCompleteCallback.call();
                } catch(Exception e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Used for asynchronously populating a review list adapter with review
     * info using the restroom's ID. If this fails the adapter will have
     * an empty list but that should be the worst that happens.
     */
    private class LoadReviewsTask extends AsyncTask<Void, Void, Void> {


        private DateFormat parseDatePattern;
        private DateFormat formatDatePattern;

        private ReviewListAdapter mAdapter;
        private Callable mOnTaskCompleteCallable;
        private int mRestroomID;

        public LoadReviewsTask(ReviewListAdapter adapter, int restroomID) {
            this(adapter, restroomID, null);
        }

        public LoadReviewsTask(ReviewListAdapter adapter, int restroomID, Callable onTaskCompleteCallable) {
            mAdapter = adapter;
            mRestroomID = restroomID;
            mOnTaskCompleteCallable = onTaskCompleteCallable;

            parseDatePattern = new SimpleDateFormat(
                    mContext.getResources().getString(R.string.review_parse_date), Locale.ROOT);
            formatDatePattern = new SimpleDateFormat(
                    mContext.getResources().getString(R.string.review_format_date), Locale.ROOT);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
//                JSONArray reviews = new JSONArray(getResources().getString(R.string.debug_review_json));
                String res = getFromURL(String.format(mContext.getString(R.string.get_reviews_uri), mRestroomID));

                JSONArray reviews = new JSONArray();
                try {
                    reviews = new JSONArray(res);
                } catch(JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
                mAdapter.clear();

                // Create a hash map for each row's data (one hash map per row)
                for(int i = 0; i < reviews.length(); i++) {
                    JSONObject review = reviews.getJSONObject(i);
                    ReviewListAdapter.ReviewInfo rowData = new ReviewListAdapter.ReviewInfo();

                    rowData.user = review.optString("user");
                    rowData.rating = (float) review.optDouble("rating");
                    rowData.title = review.optString("title");
                    rowData.description = review.optString("description");
                    rowData.id = review.optInt("id");

                    rowData.date = review.optString("date");

                    try {
                        Date date = parseDatePattern.parse(rowData.date);
                        rowData.date = formatDatePattern.format(date);
                    } catch (ParseException e) {
                        Log.e("Date parsing/formatting", e.getMessage(), e);
                        rowData.date = "Invalid Date";
                    }
                    mAdapter.add(rowData);
                }
            } catch(JSONException e) {
                Log.e("Review List", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            mAdapter.notifyDataSetChanged();
            if(mOnTaskCompleteCallable != null) {
                try {
                    mOnTaskCompleteCallable.call();
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Used for asynchronously patching an existing restroom with image URLS.
     * If this fails, show a toast or something to let the user know their
     * image upload failed but that should be the worst consequence.
     */
    private class AddImageUrlsToRestroomTask extends AsyncTask<Void, Void, Void> {

        private String mURL;
        private int mRestroomID;
        private Callable mOnTaskCompleteCallback;

        public AddImageUrlsToRestroomTask(String url, int restroomID) {
            this(url, restroomID, null);
        }

        public AddImageUrlsToRestroomTask(String url, int restroomID, Callable onTaskCompleteCallback) {
            mURL = url;
            mRestroomID = restroomID;
            mOnTaskCompleteCallback = onTaskCompleteCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            JSONObject resObj = null;

            // Transform parameters into a correctly formatted string
            JSONObject req = new JSONObject();
            try {
                JSONArray urlsArr = new JSONArray();
                urlsArr.put(mURL);
                req.put("imagesUrl", urlsArr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            String res = patchJSONToURL(req, String.format(mContext.getString(R.string.patch_photo_uri), mRestroomID));

            // Convert response to JSON object
            if(res != null) {
                try {
                    resObj = new JSONObject(res);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            if(mOnTaskCompleteCallback != null) {
                try {
                    mOnTaskCompleteCallback.call();
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }
    }
}
