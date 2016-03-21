package info.goforus.goforus.models.driver;

import android.app.Activity;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowCloseListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class DriverMarker implements InfoWindowAdapter, OnInfoWindowClickListener,
        OnInfoWindowCloseListener, OnInfoWindowLongClickListener, OnMarkerClickListener {

    protected Activity activity;

    protected Driver driver;
    protected Marker marker;
    protected GoogleMap map;
    protected LatLng latlng;

    public DriverMarker() {
    }

    public DriverMarker(Activity activity, Driver driver, GoogleMap map, Marker marker) {
        this.activity = activity;
        this.driver = driver;
        this.marker = marker;
        this.map = map;
        this.latlng = driver.location();
    }

    public void showInfoWindow() {
        marker.showInfoWindow();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        //Toast.makeText(activity, "Click Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        //Toast.makeText(activity, "Close Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        //Toast.makeText(activity, "Info Window long click", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
