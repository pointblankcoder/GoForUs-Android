package info.goforus.goforus.models.api;

import android.util.Log;

import java.io.IOException;

import info.goforus.goforus.models.account.Account;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

import static us.monoid.web.Resty.*;

public class Api {
    public static final String BaseURI = "http://dev.goforus.info/api/v1/";
    public static final String loginURI = BaseURI + "login";
    public static final String logoutURI = BaseURI + "logout";
    public static final String nearbyDriversURI = BaseURI + "drivers/nearby";
    public static final String updateLocationURI = BaseURI + "location/update";
    public static final Resty resty = new Resty();
    private final static String TAG = "API";

    public static void logOut() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    resty.json(logoutURI + tokenParams(), put(content("")));
                    Account.currentAccount().delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void updateMyLocation(final double lat, final double lng) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject location = new JSONObject();
                    location.put("lat", lat);
                    location.put("lng", lng);

                    resty.json(updateLocationURI + tokenParams(), put(content(location)));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static String tokenParams() {
        return String.format("?customer_email=%s&customer_token=%s", Account.currentAccount().email, Account.currentAccount().apiToken);
    }
}
