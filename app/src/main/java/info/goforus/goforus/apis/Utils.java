package info.goforus.goforus.apis;

import android.support.annotation.Nullable;

import com.orhanobut.logger.Logger;

import info.goforus.goforus.BuildConfig;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.settings.DebugSettings;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

public class Utils {
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
    public final static Orders OrdersApi = Orders.getInstance();

    protected static String tokenParams() {
        Account account = Account.currentAccount();
        if(account != null) {
            return String.format("?customer_email=%s&customer_token=%s", account.email, account.apiToken);
        } else{
            return "?";
        }
    }

    public static String getBaseUri(){
        return (BuildConfig.DEBUG) ? DebugSettings.getInstance().getApiUrl() : "http://dev.goforus.info/api/v1/";
    }

    @Nullable
    public static JSONObject errorToJson(String errorMessage) {
        try {
            JSONObject baseJson = new JSONObject();
            return baseJson.put("error", new JSONObject().put("base", errorMessage));
        } catch (JSONException e) {
            Logger.e(e.toString());
            return null;
        }
    }
}
