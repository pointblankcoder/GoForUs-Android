package info.goforus.goforus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.orm.SugarApp;
import com.orm.SugarContext;

import info.goforus.goforus.models.account.Account;
import info.goforus.goforus.models.api.Api;

public class Application extends SugarApp implements GpsStatus.Listener, LocationListener {
    static final int LOCATION_PERMISSION_REQUEST = 0;
    static final int REQUEST_CHECK_GPS_SETTINGS = 1;

    static final String TAG = "Application";

    public static final Api mApi = new Api();

    public static GoogleApiClient googleApiClient;
    public android.location.LocationManager locationManager;

    public boolean hasGpsPermission = true;

    /* Current activity */
    private Activity mCurrentActivity = null;

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    public boolean isReady() {
        checkGpsPermissions();
        return gpsOn() && hasGpsPermission;
    }

    @Override
    @SuppressWarnings("ResourceType")
    public void onCreate() {
        super.onCreate();
        locationManager = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.addGpsStatusListener(this);
    }

    public boolean gpsOn() {
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    public boolean gpsOff() {
        return !locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    public void requireGps() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(mCurrentActivity)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                Log.d("onResult", String.format("status(%s) state(%s)", status, state));
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        try {
                            status.startResolutionForResult(mCurrentActivity, REQUEST_CHECK_GPS_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(mCurrentActivity, REQUEST_CHECK_GPS_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    public boolean checkGpsPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation for why we need GPS?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getCurrentActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(getCurrentActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                // TODO:  an expanation to the user *asynchronously* -- don't bloc this thread waiting for the user's response. After the user sees the explanation, try again to request the permission.
            }
            // Request permission to access fine location and coarse location
            ActivityCompat.requestPermissions(getCurrentActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            hasGpsPermission = true;
            return true;
        }
        hasGpsPermission = false;
        return false;
    }

    protected void alertUserGpsIsRequired(Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.gps_is_required_alert_text)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        requireGps();
                    }
                });
        builder.create().show();
    }

    @SuppressWarnings("ResourceType")
    public void startLocationUpdates() {
        Log.d(TAG, "sub to location updates");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);

        Location lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLoc == null)
            lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (lastKnownLoc != null) {
            Log.d(TAG, "Updating our last known location");
            mApi.updateMyLocation(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
            if (Account.currentAccount() != null) {
                Account.currentAccount().updateLocation(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
            }
        }
    }

    @SuppressWarnings("ResourceType")
    public void stopLocationUpdates() {
        Log.d(TAG, "unsub to location updates");
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Account account = Account.currentAccount();
        // Only update if we need too
        if (account != null && account.lat != location.getLatitude() && account.lng != location.getLongitude()) {
            Log.d(TAG, String.format("we have a new location (%s)", location));
            mApi.updateMyLocation(location.getLatitude(), location.getLongitude());
            Account.currentAccount().updateLocation(location.getLatitude(), location.getLongitude());
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, String.format("Network status has changed to (%s)", status));
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, String.format("Network network provider enabled (%s)", provider));
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, String.format("Network network provider disabled (%s)", provider));
    }

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                Log.d(TAG, String.format("GPS status has started (%s)", event));
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                Log.d(TAG, String.format("GPS status has stopped (%s)", event));
                //if (mCurrentActivity != null) {
                    requireGps();
                //}
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                break;
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
