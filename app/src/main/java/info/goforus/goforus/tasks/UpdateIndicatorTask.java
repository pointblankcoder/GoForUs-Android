package info.goforus.goforus.tasks;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.GoForUs;
import info.goforus.goforus.R;
import info.goforus.goforus.event_results.IndicatorUpdateResult;
import info.goforus.goforus.models.drivers.Driver;

public class UpdateIndicatorTask extends AsyncTask<Object, Integer, Float[]> {
    private final GoogleMap mMap;
    private final Driver mDriver;
    private final ImageView mArrowView;

    private LatLng mapCenter;
    private LatLng driverLocation;
    private Projection projection;
    private LatLngBounds bounds;


    public UpdateIndicatorTask(GoogleMap map, Driver driver, ImageView arrowView) {
        mMap = map;
        mDriver = driver;
        mArrowView = arrowView;
    }

    @Override
    protected void onPreExecute() {
        projection = mMap.getProjection();
        bounds = projection.getVisibleRegion().latLngBounds;
        driverLocation = mDriver.location();

        if (bounds.contains(driverLocation)) {
            mArrowView.setVisibility(View.GONE);
            cancel(true);
        }

        mapCenter = mMap.getCameraPosition().target;
    }

    protected Float[] doInBackground(Object... objects) {
        final GoForUs goForUs = GoForUs.getInstance();
        final Resources resources = GoForUs.getInstance().getResources();
        final BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, R.drawable.ic_navigation_black_36dp, opt);

        final int arrowHeight = opt.outHeight;
        final int arrowWidth = opt.outWidth;
        int actionBarSize = 0;
        TypedValue tv = new TypedValue();

        if (goForUs.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarSize = TypedValue.complexToDimensionPixelSize(tv.data, resources.getDisplayMetrics());
        }

        final Point driverPositionOnScreen = projection.toScreenLocation(driverLocation);
        final Point topRightXY = projection.toScreenLocation(bounds.northeast);
        final Point bottomLeftXY = projection.toScreenLocation(bounds.southwest);

        // Visible top of map, actionbar size, middle point(pivot) of the arrow, arrow padding
        final float minY = (topRightXY.y + actionBarSize + (arrowHeight / 2));
        final float maxY = (bottomLeftXY.y) - (arrowHeight / 2);
        final float targetY = driverPositionOnScreen.y + (actionBarSize - (arrowHeight / 2));

        final float minX = bottomLeftXY.x + (arrowWidth / 2);
        final float maxX = topRightXY.x - (arrowWidth + (arrowWidth / 2));
        final float targetX = driverPositionOnScreen.x - (arrowWidth / 2);

        final float y = ScrollUtils.getFloat(targetY, minY, maxY);
        final float x = ScrollUtils.getFloat(targetX, minX, maxX);
        final float heading = (float) SphericalUtil.computeHeading(mapCenter, driverLocation);


        Float[] response = new Float[]{x, y, heading};
        return response;
    }

    protected void onPostExecute(Float[] xyh) {
        EventBus.getDefault().post(new IndicatorUpdateResult(xyh[0], xyh[1], xyh[2], mArrowView.getId()));
    }
}
