package info.goforus.goforus.apis;

import android.support.annotation.Nullable;

import com.orhanobut.logger.Logger;

import info.goforus.goforus.models.accounts.Account;

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
    public final static Sessions SessionsApi = Sessions.getInstance();
    public final static Conversations ConversationsApi = Conversations.getInstance();
    public final static Messages MessagesApi = Messages.getInstance();

    protected static String tokenParams() {
        return String.format("?customer_email=%s&customer_token=%s", Account.currentAccount().email, Account.currentAccount().apiToken);
    }

    @Nullable
    protected static JSONObject errorToJson(String errorMessage) {
        try {
            JSONObject baseJson = new JSONObject();
            return baseJson.put("error", new JSONObject().put("base", errorMessage));
        } catch (JSONException e) {
            Logger.e(e.toString());
            return null;
        }
    }
}
