package info.goforus.goforus;


import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.atomic.AtomicInteger;

public class Driver {
    protected String name;
    protected double lat;
    protected double lng;
    protected int viewID;
    protected String short_bio;

    public Driver(String _name, double _lat, double _lng, String _short_bio) {
        name = _name;
        lat = _lat;
        lng = _lng;
        short_bio = _short_bio;
        viewID = (int)(Math.random() + Math.random() * 1000);
    }

    public LatLng location() {
        return new LatLng(lat, lng);
    }

    public String toString() {
        return name;
    }
}
