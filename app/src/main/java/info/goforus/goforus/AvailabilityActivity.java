package info.goforus.goforus;

import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Arrays;
import java.util.List;

import info.goforus.goforus.models.Driver;
import info.goforus.goforus.models.DriverIndicator;
import info.goforus.goforus.tasks.SimulateMyLocationClickTask;

@SuppressWarnings("ResourceType")
public class AvailabilityActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapLoadedCallback {

    private GoogleMap mMap;
    private SlidingUpPanelLayout mLayout;

    private double mLongitude;
    private double mLatitude;

    private static final String TAG = "AvailabilityActivity";

    private AvailabilityActivity self;

    LocationManager mLocationManager;
    SupportMapFragment mapFragment;

    List<Driver> drivers = Arrays.asList(
            new Driver("Tim Westwood", 54.7897645, -1.3481971, ""),
            new Driver("Fat Head (Damo)", 54.7887345, -1.3401970, "")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availability);

        self = this;

        // Enable Top Actionbar
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

        // AVAILABILITY SLIDE UP PANEL
        // =========================================================================================
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new AvailabilityPanelSlideListener());
        mLayout.setFadeOnClickListener(new AvailabilityFadeOnClickListener(mLayout));
        mLayout.setAnchorPoint(0.45f);
        updateAvailableDriversListView();

        // GOOGLE MAPS
        // =========================================================================================
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private ArrayAdapter<Driver> updateAvailableDriversListView() {
        TextView nameTV = (TextView) findViewById(R.id.name);

        if (nameTV != null) {
            nameTV.setText(getString(R.string.available_driver_list_title, String.valueOf(drivers.size())));
        }

        ListView lv = (ListView) findViewById(R.id.driverList);

        ArrayAdapter<Driver> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drivers);

        if (lv != null) {
            lv.setAdapter(arrayAdapter);
        }
        return arrayAdapter;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Start attempting enable GPS and set our current location
        attemptToEnableGPS();
        updateCurrentLocation();

        // Setup Map UI
        UiSettings mapSettings = mMap.getUiSettings();
        mapSettings.setAllGesturesEnabled(true);
        mapSettings.setCompassEnabled(false);
        mapSettings.setMapToolbarEnabled(false);
        mapSettings.setZoomControlsEnabled(false);


        // Add all nearby cars to the map
        for (Driver d : drivers) {
            addCarToMap(d);
        }
        // Assign an indicator with a nearby drivers
        for (Driver d : drivers) {
            d.addIndicator(new DriverIndicator(d, self, mMap, mapFragment));
        }

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

    private void addCarToMap(Driver driver) {
        mMap.addMarker(new MarkerOptions()
                        .position(driver.location())
                        .visible(true)
                        .anchor(0.5f, 0.5f)
                        .title(driver.name)
                        .snippet(driver.short_bio)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
        );
    }

    @Override
    public void onMapLoaded() {
        SimulateMyLocationClickTask task = new SimulateMyLocationClickTask();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mapFragment, self);
        } else {
            task.execute(mapFragment, self);
        }
    }

    private void attemptToEnableGPS() {
        if (PermissionChecker.requireGPS(this)) {
            mMap.setMyLocationEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
        }
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionChecker.LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                } else {
                    attemptToEnableGPS(); // We really need that location of yours, request again
                }
            }
        }
    }

    private void updateCurrentLocation() {
        Criteria criteria = new Criteria();
        String provider = mLocationManager.getBestProvider(criteria, true);
        Location location = mLocationManager.getLastKnownLocation(provider);
        if (location == null) {
            mLocationManager.requestLocationUpdates(provider, 1000, 0, this);
        } else {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //remove location callback
        mLocationManager.removeUpdates(this);
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        MenuItem item = menu.findItem(R.id.action_toggle);
        if (mLayout != null) {
            if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                item.setTitle(R.string.action_show);
            } else {
                item.setTitle(R.string.action_hide);
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggle: {
                if (mLayout != null) {
                    if (mLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        item.setTitle(R.string.action_show);
                    } else {
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        item.setTitle(R.string.action_hide);
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }
}
