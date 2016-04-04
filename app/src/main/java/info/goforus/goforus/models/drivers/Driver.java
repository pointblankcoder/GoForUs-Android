package info.goforus.goforus.models.drivers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import info.goforus.goforus.Application;
import info.goforus.goforus.R;
import us.monoid.json.JSONObject;

public class Driver extends Model implements Comparable<Driver> {
    private static final String TAG = "Driver";
    public static final float markerAnchorY = 0.5f;
    public static final float markerAnchorX = 0.5f;


    @Column(name = "externalId", index = true)
    public Integer externalId;
    @Column(name = "name")
    public String name;
    @Column(name = "email")
    public String email;
    @Column(name = "lat")
    public double lat;
    @Column(name = "lng")
    public double lng;
    @Column(name = "mobileNumber")
    public String mobileNumber;
    @Column(name = "rating")
    public Integer rating = 5;


    public Indicator indicator;
    public Marker marker;
    public GoogleMap map;

    public Driver(){}

    public Driver(JSONObject driverObject) {
        try {
            this.externalId = Integer.parseInt(driverObject.get("id").toString());
            this.name = driverObject.get("name").toString();
            this.email = driverObject.get("email").toString();
            this.lat = Double.parseDouble(driverObject.get("lat").toString());
            this.lng = Double.parseDouble(driverObject.get("lng").toString());
            this.mobileNumber = driverObject.get("mobile_number").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public LatLng location() {
        return new LatLng(lat, lng);
    }

    public String toString() {
        return name;
    }

    public void addToMap(GoogleMap map) {
        this.map = map;
        Drawable car = ActivityCompat.getDrawable(Application.getInstance(), R.drawable.car_black_36dp);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(car.getIntrinsicWidth(), car.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        car.setBounds(0, 0, car.getIntrinsicWidth(), car.getIntrinsicHeight());
        car.draw(canvas);

        marker = map.addMarker(new MarkerOptions()
                        .position(location())
                        .visible(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .anchor(markerAnchorX, markerAnchorY)
                        .title(name)
        );
    }

    public void goToWithInfoWindow() {
        if (marker != null) {
            // TODO: Add actual calculation based on the info window height
            LatLng latLngPositionWithInfoWindow = new LatLng(marker.getPosition().latitude + 0.0022f, marker.getPosition().longitude);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngPositionWithInfoWindow, 15);
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

    public void goTo() {
        if (marker != null) {
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

    /* ================== Class Conversions  =======================*/
    public Information toDriverInformation(){
        return new Information(this);
    }

    /* =================== Custom Lookups/Comparators =============== */
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
            if (object.marker != null){
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
        if(map != null){
            marker.setPosition(new LatLng(lat, lng));
        }
    }
}
