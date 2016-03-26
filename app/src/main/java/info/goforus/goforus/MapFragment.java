package info.goforus.goforus;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.account.Account;
import info.goforus.goforus.models.api.Api;
import info.goforus.goforus.models.driver.Driver;
import info.goforus.goforus.models.driver.Indicator;
import info.goforus.goforus.models.driver.InfoWindowAdapter;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback,
        GoogleMap.OnMapLoadedCallback, MapWrapperLayout.OnDragListener,
        GoogleMap.OnCameraChangeListener, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnInfoWindowCloseListener, GoogleMap.OnInfoWindowLongClickListener, GoogleMap.OnMarkerClickListener,
        Api.ApiNearbyDriversListener {

    private static final String TAG = "MapFragment";
    private View mOriginalView;
    private MapWrapperLayout mMapWrapperLayout;
    private BaseActivity mActivity;
    private GoogleMap mMap;
    public List<Driver> currentlyDisplayedDrivers;

    /* ======================== Fragment Overrides =================== */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mOriginalView = super.onCreateView(inflater, container, savedInstanceState);

        // Assign our maps surround layout to so we can track screen gestures
        mMapWrapperLayout = new MapWrapperLayout(getActivity());
        mMapWrapperLayout.addView(mOriginalView);

        // Init Google Map
        getMapAsync(this);

        currentlyDisplayedDrivers = new ArrayList<>();
        mActivity = (BaseActivity) getActivity();

        return mMapWrapperLayout;
    }

    @Override
    public void onStart() {
        // Start Collecting Driver Updates Periodically
        Application.mApi.startDriverUpdates(this);
        mActivity.mApplication.requireGps();

        Log.d(TAG, "Map has loaded, starting location updates");
        mActivity.mApplication.startLocationUpdates();

        super.onStart();
    }

    @Override
    public void onStop() {
        // Stop Collecting Driver Updates Periodically
        Application.mApi.stopDriverUpdates();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        for (Driver d : currentlyDisplayedDrivers) {
            if (d.indicator != null) {
                d.indicator.removeIndicator();
            }
        }

        mActivity.mApplication.stopLocationUpdates();
        Application.mApi.stopDriverUpdates();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View getView() {
        return mOriginalView;
    }


    /* ======================== Google Map Related =================== */
    @Override
    public void onCameraChange(CameraPosition position) {
        for (Driver d : currentlyDisplayedDrivers) {
            if (d.indicator != null) {
                d.indicator.update();
            }
        }
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Setup Map UI
        UiSettings mapUISettings = mMap.getUiSettings();
        mapUISettings.setAllGesturesEnabled(true);
        mapUISettings.setCompassEnabled(false);
        mapUISettings.setMapToolbarEnabled(false);
        mapUISettings.setZoomControlsEnabled(false);

        mMap.setInfoWindowAdapter(new InfoWindowAdapter(mActivity));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowCloseListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMapLoadedCallback(this);


        // listen to dragging motion
        setOnDragListener(this);
    }

    @Override
    public void onMapLoaded() {
        Account account = Account.currentAccount();
        LatLng latLng = new LatLng(account.lat, account.lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
        mMap.animateCamera(cameraUpdate, 1000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                /*
                 TODO if the user is a first time user, start a helper flow to guide them through how to order a driver for delivery
                */
            }

            @Override
            public void onCancel() {
            }
        });
    }

    /* ======================== Marker Listeners =================== */
    private Driver currentDriverSelected;

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, "Info Order Window  has been clicked. Starting Order flow");
        Log.d(TAG, String.format("our activity is (%s)", mActivity));
        if (mActivity != null) {
            if (currentDriverSelected != null) {
                Intent intent = new Intent(mActivity, DriverDetailsActivity.class);
                Bundle extras = new Bundle();
                extras.putParcelable("Driver", currentDriverSelected.toDriverInformation());
                intent.putExtras(extras);
                mActivity.startActivity(intent);
            }
        }
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        currentDriverSelected = null;
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        // TODO: when in Order placement mode, long press should act as placing EXACT pickup point
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "marker was clicked, updated currently selected");
        currentDriverSelected = Driver.findByDriverMarker(currentlyDisplayedDrivers, marker);
        currentDriverSelected.goToWithInfoWindow();

        return true; // disable default behavior
    }

    /* ======================== Map Wrapper Listeners =================== */
    @Override
    public void onDrag(MotionEvent motionEvent) {
        for (Driver d : currentlyDisplayedDrivers) {
            if (d.indicator != null) {
                d.indicator.update();
            }
        }
    }

    public void setOnDragListener(MapWrapperLayout.OnDragListener onDragListener) {
        mMapWrapperLayout.setOnDragListener(onDragListener);
    }

    /* ========================= API Callbacks =================== */
    @Override
    public void onResponse(JSONArray response) {
        // Create the array of drivers from json response
        ArrayList<Driver> drivers = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            JSONObject driverObject = null;
            try {
                driverObject = response.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (driverObject != null) {
                drivers.add(new Driver(driverObject));
            }
        }
        addDriversToMap(drivers);
    }

    /* ========================= Methods =================== */
    public void addDriversToMap(final ArrayList<Driver> drivers) {
        final BaseActivity _activity = mActivity;
        mActivity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, String.format("Adding nearby drivers (%s) to map", drivers.size()));

                        List<Driver> newDisplayedDrivers = new ArrayList<Driver>();
                        for (Driver d : drivers) {
                            if (Driver.containsId(currentlyDisplayedDrivers, d.externalId)) {
                                Log.d(TAG, "we already have the driver displayed, update the old object");
                                // Just update the position of the current driver
                                int index = Driver.getIndexOfDriverFrom(currentlyDisplayedDrivers, d.externalId);
                                Driver _d = currentlyDisplayedDrivers.get(index);
                                _d.lat = d.lat;
                                _d.lng = d.lng;
                                if (_d.marker != null) {
                                    _d.updatePositionOnMap();
                                    if (_d.marker.isInfoWindowShown()) {
                                        _d.goToWithInfoWindow();
                                    }
                                }

                                if (_d.indicator != null)
                                    _d.indicator.update();
                                newDisplayedDrivers.add(_d);
                                currentlyDisplayedDrivers.remove(_d);
                            } else {
                                Log.d(TAG, "can't find the driver on the screen, let's add one");
                                Log.d(TAG, String.format("our activity is (%s)", _activity));
                                // Add to map
                                d.addToMap(mMap);
                                d.indicator = new Indicator(d, _activity, mMap, MapFragment.this);
                                d.indicator.update();
                                newDisplayedDrivers.add(d);
                            }
                        }

                        // Clear drivers that are not in range anymore
                        for (Driver d : currentlyDisplayedDrivers) {
                            d.indicator.removeIndicator();
                            d.marker.remove();
                        }

                        currentlyDisplayedDrivers = newDisplayedDrivers;
                    }
                }
        );

    }
}
