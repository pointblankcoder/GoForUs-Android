package info.goforus.goforus;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowCloseListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.Marker;

import info.goforus.goforus.models.Driver;

public class DriverMarker implements InfoWindowAdapter, OnInfoWindowClickListener,
        OnInfoWindowCloseListener, OnInfoWindowLongClickListener, OnMarkerClickListener {

    Activity activity;
    Driver driver;

    DriverMarker(Activity activity, Driver driver) {
        this.activity = activity;
        this.driver = driver;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(activity, "Click Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        Toast.makeText(activity, "Close Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        Toast.makeText(activity, "Info Window long click", Toast.LENGTH_SHORT).show();
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
