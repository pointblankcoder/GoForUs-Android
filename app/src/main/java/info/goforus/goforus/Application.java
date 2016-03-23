package info.goforus.goforus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
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

public class Application extends SugarApp implements GpsStatus.Listener {
    static final int LOCATION_PERMISSION_REQUEST = 0;
    static final int REQUEST_CHECK_GPS_SETTINGS = 1;

    public static GoogleApiClient googleApiClient;
    public android.location.LocationManager locationManager;

    public boolean hasGpsPermission = true;
    private Activity mCurrentActivity = null;

    public boolean loggedIn = false;


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

    protected void requireGps() {
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

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                if (mCurrentActivity != null) {
                    requireGps();
                }
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
