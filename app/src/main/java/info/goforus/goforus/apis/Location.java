package info.goforus.goforus.apis;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import info.goforus.goforus.event_results.LocationUpdateServiceResult;
import info.goforus.goforus.event_results.NearbyDriversFromApiResult;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.put;

public class Location {

    private static final Location location = new Location();

    private Location() {
    }

    public static Location getInstance() {
        return location;
    }

    public static final String nearbyDriversURI = Utils.BaseURI + "partners/nearby";

    public JSONArray getNearbyDrivers(final double lat, final double lng) {
        JSONArray drivers = null;
        try {
            String latLngParams = String.format("&lat=%s&lng=%s", lat, lng);
            drivers = Utils.resty.json(nearbyDriversURI + Utils.tokenParams() + latLngParams).array();
            Logger.d("Got drivers JSON (%s)", drivers);
        } catch (IOException | JSONException e) {
            Logger.e(e, "getNearbyDrivers");
        }
        if(drivers != null) {
            EventBus.getDefault().post(new NearbyDriversFromApiResult(drivers));
        }
        return drivers;
    }


    public static final String updateLocationURI = Utils.BaseURI + "location/update";

    public void updateMyLocation(final double lat, final double lng) {
        try {
            JSONObject location = new JSONObject();
            location.put("lat", lat);
            location.put("lng", lng);

            Utils.resty.json(updateLocationURI + Utils.tokenParams(), put(content(location)));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onLocationUpdate(LocationUpdateServiceResult result) {
        updateMyLocation(result.getLocation().latitude, result.getLocation().longitude);
    }
}
