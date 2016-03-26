package info.goforus.goforus.models.api;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.account.Account;

import info.goforus.goforus.models.driver.Driver;
import info.goforus.goforus.tasks.DriverUpdatesTask;
import us.monoid.json.*;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

import static us.monoid.web.Resty.*;

public class Api {
    public static final String BaseURI = "http://dev.goforus.info/api/v1/";
    public static final String loginURI = BaseURI + "login";
    public static final String logoutURI = BaseURI + "logout";
    public static final String nearbyDriversURI = BaseURI + "partners/nearby";
    public static final String updateLocationURI = BaseURI + "location/update";
    public static final Resty resty = new Resty();

    private Intent mNearbyDriversService;

    private final static String TAG = "API";

    /* ============================ Listeners ============================ */
    private List<ApiUpdateListener> mListeners = new ArrayList<>();

    public interface ApiUpdateListener {
        void onNearbyDriversUpdate(List<Driver> drivers);

        void onLogOut(boolean success);

        void onLogIn(JSONObject response);
    }

    public interface ApiLoginListener {
        void onResponse(JSONObject response);
    }
    public void addApiUpdateListener(ApiUpdateListener listener) {
        mListeners.add(listener);
    }

    public void removeApiUpdateListener(ApiUpdateListener listener) {
        mListeners.remove(listener);
    }

    /* ============================ Periodic Call Starters/Stoppers ============================ */
    Runnable mNearbyDriversTask;
    Handler mTaskHandler = new Handler();

    public void startDriverUpdates() {
        if (mNearbyDriversTask == null) {
            mNearbyDriversTask = new DriverUpdatesTask(mTaskHandler, 4000);
            mTaskHandler.post(mNearbyDriversTask);
        } else {
            Log.d(TAG, "tried to start driver updates when it's already working. Please stop updates before starting to start this again via Api.stopDriverUpdates(Activity, Int)");
        }
    }

    public void stopDriverUpdates() {
        if (mNearbyDriversTask != null) {
            Log.d(TAG, "stopping driver updates");
            mTaskHandler.removeCallbacks(mNearbyDriversTask);
            mNearbyDriversTask = null; // ensure we reset our current running task
        } else {
            Log.d(TAG, "tried to stop driver updates but updates have not started. Please start updates  via Api.startDriverUpdates()");
        }
    }


    /* ============================ Api Calls ============================ */
    public void logIn(final String email, final String password) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject baseJson = new JSONObject();
                    JSONObject customerData = new JSONObject();

                    customerData.put("email", email);
                    customerData.put("password", password);
                    baseJson.put("customer", customerData);

                    JSONObject response = resty.json(loginURI + tokenParams(), put(content(baseJson))).object();
                    for (ApiUpdateListener listener : mListeners) {
                        listener.onLogIn(response);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void logIn(final String email, final String password, final ApiLoginListener listener) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject baseJson = new JSONObject();
                    JSONObject customerData = new JSONObject();

                    customerData.put("email", email);
                    customerData.put("password", password);
                    baseJson.put("customer", customerData);

                    JSONObject response = resty.json(loginURI, put(content(baseJson))).object();
                    listener.onResponse(response);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void logOut() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    resty.json(logoutURI + tokenParams(), put(content("")));
                    for (ApiUpdateListener listener : mListeners) {
                        listener.onLogOut(true);
                    }
                } catch (IOException e) {
                    for (ApiUpdateListener listener : mListeners) {
                        listener.onLogOut(false);
                    }
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getNearbyDrivers(final double lat, final double lng) {
        new Thread(new Runnable() {
            public void run() {
                List<Driver> drivers = new ArrayList<Driver>();
                try {
                    String latLngParams = String.format("&lat=%s&lng=%s", lat, lng);
                    JSONArray driversArray = (JSONArray) resty.json(nearbyDriversURI + tokenParams() + latLngParams).array();
                    Log.d(TAG, String.format("Got drivers JSON (%s)", driversArray));

                    for (int i = 0; i < driversArray.length(); i++) {
                        JSONObject driverObject = driversArray.getJSONObject(i);
                        drivers.add(new Driver(driverObject));
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, String.format("sending drivers to all listeners (%s)", mListeners));
                for (ApiUpdateListener listener : mListeners) {
                    listener.onNearbyDriversUpdate(drivers);
                }
            }
        }).start();
    }

    public void updateMyLocation(final double lat, final double lng) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject location = new JSONObject();
                    location.put("lat", lat);
                    location.put("lng", lng);

                    resty.json(updateLocationURI + tokenParams(), put(content(location)));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String tokenParams() {
        return String.format("?customer_email=%s&customer_token=%s", Account.currentAccount().email, Account.currentAccount().apiToken);
    }
}
