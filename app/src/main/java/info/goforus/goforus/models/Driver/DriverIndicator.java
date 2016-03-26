package info.goforus.goforus.models.driver;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.io.Serializable;

import info.goforus.goforus.R;
import info.goforus.goforus.ViewIdGenerator;

public class DriverIndicator {
    private Activity mActivity;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private RelativeLayout arrowContainer;
    private ImageView arrowView;

    protected Driver driver;
    protected int viewId;

    private static final String TAG = "DriverIndicator";
    private static final Float anchorX = 0.5f;
    private static final Float anchorY = 0.5f;

    public DriverIndicator(Driver _driver, Activity activity, GoogleMap map, SupportMapFragment _mapFragment) {
        mActivity = activity;
        driver = _driver;
        mMap = map;
        mapFragment = _mapFragment;

        arrowContainer = (RelativeLayout) mActivity.findViewById(R.id.arrowContainer);
        arrowView = createArrowView();
        addIndicator();
    }

    private ImageView createArrowView() {
        viewId = ViewIdGenerator.generateViewId();

        ImageView _arrowView = new ImageView(mActivity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            _arrowView.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.up_arrow, mActivity.getTheme()));
        } else {
            //noinspection deprecation
            _arrowView.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.up_arrow));
        }
        _arrowView.setId(viewId);

        return _arrowView;
    }

    public void removeIndicator() {
        arrowContainer.removeView(arrowView);
    }

    public void addIndicator() {
        arrowContainer.addView(arrowView);
    }

    public void show() {
        arrowView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        arrowView.setVisibility(View.GONE);
    }

    public void update() {
        // we only want to display the arrow when the driver is not in view
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        if (!bounds.contains(driver.location())) {
            Projection projection = mMap.getProjection();
            Point screenPosition = projection.toScreenLocation(driver.location());

            // Point the arrow towards the driver
            float heading = (float) SphericalUtil.computeHeading(mMap.getCameraPosition().target, driver.location());
            arrowView.setRotation(heading);

            // Default out border screen width to be 400x400 just incase we blow up on getting screen width/height
            int width = 400;
            int height = 400;
            try {
                //noinspection ConstantConditions
                width = mapFragment.getView().getMeasuredWidth();
                height = mapFragment.getView().getMeasuredHeight();

            } catch (NullPointerException e) {
                e.fillInStackTrace();
            }
            Point size = new Point(width, height);

            int[] actionBarHeightAttr = new int[]{R.attr.actionBarSize};
            int indexOfAttrActionBarHeight = 0;
            TypedArray a = mActivity.obtainStyledAttributes(actionBarHeightAttr);
            int actionBarHeight = a.getDimensionPixelSize(indexOfAttrActionBarHeight, -1);



            int _x = Math.min((Math.max(20, screenPosition.x)), size.x - 20);
            int _y = Math.min((Math.max((actionBarHeight + 20), screenPosition.y)), size.y - (-actionBarHeight + 20));
            arrowView.setX(_x);
            arrowView.setY(_y);

            arrowView.setPivotX(anchorX);
            arrowView.setPivotY(anchorY);

            show();
        } else {
            hide();
        }
    }
}
