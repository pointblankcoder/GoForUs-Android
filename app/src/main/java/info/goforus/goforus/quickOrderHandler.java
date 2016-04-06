package info.goforus.goforus;

import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;

import java.util.ArrayList;
import java.util.Arrays;

import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.drivers.Driver;

public class QuickOrderHandler implements OnClickListener {
    final NavigationActivity activity;
    private final DriversOnMapManager driversOnMapManager;
    MapFragment mapFragment;
    DialogPlus mDialog;
    View vehicleStep;
    View itemStep;
    View distanceStep;
    Button proceedButton;
    Button backButton;
    Button nextDriverButton;
    ArrayList<View> mSteps;
    ArrayList<Driver> excludedDrivers = new ArrayList<>();
    LinearLayout driverFoundStep;
    TextView driverFoundText;
    Driver closestDriver = null;
    float[] closetDriverResults = new float[1];


    public QuickOrderHandler(NavigationActivity activity) {
        this.activity = activity;
        driversOnMapManager = DriversOnMapManager.getInstance();
    }

    @Override
    public void onClick(DialogPlus dialog, View clickedView) {
        mDialog = dialog;
        vehicleStep = dialog.findViewById(R.id.vehicleTypeStep);
        itemStep = dialog.findViewById(R.id.itemTypeStep);
        distanceStep = dialog.findViewById(R.id.distanceStep);
        driverFoundStep = (LinearLayout) dialog.findViewById(R.id.driverFoundLayout);
        driverFoundText = (TextView) dialog.findViewById(R.id.driverFoundTextView);
        proceedButton = (Button) dialog.findViewById(R.id.nextBtn);
        backButton = (Button) dialog.findViewById(R.id.backBtn);
        nextDriverButton = (Button) dialog.findViewById(R.id.nextDriver);
        mSteps = new ArrayList<>(Arrays
                .asList(vehicleStep, itemStep, distanceStep, driverFoundStep));
        mapFragment = (MapFragment) activity.getSupportFragmentManager().findFragmentByTag("Map");


        // Going forward
        if (clickedView == proceedButton) {

            RadioGroup selectionGroup = (RadioGroup) currentStep()
                    .findViewWithTag("selectionGroup");
            if (selectionGroup != null && selectionGroup
                    .getCheckedRadioButtonId() == -1) { // nothing was selected
                // TODO: show error message on the group to show it's required users may just keep clicking thinking it's broken
                return;
            }

            View nextStep = nextStep(currentStep());

            if (nextStep == null) {
                dialog.dismiss();
            } else if (nextStep == driverFoundStep) {
                Driver driver = findBestDriver();
                showStep(nextStep);

                showBestDriverResults(driver);
            } else {
                showBackButton();
                showProceedButton();
                showStep(nextStep);
            }
        }

        // Going backward
        if (clickedView == backButton) {
            View previousStep = previousStep(currentStep());
            showStep(previousStep);
            showProceedButton();
            showBackButton();
            excludedDrivers = new ArrayList<>(); // We went back, clear the exclusion list

            if (currentStep() == mSteps.get(0)) {
                hideBackButton();
            }
        }

        if (clickedView == nextDriverButton) {
            excludedDrivers.add(closestDriver);
            Driver driver = findBestDriver();
            showBestDriverResults(driver);
        }
    }

    private void showBestDriverResults(Driver driver) {
        if (driver == null) {
            driverFoundText
                    .setText("Unable to find a driver right now. The Pre-Booking feature is Work In Progress");
            showBackButton();
            hideNextDriverButton();
            hideProceedButton();
        } else {
            hideBackButton();
            showNextDriverButton();
            showProceedButton();

            driversOnMapManager.goToDriverWithInfoWindow(driver, 1000);

            driverFoundText.setText(String
                    .format("%s matched your criteria, is available and currently the closest to you. Would you like to proceed or find someone else?", driver.name));
        }
    }

    int driverCycleCount = 0;

    private Driver findBestDriver() {
        Account account = Account.currentAccount();
        LatLng myLocation = account.location();
        ArrayList<Driver> driversToCycle = (ArrayList<Driver>) driversOnMapManager
                .getCurrentlyDisplayedDrivers().clone();

        for (Driver d : excludedDrivers) {
            driversToCycle.remove(d);
        }

        if (excludedDrivers.contains(closestDriver)) {
            closestDriver = null;
            closetDriverResults = new float[1];
        }

        for (Driver d : driversToCycle) {
            LatLng driverLocation = d.location();
            float[] results = new float[1];
            Location.distanceBetween(myLocation.latitude, myLocation.longitude, driverLocation.latitude, driverLocation.longitude, results);

            if (closetDriverResults[0] < results[0]) {
                closestDriver = d;
                closetDriverResults = results;
            }
        }


        // if we have no drivers to cycle through anymore. and we are not on the first count.
        // wipe the slate clean and show that there are no drivers for them
        if (driverCycleCount > 0 && driversToCycle.size() == 0) {
            closestDriver = null;
            closetDriverResults = new float[1];
        }

        driverCycleCount++;
        return closestDriver;
    }

    private View previousStep(View currentStep) {
        try {
            return mSteps.get(mSteps.indexOf(currentStep) - 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private View currentStep() {
        for (View step : mSteps) {
            if (step.getVisibility() == View.VISIBLE) return step;
        }

        return mSteps
                .get(0); // Bad design choice? Always return the first step if we can't find the current step?
    }

    private View nextStep(View currentStep) {
        try {
            return mSteps.get(mSteps.indexOf(currentStep) + 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void showStep(View stepToShow) {
        for (View step : mSteps) {
            step.setVisibility(View.GONE);
        }
        stepToShow.setVisibility(View.VISIBLE);
    }


    private void hideProceedButton() { proceedButton.setVisibility(View.GONE); }

    private void showProceedButton() { proceedButton.setVisibility(View.VISIBLE); }

    private void showNextDriverButton() { nextDriverButton.setVisibility(View.VISIBLE); }

    private void hideNextDriverButton() { nextDriverButton.setVisibility(View.GONE); }

    private void hideBackButton() { backButton.setVisibility(View.GONE); }

    private void showBackButton() { backButton.setVisibility(View.VISIBLE); }
}
