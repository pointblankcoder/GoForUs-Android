package info.goforus.goforus.event_results;

import com.google.android.gms.maps.model.LatLng;

public class LocationUpdateServiceResult {
    LatLng mLocation;

    public LocationUpdateServiceResult(LatLng location) { mLocation = location; }

    public LatLng getLocation() { return mLocation; }
}
