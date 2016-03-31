package info.goforus.goforus.settings;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import info.goforus.goforus.Application;
import info.goforus.goforus.R;

public class PermissionsHandler {
    private static final PermissionsHandler permissionsHandler = new PermissionsHandler();
    private PermissionsHandler(){}
    public static PermissionsHandler getInstance(){
        return permissionsHandler;
    }


    protected  boolean GpsPermission;
    public static final int GPS_PERMISSIONS_REQUEST = 0;


    public static boolean hasGpsPermission(){
        return permissionsHandler.GpsPermission;
    }

    public static void gpsPermissionGranted(){
        permissionsHandler.GpsPermission = true;
        Application.getInstance().LocationUpdateHandler.turnUpdatesOn();
    }

    public static void checkGpsPermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission to access fine location and coarse location
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, GPS_PERMISSIONS_REQUEST);
            permissionsHandler.GpsPermission = false;
        } else {
            permissionsHandler.GpsPermission = true;
        }
    }

    public static void alertUserGpsIsRequired(final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.gps_is_required_alert_text)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        checkGpsPermissions(activity);
                    }
                });
        builder.create().show();
    }
}
