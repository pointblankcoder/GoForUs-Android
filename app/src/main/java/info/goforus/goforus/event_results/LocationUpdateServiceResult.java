package info.goforus.goforus.event_results;

import com.google.android.gms.maps.model.LatLng;

public class LocationUpdateServiceResult {
    LatLng mLocation;
    long mAccountId;

    public LocationUpdateServiceResult(int accountId, LatLng location) {
        mAccountId = accountId;
        mLocation = location;
    }

    public long getAccountId() {
        return mAccountId;
    }

    public LatLng getLocation() {
        return mLocation;
    }
}
