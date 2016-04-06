package info.goforus.goforus;

import com.google.android.gms.maps.GoogleMap;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import info.goforus.goforus.event_results.DriverUpdateResult;
import info.goforus.goforus.models.drivers.Driver;
import info.goforus.goforus.models.drivers.Indicator;

public class DriversOnMapManager {
    private static DriversOnMapManager ourInstance = new DriversOnMapManager();

    public static DriversOnMapManager getInstance() {
        return ourInstance;
    }

    ArrayList<Driver> mCurrentlyDisplayedDrivers = new ArrayList<>();
    BaseActivity mActivity;
    GoogleMap mMap;
    MapFragment mFragment;

    private DriversOnMapManager() {
        EventBus.getDefault().register(this);
    }

    public void setCurrentlyDisplayedDrivers(ArrayList<Driver> currentlyDisplayedDrivers) {
        mCurrentlyDisplayedDrivers = currentlyDisplayedDrivers;
    }

    public ArrayList<Driver> getCurrentlyDisplayedDrivers() {
        return mCurrentlyDisplayedDrivers;
    }

    public void setup(BaseActivity activity, GoogleMap map, MapFragment mapFragment) {
        mActivity = activity;
        mMap = map;
        mFragment = mapFragment;
    }

    public void goToDriverWithInfoWindow(Driver d, int animationTime) {
        int driverIndex = Driver.getIndexOfDriverFrom(mCurrentlyDisplayedDrivers, d.externalId);
        Driver driver = mCurrentlyDisplayedDrivers.get(driverIndex);
        driver.goToWithInfoWindow(animationTime);
    }

    public void updateIndicators() {
        for (Driver d : mCurrentlyDisplayedDrivers) {
            if (d.indicator != null) {
                d.indicator.update();
            }
        }
    }

    public void hideAllDrivers(boolean hide) {
        for (Driver d : mCurrentlyDisplayedDrivers) {
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

        if (Driver.containsId(mCurrentlyDisplayedDrivers, d.externalId)) {
            // Just update the position of the current driver
            int index = Driver.getIndexOfDriverFrom(mCurrentlyDisplayedDrivers, d.externalId);
            // -1 represents a not found driver in the array
            if (index != -1) {
                Driver displayedDriverWithSameExternalId = mCurrentlyDisplayedDrivers.get(index);

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
            d.indicator = new Indicator(d, mActivity, mMap, mFragment);
            d.indicator.update();
            mCurrentlyDisplayedDrivers.add(d);
        }
    }
}
