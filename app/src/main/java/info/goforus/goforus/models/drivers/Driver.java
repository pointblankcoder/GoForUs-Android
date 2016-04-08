package info.goforus.goforus.models.drivers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.orhanobut.logger.Logger;

import java.util.List;

import info.goforus.goforus.BaseActivity;
import info.goforus.goforus.GoForUs;
import info.goforus.goforus.R;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

@Table(name = "Drivers")
public class Driver extends Model implements Comparable<Driver> {
    private static final String TAG = "Driver";
    public static final float markerAnchorY = 0.5f;
    public static final float markerAnchorX = 0.5f;


    @Column(name = "externalId", index = true) public Integer externalId;
    @Column(name = "name") public String name;
    @Column(name = "email") public String email;
    @Column(name = "lat") public double lat;
    @Column(name = "lng") public double lng;
    @Column(name = "mobileNumber") public String mobileNumber;
    @Column(name = "rating") public Integer rating = 5;
    @Column(name = "online") public boolean online = false;
    @Column(name = "available") public boolean available = false;


    public Indicator indicator;
    public Marker marker;
    public GoogleMap map;

    public Driver() {}

    public Driver(JSONObject driverObject) {
        try {
            this.externalId = driverObject.getInt("id");
            this.name = driverObject.getString("name");
            this.email = driverObject.getString("email");
            this.lat = driverObject.getDouble("lat");
            this.lng = driverObject.getDouble("lng");
            this.available = driverObject.getBoolean("available");
            this.online = driverObject.getBoolean("online");
            this.mobileNumber = driverObject.get("mobile_number").toString();
        } catch (Exception e) {
            Logger.e(e.toString());
        }
    }


    public static Driver findByExternalId(Integer externalId) {
        return new Select().from(Driver.class).where("externalId = ?", externalId).executeSingle();
    }

    public static Driver updateOrCreateFromJson(JSONObject json) {
        int externalId = 0;

        try {
            externalId = json.getInt("id");
        } catch (JSONException e) {
            Logger.e(e.toString());
        }

        Driver existingDriver = new Select().from(Driver.class).where("externalId = ?", externalId)
                                            .executeSingle();
        if (existingDriver != null) {
            Driver driver = new Driver(json);
            existingDriver.lat = driver.lat;
            existingDriver.lng = driver.lng;
            if (existingDriver.name != driver.name) existingDriver.name = driver.name;
            if (existingDriver.available != driver.available)
                existingDriver.available = driver.available;
            if (existingDriver.online != driver.online) existingDriver.online = driver.online;
            existingDriver.save();


            try {
                JSONArray vehiclesJSON = json.getJSONArray("vehicles");
                for (int i = 0; i < vehiclesJSON.length(); i++) {
                    Vehicle vehicle = Vehicle
                            .updateOrCreateFromJson((JSONObject) vehiclesJSON.get(i));
                    vehicle.driver = existingDriver.getId();
                    vehicle.save();
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }
            return existingDriver;
        } else {
            Driver driver = new Driver(json);
            driver.save();

            try {
                JSONArray vehiclesJSON = json.getJSONArray("vehicles");
                for (int i = 0; i < vehiclesJSON.length(); i++) {
                    JSONObject vehicleJSON = (JSONObject) vehiclesJSON.get(i);
                    Vehicle vehicle = new Vehicle(vehicleJSON);
                    vehicle.driver = driver.getId();
                    vehicle.save();
                }
            } catch (Exception e) {
                Logger.e(e.toString());
            }

            return driver;
        }
    }

    public Vehicle getCurrentVehicle() {
        return new Select().from(Vehicle.class)
                           .where("Driver = ? AND onlineWith = ?", getId(), true).executeSingle();
    }

    public LatLng location() { return new LatLng(lat, lng); }

    public String toString() {
        return name;
    }

    public void addToMap(GoogleMap map) {
        this.map = map;
        Drawable vehicleIcon;
        if (getCurrentVehicle() != null) {
            if (getCurrentVehicle().vehicleType.equals(Vehicle.STANDARD_CAR)) {
                vehicleIcon = ActivityCompat
                        .getDrawable(GoForUs.getInstance(), R.drawable.car_black_36dp);
            } else if (getCurrentVehicle().vehicleType.equals(Vehicle.SCOOTER)) {
                vehicleIcon = ActivityCompat.getDrawable(GoForUs
                        .getInstance(), R.drawable.ic_directions_bike_black_36dp);
            } else {
                vehicleIcon = ActivityCompat
                        .getDrawable(GoForUs.getInstance(), R.drawable.car_black_36dp);
            }

            Canvas canvas = new Canvas();
            Bitmap bitmap = Bitmap.createBitmap(vehicleIcon.getIntrinsicWidth(), vehicleIcon
                    .getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            vehicleIcon.setBounds(0, 0, vehicleIcon.getIntrinsicWidth(), vehicleIcon
                    .getIntrinsicHeight());
            vehicleIcon.draw(canvas);

            marker = map.addMarker(new MarkerOptions().position(location()).visible(true)
                                                      .icon(BitmapDescriptorFactory
                                                              .fromBitmap(bitmap))
                                                      .anchor(markerAnchorX, markerAnchorY)
                                                      .title(name));
        }
    }

    public void goToWithInfoWindow() {
        if (marker != null) {
            updatePositionOnMap();
            map.setInfoWindowAdapter(new InfoWindowAdapter((BaseActivity) GoForUs.getInstance()
                                                                                 .getCurrentActivity(), this));

            LatLng latLngPositionWithInfoWindow = new LatLng(marker
                    .getPosition().latitude + 0.0022f, marker.getPosition().longitude);
            CameraUpdate cameraUpdate = CameraUpdateFactory
                    .newLatLngZoom(latLngPositionWithInfoWindow, 15);
            map.animateCamera(cameraUpdate, 1, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                }

                @Override
                public void onCancel() {
                }
            });
            marker.showInfoWindow();
        }
    }

    public void goToWithInfoWindow(int animationTimer) {
        if (marker != null) {
            updatePositionOnMap();
            map.setInfoWindowAdapter(new InfoWindowAdapter((BaseActivity) GoForUs.getInstance()
                                                                                 .getCurrentActivity(), this));

            LatLng latLngPositionWithInfoWindow = new LatLng(marker
                    .getPosition().latitude + 0.0022f, marker.getPosition().longitude);
            CameraUpdate cameraUpdate = CameraUpdateFactory
                    .newLatLngZoom(latLngPositionWithInfoWindow, 15);
            map.animateCamera(cameraUpdate, animationTimer, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                }

                @Override
                public void onCancel() {
                }
            });
            marker.showInfoWindow();
        }
    }

    public void goTo() {
        if (marker != null) {
            updatePositionOnMap();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15);
            map.animateCamera(cameraUpdate, 1, new GoogleMap.CancelableCallback() {
                public void onFinish() {
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }

    public void goTo(int animationTime) {
        if (marker != null) {
            updatePositionOnMap();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15);
            map.animateCamera(cameraUpdate, animationTime, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }

    public Information toDriverInformation() {
        return new Information(this);
    }

    public static boolean containsId(List<Driver> list, long id) {
        for (Driver object : list) {
            if (object.externalId == id) {
                return true;
            }
        }
        return false;
    }

    public static Driver findByDriverMarker(List<Driver> list, Marker marker) {
        for (Driver object : list) {
            if (object.marker != null) {
                if (object.marker.equals(marker)) {
                    return object;
                }
            }
        }
        return null;
    }

    public static Integer getIndexOfDriverFrom(List<Driver> list, long id) {
        for (Driver object : list) {
            if (object.externalId == id) {
                return list.indexOf(object);
            }
        }
        return -1;
    }

    @Override
    public int compareTo(@NonNull Driver another) {
        return this.externalId.compareTo(another.externalId);
    }

    public void updatePositionOnMap() {
        if (map != null) {
            marker.setPosition(new LatLng(lat, lng));
        }
    }

}
