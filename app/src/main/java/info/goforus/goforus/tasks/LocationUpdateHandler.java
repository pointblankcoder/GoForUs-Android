package info.goforus.goforus.tasks;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.GoForUs;
import info.goforus.goforus.event_results.LocationUpdateServiceResult;
import info.goforus.goforus.settings.Gps;

@SuppressWarnings("ResourceType")
public class LocationUpdateHandler implements LocationListener {
    private static LocationUpdateHandler ourInstance = new LocationUpdateHandler();
    private LocationManager mLocationManager;

    public static LocationUpdateHandler getInstance() {
        return ourInstance;
    }

    private LocationUpdateHandler() {
        mLocationManager = (LocationManager) GoForUs.getInstance()
                                                    .getSystemService(Context.LOCATION_SERVICE);
        turnUpdatesOn(); // we try right away in case the user is already logged in
    }


    // Forcing an update will collect the last known location of the user and then update
    // the account with that information, this should be used sparingly because it can update out of date
    // information for the account. Mainly used for forcing an update for login purposes.
    public void forceUpdate() {
        mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
        mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }

    public void turnUpdatesOn() {
        if (Gps.turnedOn()) {
            mLocationManager
                    .requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

            // Get last known location from the best provider we have
            Criteria criteria = new Criteria();
            String bestProvider = mLocationManager.getBestProvider(criteria, false);
            onLocationChanged(mLocationManager.getLastKnownLocation(bestProvider));
        }
    }

    Location currentBestLocation = null;

    @Override
    public void onLocationChanged(Location location) {
        if (location != null && isBetterLocation(location, currentBestLocation)) {
            currentBestLocation = location;

            LatLng responseLocation = new LatLng(location.getLatitude(), location.getLongitude());
            EventBus.getDefault().post(new LocationUpdateServiceResult(responseLocation));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Logger.i("Provider (%s) status has been changed to (%s) - (%s)", provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Logger.i("Provider (%s) has been enabled", provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Logger.i("Provider (%s) has been disabled", provider);
    }


    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /**
     * Determines whether one Locations reading is better than the current Locations fix
     * Gotta love google <3
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation
                .getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
