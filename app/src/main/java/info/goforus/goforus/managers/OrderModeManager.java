package info.goforus.goforus.managers;

import android.content.Intent;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.goforus.goforus.BaseActivity;
import info.goforus.goforus.MapFragment;
import info.goforus.goforus.R;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.drivers.Driver;

public class OrderModeManager {
    private static OrderModeManager ourInstance = new OrderModeManager();
    private boolean findDropOffVisible = true;
    private boolean findPickupVisible = true;
    private boolean removeDropOffVisible =  false;
    private boolean removePickupVisible =  false;
    private boolean completeVisible = false;
    private boolean messagesFabVisible = false;
    private boolean quickOrderFabVisible = false;
    private boolean exitModeFabVisible = true;
    private boolean quickLocationSelectionVisibile = true;

    public static OrderModeManager getInstance() { return ourInstance; }

    private OrderModeManager() { }

    public static final int FIND_PICKUP_LOCATION = 0;
    public static final int FIND_DROP_OFF_LOCATION = 1;

    private GoogleMap mMap;
    private BaseActivity mActivity;
    private MapFragment mMapFragment;
    private final DriversOnMapManager driversOnMapManager = DriversOnMapManager.getInstance();
    private final ContactDriverManager contactDriverManager = ContactDriverManager.getInstance();
    private ArrayList<Marker> pickupPoints = new ArrayList<>();
    private ArrayList<Marker> dropOffPoints = new ArrayList<>();

    @Bind(R.id.messageFab) View messagesFab;
    @Bind(R.id.quickOrderFab) View quickOrderFab;
    @Bind(R.id.exitModeFab) View exitModeFab;
    @Bind(R.id.quickLocationSelection) LinearLayout quickLocationSelection;
    @Bind(R.id.findDropOff) View findDropOff;
    @Bind(R.id.removeDropOff) View removeDropOff;
    @Bind(R.id.findPickup) View findPickup;
    @Bind(R.id.removePickup) View removePickup;
    @Bind(R.id.complete) View complete;

    DialogPlus mTipDialog;

    public void setup(BaseActivity activity, MapFragment mapFragment, GoogleMap map) {
        this.mActivity = activity;
        this.mMap = map;
        ButterKnife.bind(this, mActivity);
        mMapFragment = mapFragment;

        mTipDialog = DialogPlus.newDialog(mActivity).setHeader(R.layout.dialog_tips_main_header)
                               .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                               .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                               .setContentHolder(new ViewHolder(R.layout.dialog_tips_map_mode_body))
                               .setGravity(Gravity.CENTER).setCancelable(true)
                               .setOnClickListener(new OnClickListener() {
                                   @Override
                                   public void onClick(DialogPlus dialog, View view) {
                                       AppCompatCheckBox checkBox = (AppCompatCheckBox) dialog
                                               .findViewById(R.id.doNotShowTips);
                                       if (view.equals(dialog
                                               .findViewById(R.id.dismissTipDialog))) {
                                           mTipDialog.dismiss();
                                       } else if (view.equals(checkBox)) {
                                           Account account = Account.currentAccount();
                                           account.showOrderModeTips = !checkBox.isChecked();
                                           account.save();
                                       }
                                   }
                               }).create();

        findDropOff.setVisibility(findDropOffVisible ? View.VISIBLE : View.GONE);
        removeDropOff.setVisibility(removeDropOffVisible ? View.VISIBLE : View.GONE);
        findPickup.setVisibility(findPickupVisible ? View.VISIBLE : View.GONE);
        removePickup.setVisibility(removePickupVisible ? View.VISIBLE : View.GONE);
        complete.setVisibility(completeVisible ? View.VISIBLE : View.GONE);
        messagesFab.setVisibility(messagesFabVisible ? View.VISIBLE : View.GONE);
        quickOrderFab.setVisibility(quickOrderFabVisible ? View.VISIBLE : View.GONE);
        exitModeFab.setVisibility(exitModeFabVisible ? View.VISIBLE : View.GONE);
        quickLocationSelection
                .setVisibility(quickLocationSelectionVisibile ? View.VISIBLE : View.GONE);
    }

    public void enterOrderMode() {
        quickOrderFab.setVisibility(View.GONE);
        messagesFab.setVisibility(View.GONE);
        quickOrderFabVisible = false;
        messagesFabVisible = false;


        Toast.makeText(mActivity, "You're now in Order Mode", Toast.LENGTH_LONG).show();

        // we are in order mode only show that driver on the map
        driversOnMapManager.hideAllDriversExcept(true, driversOnMapManager.selectedDriver);
        driversOnMapManager.blockIndicatorsExcept(driversOnMapManager.selectedDriver);
        driversOnMapManager.selectedDriver.updatePositionOnMap();

        showQuickLocationSelectionDialog();

        exitModeFab.setVisibility(View.VISIBLE);
        exitModeFabVisible = true;
    }

    public void showTips(){
        if (Account.currentAccount().showOrderModeTips) {
            mTipDialog.show();
        }
    }

    public void exitMode(){
        exitModeFab.setVisibility(View.GONE);
        exitModeFabVisible = false;

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
        dropOffPoints = new ArrayList<>();

        quickLocationSelection.setVisibility(View.GONE);
        complete.setVisibility(View.GONE);
        quickLocationSelectionVisibile = false;
        completeVisible = false;


        Toast.makeText(mActivity, "You have cancelled your order", Toast.LENGTH_LONG).show();

        mMapFragment.switchMapMode(MapFragment.BROWSE_MODE);
    }

    @OnClick(R.id.exitModeFab)
    public void onExitModeClick() {
        exitMode();
    }

    @OnClick(R.id.complete)
    public void onCompleteClick() {
        quickLocationSelection.setVisibility(View.GONE);
        complete.setVisibility(View.GONE);
        quickLocationSelectionVisibile = false;
        completeVisible = false;

        contactDriverManager.setup(mActivity, driversOnMapManager.getSelectedDriver());
        contactDriverManager.show();
    }

    @OnClick(R.id.removePickup)
    public void onRemovePickupClick() {
        findPickup.setVisibility(View.VISIBLE);
        removePickup.setVisibility(View.GONE);
        complete.setVisibility(View.GONE);
        findPickupVisible = true;
        removePickupVisible = false;
        completeVisible = false;

        for (Marker m : pickupPoints)
            m.remove();
        pickupPoints = new ArrayList<>();
    }

    @OnClick(R.id.findPickup)
    public void onFindPickupClick() { openLocationFinder(FIND_PICKUP_LOCATION); }


    @OnClick(R.id.removeDropOff)
    public void onRemoveDropOffClick() {
        findDropOff.setVisibility(View.VISIBLE);
        removeDropOff.setVisibility(View.GONE);
        complete.setVisibility(View.GONE);
        findDropOffVisible = true;
        removeDropOffVisible = false;
        completeVisible = false;

        for (Marker m : dropOffPoints)
            m.remove();
        dropOffPoints = new ArrayList<>();
    }

    @OnClick(R.id.findDropOff)
    public void onFindDropOffClick() { openLocationFinder(FIND_DROP_OFF_LOCATION); }

    public void openLocationFinder(int requestId) {
        try {
            PlaceAutocomplete.IntentBuilder builder = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY);
            builder.setBoundsBias(toBounds(Account.currentAccount().location(), 5_000));

            Intent intent = builder.build(mActivity);
            mActivity.startActivityForResult(intent, requestId);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
        }
    }

    public void showQuickLocationSelectionDialog() {
        quickLocationSelection.setVisibility(View.VISIBLE);
        quickLocationSelectionVisibile = true;
    }

    public ArrayList<Marker> getPickupPoints() { return pickupPoints; }

    public ArrayList<Marker> getDropOffPoints() { return dropOffPoints; }

    public void addPickupPoint(LatLng location) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title("Pickup Point")
                                                          .draggable(true)
                                                          .icon(BitmapDescriptorFactory
                                                                  .fromResource(R.drawable.ic_nature_black_36dp)));


        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15);
        mMap.animateCamera(cameraUpdate, 1, null);
        mMapFragment.dropPinEffect(marker);


        Toast.makeText(mActivity, "Pickup point added", Toast.LENGTH_SHORT).show();

        findPickup.setVisibility(View.GONE);
        removePickup.setVisibility(View.VISIBLE);
        findPickupVisible = false;
        removePickupVisible = true;

        pickupPoints.add(marker);
    }

    public void addDropOffPoint(LatLng location) {
        Marker marker = mMap
                .addMarker(new MarkerOptions().position(location).title("Drop Off Point")
                                              .draggable(true).icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.ic_person_pin_circle_black_36dp)));
        dropOffPoints.add(marker);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15);
        mMap.animateCamera(cameraUpdate, 1, null);
        mMapFragment.dropPinEffect(marker);

        Toast.makeText(mActivity, "Drop Off point added", Toast.LENGTH_SHORT).show();

        findDropOff.setVisibility(View.GONE);
        removeDropOff.setVisibility(View.VISIBLE);
        findDropOffVisible = false;
        removeDropOffVisible = true;

        if (pickupPoints.size() == 1 && dropOffPoints.size() == 1) {
            complete.setVisibility(View.VISIBLE);
            completeVisible = true;
        }
    }

    public void explainToUserAboutLongPress() {
        // TODO: Explain to the user that long press is available if they were not able to find a location by searching.
    }


    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }
}
