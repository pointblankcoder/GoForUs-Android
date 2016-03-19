package info.goforus.goforus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ResourceType")
public class AvailabilityActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapLoadedCallback {

    private GoogleMap mMap;
    private SlidingUpPanelLayout mLayout;

    private double mLongitude;
    private double mLatitude;

    public static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final String TAG = "AvailabilityActivity";

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

        // Enable out actionbar
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

        // AVAILABILITY SLIDE UP PANEL
        // =========================================================================================
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        // Set listeners for availability slide menu
        mLayout.addPanelSlideListener(new AvailabilityPanelSlideListener());
        mLayout.setFadeOnClickListener(new AvailabilityFadeOnClickListener(mLayout));
        // Default AnchorPoint of the pullup menu
        mLayout.setAnchorPoint(0.45f);
        // Add drivers to available drivers list
        ArrayAdapter<Driver> availableDrivers = updateAvailableDriversList();

        // GOOGLE MAPS
        // =========================================================================================
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private ArrayAdapter<Driver> updateAvailableDriversList() {
        TextView nameTV = (TextView) findViewById(R.id.name);
        nameTV.setText("Available Drivers (" + String.valueOf(drivers.size()) + ")");

        ListView lv = (ListView) findViewById(R.id.driverList);

        ArrayAdapter<Driver> arrayAdapter = new ArrayAdapter<Driver>(this, android.R.layout.simple_list_item_1, drivers);

        lv.setAdapter(arrayAdapter);
        return arrayAdapter;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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

        // Add all drivers to the map
        for (Driver d : drivers) {
            addCarToMap(d);
            updatePointerToDriver(d);
        }

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                for (Driver d : drivers) {
                    updatePointerToDriver(d);
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

    public void updatePointerToDriver(Driver d) {

        RelativeLayout arrowContainer = (RelativeLayout) findViewById(R.id.arrowContainer);
        ImageView arrowView = null;
        try {
            arrowView = (ImageView) findViewById(d.viewID);
        } catch (ClassCastException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        if (arrowView == null) {
            arrowView = new ImageView(this);
            arrowView.setImageDrawable(getResources().getDrawable(R.drawable.up_arrow));
        }

        // we only want to display the arrow when the driver is not in view
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        if (!bounds.contains(d.location())) {

            Projection projection = mMap.getProjection();
            LatLng markerLocation = d.location();
            Point screenPosition = projection.toScreenLocation(markerLocation);
            // Create a new arrow image to display on the screen
            // Point the arrow towards the driver
            float heading = (float) SphericalUtil.computeHeading(mMap.getCameraPosition().target, d.location());

            arrowView.setRotation(heading);

            Point size = new Point(mapFragment.getView().getMeasuredWidth(), mapFragment.getView().getMeasuredHeight());

            int _x = Math.min((Math.max(20, screenPosition.x)), size.x - 20);
            int _y = Math.min((Math.max(20, screenPosition.y)), size.y - 20);


            Log.d(TAG, "X: " + _x);
            Log.d(TAG, "Y: " + _y);
            arrowView.setX(_x);
            arrowView.setY(_y);

            arrowView.setPivotX(0.5f);
            arrowView.setPivotY(0.5f);


            ViewGroup parent = (ViewGroup) arrowView.getParent();
            if (parent != null) {
                parent.removeView(arrowView);
            }

            arrowContainer.addView(arrowView);
            arrowView.setId((int) d.viewID);
        } else {

            ViewGroup parent = (ViewGroup) arrowView.getParent();
            if (parent != null) {
                parent.removeView(arrowView);
            }
        }
    }

    @Override
    public void onMapLoaded() {
        // Click on the myLocationButton to bring myLocation into center of screen
        View view1 = mapFragment.getView();
        View view2 = (View) view1.findViewById(1).getParent();
        View myLocationButton = view2.findViewById(2);
        myLocationButton.callOnClick();
    }

    private void attemptToEnableGPS() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation for why we need GPS?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // TODO:  an expanation to the user *asynchronously* -- don't bloc this thread waiting for the user's response. After the user sees the explanation, try again to request the permission.
            }
            // Request permission to access fine location and coarse location
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            enableGpsOnMap();
        }

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    private void enableGpsOnMap() {
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableGpsOnMap();
                } else {
                    // We really need that location of yours, request again
                    attemptToEnableGPS();
                }
                return;
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
