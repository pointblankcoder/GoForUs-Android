package info.goforus.goforus;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.orhanobut.logger.Logger;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.ButterKnife;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.drivers.Driver;
import info.goforus.goforus.models.drivers.InfoWindowAdapter;
import info.goforus.goforus.tasks.DriversUpdateHandler;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, MapWrapperLayout.GestureListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener, GoogleMap.OnInfoWindowLongClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    public static final String TAG = "MapFragment";
    public static final int ORDER_MODE = 0;
    public static final int BROWSE_MODE = 1;

    View mOriginalView;
    MapWrapperLayout mMapWrapperLayout;
    BaseActivity mActivity;
    GoogleMap mMap;
    boolean firstLoad = true;
    boolean mHidden;
    private DialogPlus mMiniProfileDriverTipDialog;
    private DriversOnMapManager driversOnMapManager;
    public int mapMode = BROWSE_MODE;


    /* ======================== Fragment Overrides =================== */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        DriversUpdateHandler.getInstance().stopUpdates();

        super.onDestroy();
    }

    public void switchMapMode(int mapMode) {
        this.mapMode = mapMode;

        if (mapMode == ORDER_MODE) {
            mOriginalView.setBackgroundResource(R.drawable.map_border);
            mOriginalView.setVisibility(View.VISIBLE);
            mOriginalView.setPadding(16, 16, 16, 16);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            mOriginalView.setLayoutParams(params);

            Toast.makeText(getContext(), "You're now in Order Mode, set your pickup point by holding down anywhere on the map", Toast.LENGTH_LONG)
                 .show();
            final View exitModeFab = mActivity.findViewById(R.id.exitModeFab);


            // we are in order mode only show that driver on the map
            driversOnMapManager.hideAllDriversExcept(true, driversOnMapManager.selectedDriver);
            driversOnMapManager.blockIndicatorsExcept(driversOnMapManager.selectedDriver);
            driversOnMapManager.selectedDriver.updatePositionOnMap();

            exitModeFab.setVisibility(View.VISIBLE);
            exitModeFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View exitModeFab = mActivity.findViewById(R.id.exitModeFab);
                    exitModeFab.setVisibility(View.GONE);
                    driversOnMapManager.setSelectedDriver(null);
                    driversOnMapManager.hideAllDrivers(false);
                    driversOnMapManager.unblockIndicators();
                    for (Driver d : driversOnMapManager.getCurrentlyDisplayedDrivers()) {
                        d.marker.remove();
                    }
                    for (Driver d : driversOnMapManager.getCurrentlyDisplayedDrivers()) {
                        d.addToMap(mMap);
                    }


                    for (Marker m : pickupPoints) {
                        m.remove();
                    }
                    for (Marker m : dropOffPoints) {
                        m.remove();
                    }

                    pickupPoints = new ArrayList<>();
                    dropOffPoints =  new ArrayList<>();

                    Toast.makeText(getContext(), "You have cancelled your order", Toast.LENGTH_LONG)
                         .show();

                    switchMapMode(BROWSE_MODE);
                }
            });
        } else {
            mOriginalView.setBackgroundResource(android.R.color.transparent);
            mOriginalView.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mOriginalView = super.onCreateView(inflater, container, savedInstanceState);
        // Assign our maps surround layout to so we can track screen gestures
        mMapWrapperLayout = new MapWrapperLayout(getActivity());
        mMapWrapperLayout.addView(mOriginalView);
        ButterKnife.bind(mMapWrapperLayout);
        mActivity = (BaseActivity) getActivity();

        // Init Google Map
        getMapAsync(this);

        mMiniProfileDriverTipDialog = DialogPlus.newDialog(mActivity)
                                                .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                                                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                                                .setContentHolder(new ViewHolder(R.layout.dialog_tips_mini_drivers_profile_body))
                                                .setGravity(Gravity.TOP).setCancelable(true)
                                                .setOnClickListener(new OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogPlus dialog, View view) {
                                                        AppCompatCheckBox checkBox = (AppCompatCheckBox) mActivity
                                                                .findViewById(R.id.doNotShowTips);
                                                        if (view.equals(mActivity
                                                                .findViewById(R.id.dismissTipDialog))) {
                                                            mMiniProfileDriverTipDialog.dismiss();
                                                        } else if (view.equals(checkBox)) {
                                                            Account account = Account
                                                                    .currentAccount();
                                                            account.showMiniProfileDriverTip = !checkBox
                                                                    .isChecked();
                                                            account.save();
                                                        }
                                                    }
                                                }).create();

        if (!firstLoad) {
            // We hold a reference to the old activity in the indicators. let's reassign them!
            for (Driver d : driversOnMapManager.getCurrentlyDisplayedDrivers()) {
                d.indicator.updateAfterConfigurationChange(mActivity);
            }
        }

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
        if (!isHidden() && driversOnMapManager != null) {
            driversOnMapManager.hideAllDrivers(false);
        }
    }

    @Override
    public void onStop() {
        driversOnMapManager.hideAllDrivers(true);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isHidden() && driversOnMapManager != null) {
            driversOnMapManager.hideAllDrivers(false);
        }

        if (driversOnMapManager != null) {
            driversOnMapManager.updateIndicators();
        }
        DriversUpdateHandler.getInstance().startUpdates();
    }

    @Override
    public void onPause() {
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

        driversOnMapManager.hideAllDrivers(hidden);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); }

    @Override
    public View getView() {
        return mOriginalView;
    }


    /* ======================== Google Map Related =================== */
    @Override
    public void onCameraChange(CameraPosition position) { driversOnMapManager.updateIndicators(); }

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
        mMap.setOnMapLongClickListener(this);


        // listen to dragging motion
        mMapWrapperLayout.setGestureListener(this);


        if (mapMode == ORDER_MODE) {
            mOriginalView.setBackgroundResource(R.drawable.map_border);
            mOriginalView.setVisibility(View.VISIBLE);
            mOriginalView.setPadding(16, 16, 16, 16);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            mOriginalView.setLayoutParams(params);
        }


        driversOnMapManager = DriversOnMapManager.getInstance();
        driversOnMapManager.setup(mActivity, mMap, this);
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
        driversOnMapManager.updateIndicators();
    }

    /* ======================== Marker Listeners =================== */

    ArrayList<Marker> pickupPoints = new ArrayList<>();
    ArrayList<Marker> dropOffPoints = new ArrayList<>();

    @Override
    public void onMapLongClick(LatLng point) {
        if (mapMode == ORDER_MODE) {
            if (pickupPoints.size() == 0) {
                Marker marker = mMap
                        .addMarker(new MarkerOptions().position(point).title("Pickup Point")
                                                      .draggable(true).icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.ic_nature_black_36dp)));
                pickupPoints.add(marker);
                dropPinEffect(marker);
                Toast.makeText(getContext(), "Pickup point added", Toast.LENGTH_SHORT).show();
            } else if (pickupPoints.size() == 1 && dropOffPoints.size() == 0) {
                Marker marker = mMap
                        .addMarker(new MarkerOptions().position(point).title("Dropoff Point")
                                                      .draggable(true).icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.ic_person_pin_circle_black_36dp)));
                dropOffPoints.add(marker);
                dropPinEffect(marker);
                Toast.makeText(getContext(), "Dropoff point added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "You can not add more than 1 dropoff point and 1 pickup point", Toast.LENGTH_SHORT)
                     .show();
            }
        }
    }


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
        if (pickupPoints.contains(marker) || dropOffPoints.contains(marker)) {
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    return null;
                }
            });
            marker.showInfoWindow();
        } else {
            mMap.setInfoWindowAdapter(new InfoWindowAdapter(mActivity));
            currentDriverSelected = Driver
                    .findByDriverMarker(driversOnMapManager.getCurrentlyDisplayedDrivers(), marker);

            // someone clicked on the marker a tad too fast!
            if (currentDriverSelected == null) return false;

            currentDriverSelected.goToWithInfoWindow();
            // Do we show the driver tip
            if (Account.currentAccount().showMiniProfileDriverTip) {
                mMiniProfileDriverTipDialog.show();
                AppCompatCheckBox checkBox = (AppCompatCheckBox) mMiniProfileDriverTipDialog
                        .findViewById(R.id.doNotShowTips);
                checkBox.setSelected(!Account.currentAccount().showMapTips);
            }
        }

        return true; // disable default behavior
    }

    @Override
    public void onDrag(MotionEvent event) {
        driversOnMapManager.updateIndicators();
    }

    @Override
    public void onFling() {
        driversOnMapManager.updateIndicators();
    }

    private void dropPinEffect(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math
                        .max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post again 15ms later.
                    handler.postDelayed(this, 15);
                } else {
                    onMarkerClick(marker);
                }
            }
        });
    }
}
