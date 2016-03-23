package info.goforus.goforus.models.driver;


import android.app.Activity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.orm.SugarRecord;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import info.goforus.goforus.R;

public class Driver extends SugarRecord {
    public String name;
    public double lat;
    public double lng;
    public String short_bio;

    public DriverIndicator indicator;
    public DriverMarker marker;

    public Driver() {
    }

    public Driver(String name, double lat, double lng, String short_bio) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.short_bio = short_bio;
    }

    public LatLng location() {
        return new LatLng(lat, lng);
    }

    public String toString() {
        return name;
    }

    public void addToMap(Activity activity, GoogleMap map) {
        Marker mMarker = map.addMarker(new MarkerOptions()
                        .position(location())
                        .visible(true)
                        .anchor(0.5f, 0.5f)
                        .title(name)
                        .snippet(short_bio)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
        );
        if (marker == null) {
            this.marker = new DriverMarker(activity, this, map, mMarker);
        }
    }

    public void goTo() {
        if (marker != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.latlng, 15);
            marker.map.animateCamera(cameraUpdate);
            marker.showInfoWindow();
        }
    }

    public DriverMarker setDriverMarker(DriverMarker marker) {
        return this.marker = marker;
    }

    public void addIndicator(DriverIndicator indicator) {
        this.indicator = indicator;
    }

}
