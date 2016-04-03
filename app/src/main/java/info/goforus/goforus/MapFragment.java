package info.goforus.goforus;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
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
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import info.goforus.goforus.event_results.DriverUpdateResult;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.drivers.Driver;
import info.goforus.goforus.models.drivers.Indicator;
import info.goforus.goforus.models.drivers.InfoWindowAdapter;
import info.goforus.goforus.tasks.DriversUpdateHandler;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, MapWrapperLayout.GestureListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener, GoogleMap.OnInfoWindowLongClickListener, GoogleMap.OnMarkerClickListener {

    public static final String TAG = "MapFragment";
    View mOriginalView;
    MapWrapperLayout mMapWrapperLayout;
    BaseActivity mActivity;
    GoogleMap mMap;
    public List<Driver> currentlyDisplayedDrivers = new ArrayList<>();
    boolean firstLoad = true;
    boolean mHidden;


    /* ======================== Fragment Overrides =================== */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        for (Driver d : currentlyDisplayedDrivers) {
            if (d.indicator != null) {
                d.indicator.removeIndicator();
            }
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mOriginalView = super.onCreateView(inflater, container, savedInstanceState);

        // Assign our maps surround layout to so we can track screen gestures
        mMapWrapperLayout = new MapWrapperLayout(getActivity());
        mMapWrapperLayout.addView(mOriginalView);
        ButterKnife.bind(mMapWrapperLayout);

        // Init Google Map
        getMapAsync(this);

        mActivity = (BaseActivity) getActivity();
        return mMapWrapperLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isHidden()) {
            hideAllDrivers(false);
        }
    }

    @Override
    public void onStop() {
        hideAllDrivers(true);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
        updateIndicators();
        DriversUpdateHandler.getInstance().startUpdates();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        DriversUpdateHandler.getInstance().stopUpdates();
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        mHidden = hidden;
        if (hidden) {
            DriversUpdateHandler.getInstance().stopUpdates();
        } else {
            DriversUpdateHandler.getInstance().startUpdates();
        }

        hideAllDrivers(hidden);
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
        updateIndicators();
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
        mapUISettings.setRotateGesturesEnabled(false);

        mMap.setInfoWindowAdapter(new InfoWindowAdapter(mActivity));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowCloseListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMapLoadedCallback(this);

        // listen to dragging motion
        mMapWrapperLayout.setGestureListener(this);

    }

    @Override
    public void onMapLoaded() {
        if (firstLoad) {
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
            firstLoad = false;
        }
        updateIndicators();
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
                intent.putExtra("Information", Parcels
                        .wrap(currentDriverSelected.toDriverInformation()));
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
        updateIndicators();
    }

    @Override
    public void onFling() {
        updateIndicators();
    }

    public void updateIndicators() {
        for (Driver d : currentlyDisplayedDrivers) {
            if (d.indicator != null) {
                d.indicator.update();
            }
        }
    }

    /* =================== Helpers =================== */
    public void hideAllDrivers(boolean hide) {
        for (Driver d : currentlyDisplayedDrivers) {
            if (d.indicator != null) {
                if (hide) {
                    d.indicator.hide();
                } else {
                    d.indicator.update();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDriversUpdate(DriverUpdateResult result) {
        Driver d = result.getDriver();

        if (Driver.containsId(currentlyDisplayedDrivers, d.externalId)) {
            // Just update the position of the current driver
            int index = Driver.getIndexOfDriverFrom(currentlyDisplayedDrivers, d.externalId);
            // -1 represents a not found driver in the array
            if (index != -1) {
                Driver displayedDriverWithSameExternalId = currentlyDisplayedDrivers.get(index);

                // No need to continue we didn't move
                if (displayedDriverWithSameExternalId.lat == d.lat && displayedDriverWithSameExternalId.lng == d.lng) {
                    return;
                }

                displayedDriverWithSameExternalId.lat = d.lat;
                displayedDriverWithSameExternalId.lng = d.lng;
                displayedDriverWithSameExternalId.updatePositionOnMap();
                displayedDriverWithSameExternalId.indicator.update();

                if (displayedDriverWithSameExternalId.marker != null) {
                    if (displayedDriverWithSameExternalId.marker.isInfoWindowShown()) {
                        displayedDriverWithSameExternalId.goToWithInfoWindow();
                    }
                }
            }
        } else {
            // Add to map
            d.addToMap(mMap);
            d.indicator = new Indicator(d, mActivity, mMap, MapFragment.this);
            d.indicator.update();
            currentlyDisplayedDrivers.add(d);
        }
    }
}
