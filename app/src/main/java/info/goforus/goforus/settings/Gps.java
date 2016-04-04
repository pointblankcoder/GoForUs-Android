package info.goforus.goforus.settings;

import android.location.GpsStatus;
import android.location.LocationManager;

import info.goforus.goforus.Application;

public class Gps implements GpsStatus.Listener {
    private static final Gps gps = new Gps();
    private Gps(){}
    public static Gps getInstance(){ return gps; }

    private final Application mApplication = Application.getInstance();
    private final LocationManager mLocationManager = (LocationManager) mApplication.getSystemService(Application.LOCATION_SERVICE);

    private boolean gpsStopped = false;


    public static boolean turnedOn() {
        boolean gpsActivated = gps.mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return gpsActivated && !gps.gpsStopped && PermissionsHandler.hasGpsPermission();
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_STOPPED) {
            gpsStopped = true;
        }
    }
}
