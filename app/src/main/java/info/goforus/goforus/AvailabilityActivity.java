package info.goforus.goforus;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Arrays;
import java.util.List;

import info.goforus.goforus.models.driver.Driver;
import info.goforus.goforus.models.driver.DriverIndicator;
import info.goforus.goforus.models.driver.DriverMarker;
import info.goforus.goforus.models.map.MapFragment;
import info.goforus.goforus.models.map.MapWrapperLayout;
import info.goforus.goforus.tasks.SimulateMyLocationClickTask;

public class AvailabilityActivity extends BaseActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private GoogleMap mMap;
    private SlidingUpPanelLayout mLayout;

    private double mLongitude;
    private double mLatitude;

    private static final String TAG = "AvailabilityActivity";

    private AvailabilityActivity self;

    MapFragment mapFragment;

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
        // Add ability to detect drag motions on the map
        mapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private ArrayAdapter<Driver> updateAvailableDriversListView() {
        TextView nameView = (TextView) findViewById(R.id.name);
        if (nameView != null) {
            nameView.setText(getString(R.string.nearby_driver_list_title, String.valueOf(drivers.size())));
        }

        ListView lv = (ListView) findViewById(R.id.driverList);
        ArrayAdapter<Driver> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drivers);

        if (lv != null) {
            lv.setAdapter(arrayAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Driver driver = (Driver) parent.getAdapter().getItem(position);
                    if (driver != null) {
                        driver.goTo();
                    }
                }
            });
        }
        return arrayAdapter;
    }


    @SuppressWarnings("ResourceType")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (true) {
            mMap.setMyLocationEnabled(true);
        }

        // Setup Map UI
        UiSettings mapUISettings = mMap.getUiSettings();
        mapUISettings.setAllGesturesEnabled(true);
        mapUISettings.setCompassEnabled(false);
        mapUISettings.setMapToolbarEnabled(false);
        mapUISettings.setZoomControlsEnabled(false);


        // Add all nearby cars to the map
        for (Driver d : drivers) {
            addDriverToMap(d);
        }
        // Assign an indicator with a nearby drivers
        for (Driver d : drivers) {
            d.addIndicator(new DriverIndicator(d, self, mMap, mapFragment));
        }

        // Setup Adapters / Listeners
        mapFragment.setOnDragListener(new MapWrapperLayout.OnDragListener() {
            @Override
            public void onDrag(MotionEvent motionEvent) {
                for (Driver d : drivers) {
                    if (d.indicator != null) {
                        d.indicator.update();
                    }
                }
            }
        });
        DriverMarker markerClass = new DriverMarker();
        mMap.setInfoWindowAdapter(markerClass);
        mMap.setOnInfoWindowClickListener(markerClass);
        mMap.setOnInfoWindowCloseListener(markerClass);
        mMap.setOnInfoWindowLongClickListener(markerClass);
        mMap.setOnMarkerClickListener(markerClass);
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

    @Override
    public void onMapLoaded() {
        SimulateMyLocationClickTask task = new SimulateMyLocationClickTask();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mapFragment, self);
        } else {
            task.execute(mapFragment, self);
        }
    }

    private void addDriverToMap(Driver driver) {
        driver.addToMap(this, mMap);
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
}
