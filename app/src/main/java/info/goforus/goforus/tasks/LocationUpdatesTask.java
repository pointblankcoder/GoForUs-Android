package info.goforus.goforus.tasks;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import com.orhanobut.logger.Logger;

import info.goforus.goforus.Application;
import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.settings.Gps;

public class LocationUpdatesTask implements LocationListener {
    private static final String TAG = "LocationUpdatesTask";

    private final Application mApplication = Application.getInstance();
    private final LocationManager mLocationManager = (LocationManager) mApplication.getSystemService(Application.LOCATION_SERVICE);

    public LocationUpdatesTask() {
    }

    public boolean running = false;

    @SuppressWarnings("ResourceType")
    public void start() {
        running = true;
        if (Gps.turnedOn()) {
            Account account = Account.currentAccount();

            Logger.d("sub to location updates");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);

            Location lastKnownLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLoc == null)
                lastKnownLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnownLoc != null && account != null) {
                Logger.d("Updating our last known location");
                Utils.LocationApi.updateMyLocation(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
                if (account != null) {
                    account.updateLocation(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
                }
            }
        }
    }

    @SuppressWarnings("ResourceType")
    public void stop() {
        running = false;
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Account account = Account.currentAccount();

        // Only update if we need too
        if (account != null && account.lat != location.getLatitude() && account.lng != location.getLongitude()) {
            Logger.d("we have a new location (%s)", location);
            Utils.LocationApi.updateMyLocation(location.getLatitude(), location.getLongitude());
            Account.currentAccount().updateLocation(location.getLatitude(), location.getLongitude());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Logger.d("Network status has changed to (%s)", status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Logger.d("Network network provider enabled (%s)", provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Logger.d("Network network provider disabled (%s)", provider);
    }
}
