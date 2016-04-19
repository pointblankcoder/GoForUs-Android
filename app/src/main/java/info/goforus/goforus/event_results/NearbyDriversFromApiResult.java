package info.goforus.goforus.event_results;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.drivers.Driver;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class NearbyDriversFromApiResult {
    List<Driver> mDrivers = new ArrayList<>();

    public NearbyDriversFromApiResult(JSONArray json) {
        if (json != null) {
            for (int i = 0; i < json.length(); i++) {
                JSONObject driverObject = null;
                try {
                    driverObject = json.getJSONObject(i);
                } catch (JSONException e) {
                    Logger.e(e.toString());
                }

                if (driverObject != null) {
                    mDrivers.add(Driver.updateOrCreateFromJson(driverObject));
                }
            }

            for (Driver d : mDrivers) {
                EventBus.getDefault().post(new DriverUpdateResult(d));
            }
        }
    }

    public List<Driver> getDrivers() {
        return mDrivers;
    }
}
