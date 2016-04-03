package info.goforus.goforus.tasks;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.Application;
import info.goforus.goforus.event_results.LocationUpdateServiceResult;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.settings.Gps;

@SuppressWarnings("ResourceType")
public class LocationUpdateHandler implements LocationListener {
    private static LocationUpdateHandler ourInstance = new LocationUpdateHandler();
    private LocationManager mLocationManager;

    public static LocationUpdateHandler getInstance() {
        return ourInstance;
    }

    private LocationUpdateHandler() {
        mLocationManager = (LocationManager) Application.getInstance().getSystemService(Context.LOCATION_SERVICE);
        turnUpdatesOn(); // we try right away in case the user is already logged in
    }


    // Forcing an update will collect the last known location of the user and then update
    // the account with that information, this should be used sparingly because it can update out of date
    // information for the account. Mainly used for forcing an update for login purposes.
    public void forceUpdate(){
        // Request a single update and then add back out periodic updates
        mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }

    public void turnUpdatesOn(){
        if (Gps.turnedOn()) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            onLocationChanged(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // sometimes we don't have a last known location, very rare.
        if(location != null) {
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
}
