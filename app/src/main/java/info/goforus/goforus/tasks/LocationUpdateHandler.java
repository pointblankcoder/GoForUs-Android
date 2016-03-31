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
        turnUpdatesOn(); // we try right away incase the user is already logged in
    }


    public void turnUpdatesOn(){
        if (Gps.turnedOn()) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng responseLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Account account = Account.currentAccount();
        if (account != null && account.location() != responseLocation) {
            EventBus.getDefault().post(new LocationUpdateServiceResult(account.externalId, responseLocation));
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
