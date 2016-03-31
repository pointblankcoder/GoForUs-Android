package info.goforus.goforus.event_results;

import android.graphics.Point;

import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

public class CalculateIndicatorUpdate {
    public float x;
    public float y;
    public float heading;

    public CalculateIndicatorUpdate(
            final Projection projection, final LatLngBounds bounds, final LatLng driverLocation,
            final int arrowViewHeight, final int arrowViewWidth, final int actionBarSize,
            final LatLng mapCenter) {

        Point driverPositionOnScreen = projection.toScreenLocation(driverLocation);
        Point topRightXY = projection.toScreenLocation(bounds.northeast);
        Point bottomLeftXY = projection.toScreenLocation(bounds.southwest);

        // Visible top of map, actionbar size, middle point(pivot) of the arrow, arrow padding
        float minY = (topRightXY.y + actionBarSize + (arrowViewHeight / 2));
        float maxY = (bottomLeftXY.y) - (arrowViewHeight / 2);
        float targetY = driverPositionOnScreen.y + (actionBarSize - (arrowViewHeight / 2));

        float minX = bottomLeftXY.x + (arrowViewWidth / 2);
        float maxX = topRightXY.x - (arrowViewWidth + (arrowViewWidth / 2));
        float targetX = driverPositionOnScreen.x - (arrowViewWidth / 2);

        y = ScrollUtils.getFloat(targetY, minY, maxY);
        x = ScrollUtils.getFloat(targetX, minX, maxX);
        heading = (float) SphericalUtil.computeHeading(mapCenter, driverLocation);
    }
}
