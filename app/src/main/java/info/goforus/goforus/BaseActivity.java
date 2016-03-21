package info.goforus.goforus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.LocationSettingsStates;

public class BaseActivity extends AppCompatActivity {
    public Application mApplication;

    /* =========================== Class Overrides =========================== */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (Application) this.getApplicationContext();
        mApplication.setCurrentActivity(this);
        if (mApplication.gpsOff()) {
            mApplication.requireGps();
            mApplication.checkGpsPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApplication.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);

        switch (requestCode) {
            case Application.REQUEST_CHECK_GPS_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All is good, let's continue
                        Log.d("foobar", "came back as okay");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d("foobar", "came back as cancelled");
                        mApplication.alertUserGpsIsRequired(this);
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    /* =========================== Class Specific =========================== */
    private void clearReferences() {
        Activity currActivity = mApplication.getCurrentActivity();
        if (this.equals(currActivity)) {
            mApplication.setCurrentActivity(null);
        }
    }
}
