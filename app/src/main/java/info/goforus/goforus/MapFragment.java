package info.goforus.goforus;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Gravity;
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
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.orhanobut.logger.Logger;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.drivers.Driver;
import info.goforus.goforus.models.drivers.InfoWindowAdapter;
import info.goforus.goforus.tasks.DriversUpdateHandler;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, MapWrapperLayout.GestureListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnInfoWindowCloseListener, GoogleMap.OnInfoWindowLongClickListener, GoogleMap.OnMarkerClickListener {

    public static final String TAG = "MapFragment";
    View mOriginalView;
    MapWrapperLayout mMapWrapperLayout;
    BaseActivity mActivity;
    GoogleMap mMap;
    boolean firstLoad = true;
    boolean mHidden;
    private DialogPlus mMiniProfileDriverTipDialog;
    private DriversOnMapManager driversOnMapManager;


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

        // listen to dragging motion
        mMapWrapperLayout.setGestureListener(this);

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
        currentDriverSelected = Driver
                .findByDriverMarker(driversOnMapManager.getCurrentlyDisplayedDrivers(), marker);
        currentDriverSelected.goToWithInfoWindow();

        // Do we show the driver tip
        if (Account.currentAccount().showMiniProfileDriverTip) {
            mMiniProfileDriverTipDialog.show();
            AppCompatCheckBox checkBox = (AppCompatCheckBox) mMiniProfileDriverTipDialog
                    .findViewById(R.id.doNotShowTips);
            checkBox.setSelected(!Account.currentAccount().showMapTips);
        }

        return true; // disable default behavior
    }

    @Override
    public void onDrag(MotionEvent event) { driversOnMapManager.updateIndicators(); }

    @Override
    public void onFling() { driversOnMapManager.updateIndicators(); }
}
