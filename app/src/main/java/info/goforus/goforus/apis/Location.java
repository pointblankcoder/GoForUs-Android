package info.goforus.goforus.apis;

import com.orhanobut.logger.Logger;

import java.io.IOException;

import info.goforus.goforus.apis.listeners.NearbyDriversResponseListener;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.put;

public class Location {

    private static final Location location = new Location();
    private Location(){}
    public static Location getInstance(){
        return location;
    }

    public static final String nearbyDriversURI = Utils.BaseURI + "partners/nearby";
    public void getNearbyDrivers(final double lat, final double lng, final NearbyDriversResponseListener listener) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    String latLngParams = String.format("&lat=%s&lng=%s", lat, lng);
                    JSONArray drivers = Utils.resty.json(nearbyDriversURI + Utils.tokenParams() + latLngParams).array();
                    Logger.d("Got drivers JSON (%s)", drivers);
                    listener.onResponse(drivers);

                } catch (IOException | JSONException e) {
                    Logger.e(e, "getNearbyDrivers");
                }
            }
        }).start();
    }


    public static final String updateLocationURI = Utils.BaseURI + "location/update";
    public void updateMyLocation(final double lat, final double lng) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject location = new JSONObject();
                    location.put("lat", lat);
                    location.put("lng", lng);

                    Utils.resty.json(updateLocationURI + Utils.tokenParams(), put(content(location)));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
