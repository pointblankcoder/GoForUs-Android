package info.goforus.goforus.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.event_results.LocationUpdateServiceResult;
import info.goforus.goforus.settings.Gps;

@SuppressWarnings("ResourceType")
public class LocationUpdateService extends IntentService {
    private LocationManager mLocationManager;

    public LocationUpdateService() {
        super("LocationUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (Gps.turnedOn()) {
            Location lastKnownLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLoc == null)
                lastKnownLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (lastKnownLoc != null) {
                LatLng responseLocation = new LatLng(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
                EventBus.getDefault().post(new LocationUpdateServiceResult(responseLocation));
            }
        }
    }
}

