package info.goforus.goforus.models.drivers;

import android.graphics.Point;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.nineoldandroids.view.ViewHelper;

import org.parceler.Parcel;

import java.util.List;

import info.goforus.goforus.BaseActivity;
import info.goforus.goforus.MapFragment;
import info.goforus.goforus.R;
import info.goforus.goforus.ViewIdGenerator;

public class Indicator implements View.OnClickListener{
    private BaseActivity mActivity;
    private GoogleMap mMap;
    private MapFragment mapFragment;
    private RelativeLayout arrowContainer;
    private ImageView arrowView;

    protected Driver mDriver;
    protected int viewId;

    private static final String TAG = "Indicator";

    public Indicator(){}

    public Indicator(Driver _driver, BaseActivity activity, GoogleMap map, MapFragment _mapFragment) {
        mActivity = activity;
        mDriver = _driver;
        mMap = map;
        mapFragment = _mapFragment;

        arrowContainer = (RelativeLayout) mActivity.findViewById(R.id.arrowContainer);
        arrowView = createArrowView();
        addIndicator();
    }

    private ImageView createArrowView() {
        viewId = ViewIdGenerator.generateViewId();

        ImageView _arrowView = new ImageView(mActivity);
        _arrowView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.driver_indicator));
        _arrowView.setId(viewId);
        _arrowView.setOnClickListener(this);

        return _arrowView;
    }

    @Override
    public void onClick(View v) {
        mDriver.goTo(1000);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("Press ").append(" ");
        builder.setSpan(new ImageSpan(mActivity, R.drawable.car), builder.length() - 1, builder.length(), 0);
        builder.append(" to find out more about this driver");

        Snackbar.make(v, builder, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void removeIndicator() {
        arrowContainer.removeView(arrowView);
    }

    public void addIndicator() {
        /* TODO:
            addInArray been called, this = android.widget.RelativeLayout{42112af0 V.E..... ......ID 0,0-540,922 #7f0d00a2 app:id/arrowContainer}call stack =
                                                               java.lang.Throwable: addInArray
         */
        arrowContainer.addView(arrowView);
    }

    public void show() {
        arrowView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        arrowView.setVisibility(View.GONE);
    }

    // Run on background thread to take away computation from the UI thread
    public void update() {
        final Projection projection = mMap.getProjection();
        final LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        final LatLng driverLocation = mDriver.location();
        final int arrowViewHeight = arrowView.getHeight();
        final int arrowViewWidth = arrowView.getWidth();
        final int actionBarSize  = mActivity.getActionBarSize();
        final LatLng mapCenter = mMap.getCameraPosition().target;

        new Thread(new Runnable() {
            public void run() {
                // we only want to display the arrow when the driver is not in view

                if (!bounds.contains(driverLocation)) {
                    Point driverPositionOnScreen = projection.toScreenLocation(driverLocation);
                    Point topRightXY = projection.toScreenLocation(bounds.northeast);
                    Point bottomLeftXY = projection.toScreenLocation(bounds.southwest);

                    // Visible top of map, actionbar size, middle point(pivot) of the arrow, arrow padding
                    float minY = (topRightXY.y + actionBarSize + (arrowViewHeight / 2));
                    float maxY = (bottomLeftXY.y) - ( arrowViewHeight/ 2);
                    float targetY = driverPositionOnScreen.y + (actionBarSize - (arrowViewHeight / 2));

                    final float y = ScrollUtils.getFloat(targetY, minY, maxY);

                    float minX = bottomLeftXY.x + (arrowViewWidth / 2);
                    float maxX = topRightXY.x - (arrowViewWidth + (arrowViewWidth / 2));
                    float targetX = driverPositionOnScreen.x - (arrowViewWidth / 2);
                    final float x = ScrollUtils.getFloat(targetX, minX, maxX);
                    final float heading = (float) SphericalUtil.computeHeading(mapCenter, driverLocation);

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewHelper.setY(arrowView, y);
                            ViewHelper.setX(arrowView, x);
                            // Point the arrow towards the driver
                            arrowView.setRotation(heading);

                            show();
                        }
                    });
                } else {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hide();
                        }
                    });
                }
            }
        }).start();
    }
}
