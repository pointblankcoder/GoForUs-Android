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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.orhanobut.logger.Logger;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import info.goforus.goforus.managers.DriversOnMapManager;
import info.goforus.goforus.managers.OrderModeManager;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.drivers.Driver;
import info.goforus.goforus.models.drivers.InfoWindowAdapter;
import info.goforus.goforus.models.orders.Order;
import info.goforus.goforus.tasks.DriversUpdateHandler;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, MapWrapperLayout.GestureListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener, GoogleMap.OnInfoWindowLongClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    public static final String TAG = "MapFragment";
    public static final int ORDER_MODE = 0;
    public static final int BROWSE_MODE = 1;

    public View mOriginalView;
    MapWrapperLayout mMapWrapperLayout;
    BaseActivity mActivity;
    public GoogleMap mMap;
    boolean firstLoad = true;
    boolean shouldShowJourney = false;
    Order journeyToShow;
    boolean mHidden;
    private DialogPlus mMiniProfileDriverTipDialog;
    private DriversOnMapManager driversOnMapManager;
    public int mapMode = BROWSE_MODE;
    private OrderModeManager orderModeManager = OrderModeManager.getInstance();


    public void setJourneyToShow(Order order){
        shouldShowJourney = true;
        journeyToShow = order;
    }

    /* ======================== Fragment Overrides =================== */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        MapsInitializer.initialize(getContext());
    }

    @Override
    public void onDestroy() {
        DriversUpdateHandler.getInstance().stopUpdates();

        super.onDestroy();
    }

    public void switchMapMode(int mapMode) {
        this.mapMode = mapMode;

        if (mapMode == ORDER_MODE) {
            // TODO: This should be put in the enterOrderMode but how do I get the original view reference?
            mOriginalView.setBackgroundResource(R.drawable.map_border);
            mOriginalView.setVisibility(View.VISIBLE);
            mOriginalView.setPadding(16, 16, 16, 16);

            orderModeManager.setup((BaseActivity) getActivity(), this, mMap);
            orderModeManager.enterOrderMode();
        } else {
            View quickOrderFab = mActivity.findViewById(R.id.quickOrderFab);
            View messagesFab = mActivity.findViewById(R.id.messageFab);
            if (quickOrderFab != null) {
                quickOrderFab.setVisibility(View.VISIBLE);
            }

            if (messagesFab != null) {
                messagesFab.setVisibility(View.VISIBLE);
            }

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
            showJourney();
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

        switchMapMode(mapMode);
    }

    @Override
    public void onMapLoaded() {
        if (firstLoad && !shouldShowJourney) {
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
        showJourney();
    }

    public void showJourney(){
        if (shouldShowJourney) {
            OrderModeManager.getInstance()
                            .loadFromOrder((NavigationActivity) getContext(), journeyToShow);
            shouldShowJourney = false;
            journeyToShow = null;
        }
    }

    /* ======================== Marker Listeners =================== */

    @Override
    public void onMapLongClick(LatLng point) {
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
        if (orderModeManager.getPickupPoints().contains(marker) || orderModeManager.getDropOffPoints().contains(marker)) {
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
            currentDriverSelected = Driver
                    .findByDriverMarker(driversOnMapManager.getCurrentlyDisplayedDrivers(), marker);
            mMap.setInfoWindowAdapter(new InfoWindowAdapter(mActivity, currentDriverSelected));

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

    public void dropPinEffect(final Marker marker) {
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
