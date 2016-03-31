package info.goforus.goforus.tasks;

import android.graphics.Point;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.BaseActivity;
import info.goforus.goforus.event_results.IndicatorUpdateResult;
import info.goforus.goforus.models.drivers.Driver;

public class UpdateIndicatorTask extends AsyncTask<Object, Integer, Float[]> {
    private final GoogleMap mMap;
    private final Driver mDriver;
    private final ImageView mArrowView;
    private final BaseActivity mActivity;

    private LatLng mapCenter;
    private LatLng driverLocation;
    private int arrowViewHeight;
    private int arrowViewWidth;
    private int actionBarSize;
    private Projection projection;
    private LatLngBounds bounds;


    public UpdateIndicatorTask(GoogleMap map, Driver driver, ImageView arrowView, BaseActivity activity){
        mMap = map;
        mDriver = driver;
        mArrowView = arrowView;
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        projection = mMap.getProjection();
        bounds = projection.getVisibleRegion().latLngBounds;
        driverLocation = mDriver.location();
        arrowViewHeight   = mArrowView.getHeight();
        arrowViewWidth    = mArrowView.getWidth();
        actionBarSize     = mActivity.getActionBarSize();
        mapCenter = mMap.getCameraPosition().target;
        if (bounds.contains(driverLocation)) {
            mArrowView.setVisibility(View.GONE);
            cancel(true);
        }
    }

    protected Float[] doInBackground(Object... objects) {
        final Point driverPositionOnScreen = projection.toScreenLocation(driverLocation);
        final Point topRightXY = projection.toScreenLocation(bounds.northeast);
        final Point bottomLeftXY = projection.toScreenLocation(bounds.southwest);

        // Visible top of map, actionbar size, middle point(pivot) of the arrow, arrow padding
        final float minY = (topRightXY.y + actionBarSize + (arrowViewHeight / 2));
        final float maxY = (bottomLeftXY.y) - (arrowViewHeight / 2);
        final float targetY = driverPositionOnScreen.y + (actionBarSize - (arrowViewHeight / 2));

        final float minX = bottomLeftXY.x + (arrowViewWidth / 2);
        final float maxX = topRightXY.x - (arrowViewWidth + (arrowViewWidth / 2));
        final float targetX = driverPositionOnScreen.x - (arrowViewWidth / 2);

        final float y = ScrollUtils.getFloat(targetY, minY, maxY);
        final float x = ScrollUtils.getFloat(targetX, minX, maxX);
        final float heading = (float) SphericalUtil.computeHeading(mapCenter, driverLocation);


        Float[] response = new Float[] {x, y, heading};
        return response;
    }

    protected void onPostExecute(Float[] xyh) {
       EventBus.getDefault().post(new IndicatorUpdateResult(xyh[0], xyh[1], xyh[2]));
    }
}
