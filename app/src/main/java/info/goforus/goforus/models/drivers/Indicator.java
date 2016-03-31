package info.goforus.goforus.models.drivers;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nineoldandroids.view.ViewHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.eventbus.util.AsyncExecutor;

import info.goforus.goforus.BaseActivity;
import info.goforus.goforus.MapFragment;
import info.goforus.goforus.R;
import info.goforus.goforus.ViewIdGenerator;
import info.goforus.goforus.event_results.CalculateIndicatorUpdate;
import info.goforus.goforus.event_results.IndicatorUpdateResult;

public class Indicator implements View.OnClickListener {
    private BaseActivity mActivity;
    private GoogleMap mMap;
    private MapFragment mapFragment;
    private RelativeLayout arrowContainer;
    private ImageView arrowView;

    protected Driver mDriver;
    protected int viewId;

    private static final String TAG = "Indicator";

    public Indicator() {
    }

    public Indicator(Driver _driver, BaseActivity activity, GoogleMap map, MapFragment _mapFragment) {
        mActivity = activity;
        mDriver = _driver;
        mMap = map;
        mapFragment = _mapFragment;

        arrowContainer = (RelativeLayout) mActivity.findViewById(R.id.arrowContainer);
        arrowView = createArrowView();
        addIndicator();
        EventBus.getDefault().register(this);
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

    public void update(){
        final Projection projection = mMap.getProjection();
        final LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        final LatLng driverLocation = mDriver.location();
        final int arrowViewHeight = arrowView.getHeight();
        final int arrowViewWidth = arrowView.getWidth();
        final int actionBarSize  = mActivity.getActionBarSize();
        final LatLng mapCenter = mMap.getCameraPosition().target;

        if (bounds.contains(driverLocation)) {
            hide();
        } else {
            AsyncExecutor.create().execute(new AsyncExecutor.RunnableEx() {
                @Override
                public void run() throws Exception {
                    EventBus.getDefault().post(new CalculateIndicatorUpdate(
                            projection, bounds, driverLocation, arrowViewHeight,
                            arrowViewWidth, actionBarSize, mapCenter
                    ));
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onCalculatedResult(CalculateIndicatorUpdate update){
        EventBus.getDefault().post(new IndicatorUpdateResult(update.x, update.y, update.heading));
    }

    // Run on background thread to take away computation from the UI thread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateResult(IndicatorUpdateResult result) {
        ViewHelper.setY(arrowView, result.y);
        ViewHelper.setX(arrowView, result.x);
        // Point the arrow towards the driver
        arrowView.setRotation(result.heading);
        show();
    }
}
