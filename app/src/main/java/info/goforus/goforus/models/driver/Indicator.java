package info.goforus.goforus.models.driver;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.orm.dsl.NotNull;

import java.io.Serializable;

import info.goforus.goforus.BaseActivity;
import info.goforus.goforus.R;
import info.goforus.goforus.ViewIdGenerator;

public class Indicator implements View.OnClickListener{
    private BaseActivity mActivity;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private RelativeLayout arrowContainer;
    private ImageView arrowView;

    protected Driver mDriver;
    protected int viewId;

    private static final String TAG = "Indicator";
    private static final Float anchorX = 0.5f;
    private static final Float anchorY = 0.5f;

    public Indicator(Driver _driver, BaseActivity activity, GoogleMap map, SupportMapFragment _mapFragment) {
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

        if (!bounds.contains(mDriver.location())) {
            Projection projection = mMap.getProjection();
            Point screenPosition = projection.toScreenLocation(mDriver.location());

            // Point the arrow towards the driver
            float heading = (float) SphericalUtil.computeHeading(mMap.getCameraPosition().target, mDriver.location());
            arrowView.setRotation(heading);

            // Default out border screen width to be 400x400 just in case we blow up on getting screen width/height
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
            int _x = Math.min((Math.max(20, screenPosition.x)), size.x - 20);
            int _y = Math.min((Math.max((mActivity.getActionBarSize()+ 20), screenPosition.y)), size.y - (-mActivity.getActionBarSize() + 20));
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
