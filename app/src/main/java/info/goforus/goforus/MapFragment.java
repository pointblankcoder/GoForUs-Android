package info.goforus.goforus;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.orhanobut.logger.Logger;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.apis.listeners.NearbyDriversResponseListener;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.models.drivers.Driver;
import info.goforus.goforus.models.drivers.Indicator;
import info.goforus.goforus.models.drivers.InfoWindowAdapter;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapLoadedCallback, MapWrapperLayout.OnDragListener,
        GoogleMap.OnCameraChangeListener, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnInfoWindowCloseListener, GoogleMap.OnInfoWindowLongClickListener, GoogleMap.OnMarkerClickListener,
        NearbyDriversResponseListener {

    public static final String TAG = "MapFragment";
    private View mOriginalView;
    private MapWrapperLayout mMapWrapperLayout;
    private BaseActivity mActivity;
    private GoogleMap mMap;
    public List<Driver> currentlyDisplayedDrivers = new ArrayList<>();
    private MapView mapView;


    /* ======================== Fragment Overrides =================== */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mOriginalView = super.onCreateView(inflater, container, savedInstanceState);
        mActivity = (BaseActivity) getActivity();
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mapView = (MapView) view.findViewById(R.id.map);
        mMapWrapperLayout = (MapWrapperLayout) view.findViewById(R.id.map_wrapper);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onStart() {
        // Start Collecting Driver Updates Periodically
        Utils.startDriverUpdates(this);
        if(!isHidden()) {
            hideAllDrivers(false);
        }
        super.onStart();
    }

    @Override
    public void onHiddenChanged(boolean hidden){
        hideAllDrivers(hidden);
    }

    @Override
    public void onStop() {
        // Stop Collecting Driver Updates
        Utils.stopDriverUpdates();

        hideAllDrivers(true);

        super.onStop();
    }

    @Override
    public void onDestroy() {
        for (Driver d : currentlyDisplayedDrivers) {
            if (d.indicator != null) {
                d.indicator.removeIndicator();
            }
        }
        Utils.stopDriverUpdates();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        mMapWrapperLayout.setOnDragListener(this);
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
        Logger.d("Info Order Window  has been clicked. Starting Order flow");
        Logger.d("our activity is (%s)", mActivity);
        if (mActivity != null) {
            if (currentDriverSelected != null) {
                Intent intent = new Intent(mActivity, DriverDetailsActivity.class);
                intent.putExtra("Information", Parcels.wrap(currentDriverSelected.toDriverInformation()));
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
        Logger.d("marker was clicked, updated currently selected");
        currentDriverSelected = Driver.findByDriverMarker(currentlyDisplayedDrivers, marker);
        currentDriverSelected.goToWithInfoWindow();

        return true; // disable default behavior
    }

    @Override
    public void onDrag(MotionEvent event) {
        for (Driver d : currentlyDisplayedDrivers) {
            if (d.indicator != null) {
                d.indicator.update();
            }
        }
    }

    /* ========================= API Callbacks =================== */
    @Override
    public void onResponse(JSONArray response) {
        if (mMap == null){
            return;
        }
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

    /* =================== Helpers =================== */

    public void hideAllDrivers(boolean hide) {
        for (Driver d : currentlyDisplayedDrivers) {
            if (d.indicator != null) {
                if (hide) {
                    d.indicator.hide();
                } else {
                    d.indicator.show();
                }
            }
        }
    }

    public void addDriversToMap(final ArrayList<Driver> drivers) {
        final BaseActivity _activity = mActivity;
        mActivity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Logger.i("Adding nearby drivers (%s) to map", drivers.size());

                        List<Driver> newDisplayedDrivers = new ArrayList<Driver>();
                        for (Driver d : drivers) {
                            if (Driver.containsId(currentlyDisplayedDrivers, d.externalId)) {
                                // Just update the position of the current driver
                                int index = Driver.getIndexOfDriverFrom(currentlyDisplayedDrivers, d.externalId);
                                Driver _d = currentlyDisplayedDrivers.get(index);

                                // No need to continue we didn't move
                                if (_d.lat == d.lat && _d.lng == d.lng) {
                                    Logger.i("we already have %s displayed in the same location", _d);
                                    newDisplayedDrivers.add(_d);
                                    currentlyDisplayedDrivers.remove(_d);
                                    break;
                                }

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
