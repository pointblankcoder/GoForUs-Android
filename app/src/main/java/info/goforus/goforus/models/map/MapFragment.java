package info.goforus.goforus.models.map;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;

import java.util.Arrays;
import java.util.List;

import info.goforus.goforus.BaseActivity;
import info.goforus.goforus.models.driver.Driver;
import info.goforus.goforus.models.driver.DriverIndicator;
import info.goforus.goforus.models.driver.DriverMarker;
import info.goforus.goforus.tasks.SimulateMyLocationClickTask;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private View mOriginalView;
    private MapWrapperLayout mMapWrapperLayout;

    private GoogleMap mMap;

    List<Driver> drivers = Arrays.asList(
            new Driver("Tim Westwood", 54.7897645, -1.3481971, ""),
            new Driver("Fat Head (Damo)", 54.7887345, -1.3401970, "")
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mOriginalView = super.onCreateView(inflater, container, savedInstanceState);

        mMapWrapperLayout = new MapWrapperLayout(getActivity());
        mMapWrapperLayout.addView(mOriginalView);

        getMapAsync(this);

        return mMapWrapperLayout;
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (true) {
            mMap.setMyLocationEnabled(true);
        }

        // Setup Map UI
        UiSettings mapUISettings = mMap.getUiSettings();
        mapUISettings.setAllGesturesEnabled(true);
        mapUISettings.setCompassEnabled(false);
        mapUISettings.setMapToolbarEnabled(false);
        mapUISettings.setZoomControlsEnabled(false);


        // Add all nearby cars to the map
        for (Driver d : drivers) {
            addDriverToMap(d);
        }
        // Assign an indicator with a nearby drivers
        for (Driver d : drivers) {
            d.addIndicator(new DriverIndicator(d, getActivity(), mMap, this));
        }

        // Setup Adapters / Listeners
        setOnDragListener(new MapWrapperLayout.OnDragListener() {
            @Override
            public void onDrag(MotionEvent motionEvent) {
                for (Driver d : drivers) {
                    if (d.indicator != null) {
                        d.indicator.update();
                    }
                }
            }
        });

        DriverMarker markerClass = new DriverMarker();
        mMap.setInfoWindowAdapter(markerClass);
        mMap.setOnInfoWindowClickListener(markerClass);
        mMap.setOnInfoWindowCloseListener(markerClass);
        mMap.setOnInfoWindowLongClickListener(markerClass);
        mMap.setOnMarkerClickListener(markerClass);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                for (Driver d : drivers) {
                    if (d.indicator != null) {
                        d.indicator.update();
                    }
                }
            }
        });

        mMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        for(Driver d : drivers) {
            if(d.indicator != null) {
                d.indicator.removeIndicator();
            }
        }

        BaseActivity activity = (BaseActivity) getActivity();
        activity.mApplication.stopLocationUpdates();
    }

    @Override
    public void onMapLoaded() {
        SimulateMyLocationClickTask task = new SimulateMyLocationClickTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this, getActivity());
        } else {
            task.execute(this, getActivity());
        }
        BaseActivity activity = (BaseActivity) getActivity();
        activity.mApplication.startLocationUpdates();
    }

    private void addDriverToMap(Driver driver) {
        driver.addToMap(getActivity(), mMap);
    }

    @Override
    public View getView() {
        return mOriginalView;
    }

    public void setOnDragListener(MapWrapperLayout.OnDragListener onDragListener) {
        mMapWrapperLayout.setOnDragListener(onDragListener);
    }
}
