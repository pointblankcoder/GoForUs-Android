package info.goforus.goforus.models.api;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.orm.dsl.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.account.Account;
import info.goforus.goforus.models.driver.Driver;
import info.goforus.goforus.tasks.DriverUpdatesTask;

import us.monoid.json.*;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

import static us.monoid.web.Resty.*;

// TODO: Add catch all for loss of internet connection (UnknownHostException)
public class Api {
    public static final String BaseURI = "http://dev.goforus.info/api/v1/";
    public static final String loginURI = BaseURI + "login";
    public static final String logoutURI = BaseURI + "logout";
    public static final String nearbyDriversURI = BaseURI + "partners/nearby";
    public static final String updateLocationURI = BaseURI + "location/update";
    public static final Resty resty = new Resty();

    private final static String TAG = "API";
    private static final int ATTEMPT_COUNT_LIMIT = 3;

    /* ============================ Listeners ============================ */
    private List<ApiUpdateListener> mListeners = new ArrayList<>();

    public interface ApiUpdateListener {
        void onNearbyDriversUpdate(List<Driver> drivers);

        void onLogOut(JSONObject success);

        void onLogIn(JSONObject response);
    }

    public interface ApiLoginListener {
        void onResponse(JSONObject response);
    }

    public interface ApiLogoutListener {
        void onResponse(JSONObject response);
    }

    public interface ApiNearbyDriversListener {
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

    /* Start: Login */
    private int loginAttemptCount = 0;

    public JSONObject attemptLogin(@NotNull final String email, @NotNull final String password) throws ApiAttemptCountExceededException {
        try {
            JSONObject baseJson = new JSONObject();
            JSONObject customerData = new JSONObject();

            customerData.put("email", email);
            customerData.put("password", password);
            baseJson.put("customer", customerData);

            JSONObject response = resty.json(loginURI, put(content(baseJson))).object();
            return response;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            if (loginAttemptCount > ATTEMPT_COUNT_LIMIT) {
                loginAttemptCount = 0;
                throw new ApiAttemptCountExceededException("login attempt limit exceeded");
            } else {
                loginAttemptCount += 1;
                return attemptLogin(email, password);
            }
        }
    }


    public void logIn(@NotNull final String email, @NotNull final String password) {
        new Thread(new Runnable() {
            public void run() {
                JSONObject response;

                try {
                    response = attemptLogin(email, password);
                } catch (ApiAttemptCountExceededException e) {
                    response = errorToJson(e.getMessage());
                }

                for (ApiUpdateListener listener : mListeners) {
                    listener.onLogIn(response);
                }
            }
        }).start();
    }


    public void logIn(final String email, final String password, final ApiLoginListener listener) {
        new Thread(new Runnable() {
            public void run() {
                JSONObject response;

                try {
                    response = attemptLogin(email, password);
                } catch (ApiAttemptCountExceededException e) {
                    response = errorToJson(e.getMessage());
                }

                listener.onResponse(response);
            }
        }).start();
    }
    /* Stop: Login */



    /* Start: Logout */
    private int logoutAttemptCount = 0;
    public JSONObject attemptLogOut() throws ApiAttemptCountExceededException {
        try {
            return resty.json(logoutURI + tokenParams(), put(content(""))).object();
        } catch (JSONException | IOException e) {
            if (logoutAttemptCount > ATTEMPT_COUNT_LIMIT) {
                logoutAttemptCount = 0;
                throw new ApiAttemptCountExceededException("login attempt limit exceeded");
            } else {
                logoutAttemptCount += 1;
                return attemptLogOut();
            }
        }
    }


    public void logOut() {
        new Thread(new Runnable() {
            public void run() {
                JSONObject response;
                try {
                     response = attemptLogOut();
                } catch (ApiAttemptCountExceededException e) {
                    response = errorToJson(e.getMessage());
                }

                for (ApiUpdateListener listener : mListeners) {
                    listener.onLogOut(response);
                }
            }
        }).start();
    }


    public void logOut(final ApiLogoutListener listener) {
        new Thread(new Runnable() {
            public void run() {
                JSONObject response;
                try {
                    response = attemptLogOut();
                } catch (ApiAttemptCountExceededException e) {
                    response = errorToJson(e.getMessage());
                }
                listener.onResponse(response);
            }
        }).start();
    }
    /* Stop: Logout */


    /* Start: Nearby Drivers */
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
    /* Stop: Nearby Drivers */


    /* Start: Update current location*/
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
    /* Stop: Update current location*/



    /* =========================== Helpers =======================  */
    private String tokenParams() {
        return String.format("?customer_email=%s&customer_token=%s", Account.currentAccount().email, Account.currentAccount().apiToken);
    }

    @Nullable
    private JSONObject errorToJson(@NotNull String errorMessage) {
        try {
            JSONObject baseJson = new JSONObject();
            return baseJson.put("error", new JSONObject().put("base", errorMessage));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }



    /* =========================== Exceptions =======================  */
    private class ApiAttemptCountExceededException extends Exception {

        public ApiAttemptCountExceededException() {
        }

        public ApiAttemptCountExceededException(String detailMessage) {
            super(detailMessage);
        }
    }
}
