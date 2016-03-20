package info.goforus.goforus.models;


import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarRecord;

public class Driver extends SugarRecord {
    public String name;
    public double lat;
    public double lng;
    public String short_bio;

    public DriverIndicator indicator;

    public Driver() {
    }

    public Driver(String name, double lat, double lng, String short_bio) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.short_bio = short_bio;
    }

    public void addIndicator(DriverIndicator indicator) {
        this.indicator = indicator;
    }

    public LatLng location() {
        return new LatLng(lat, lng);
    }

    public String toString() {
        return name;
    }
}
