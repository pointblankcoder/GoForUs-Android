package info.goforus.goforus.apis;

import android.os.Handler;
import android.support.annotation.Nullable;

import com.orhanobut.logger.Logger;

import info.goforus.goforus.apis.listeners.NearbyDriversResponseListener;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.tasks.DriverUpdatesTask;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

// TODO: Add catch all for loss of internet connection (UnknownHostException)
public class Utils {
    public static final String BaseURI = "http://dev.goforus.info/api/v1/";
    public static final Resty resty = new Resty();
    private final static String TAG = "Utils";



    private static final Utils utils = new Utils();
    private Utils(){}
    public static Utils getInstance(){
        return utils;
    }


    // Access our associated APIs
    public final static Location LocationApi = Location.getInstance();
    public final static Conversation ConversationsApi = Conversation.getInstance();
    public final static Sessions SessionsApi = Sessions.getInstance();

    /* ============================ Periodic Call Starters/Stoppers ============================ */
    static Runnable mNearbyDriversTask;
    static Handler mTaskHandler = new Handler();

    public static void startDriverUpdates(NearbyDriversResponseListener listener) {
        if (mNearbyDriversTask == null) {
            mNearbyDriversTask = new DriverUpdatesTask(mTaskHandler, 4000, listener);
            mTaskHandler.post(mNearbyDriversTask);
        } else {
            Logger.d("tried to start driver updates when it's already working. Please stop updates before starting to start this again via Utils.stopDriverUpdates(Activity, Int)");
        }
    }

    public static void stopDriverUpdates() {
        if (mNearbyDriversTask != null) {
            Logger.d("stopping driver updates");
            mTaskHandler.removeCallbacks(mNearbyDriversTask);
            mNearbyDriversTask = null; // ensure we reset our current running task
        } else {
            Logger.d("tried to stop driver updates but updates have not started. Please start updates  via Utils.startDriverUpdates()");
        }
    }

    protected static String tokenParams() {
        return String.format("?customer_email=%s&customer_token=%s", Account.currentAccount().email, Account.currentAccount().apiToken);
    }

    @Nullable
    protected static JSONObject errorToJson(String errorMessage) {
        try {
            JSONObject baseJson = new JSONObject();
            return baseJson.put("error", new JSONObject().put("base", errorMessage));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
