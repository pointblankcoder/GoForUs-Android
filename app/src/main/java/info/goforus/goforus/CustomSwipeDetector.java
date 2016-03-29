package info.goforus.goforus;


import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.orhanobut.logger.Logger;

class CustomSwipeDetector extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        Logger.i("onFling has been called!");
        final int SWIPE_MIN_DISTANCE = 120;
        final int SWIPE_MAX_OFF_PATH = 250;
        final int SWIPE_THRESHOLD_VELOCITY = 200;
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Logger.i("Right to Left");
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Logger.i("Left to Right");
            }
        } catch (Exception e) {
            // nothing
        }
        return super.onFling(e1, e2, velocityX, velocityY);
    }
}
