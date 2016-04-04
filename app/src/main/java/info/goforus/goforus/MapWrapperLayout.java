package info.goforus.goforus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class MapWrapperLayout extends FrameLayout {

    private GestureListener mGestureListener;

    public interface GestureListener {
        void onDrag(MotionEvent motionEvent);
        void onFling();
    }
    public MapWrapperLayout(Context context) {
        super(context);
    }

    public MapWrapperLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public MapWrapperLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    GestureDetector mDetector;
    GestureDetector.OnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mGestureListener.onFling();

            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                return true;
            }
            return false;
        }
    };


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mGestureListener != null) {
            mGestureListener.onDrag(event);
        }
        return super.dispatchTouchEvent(event);
    }

    public void setGestureListener(GestureListener mGestureListener) {
        this.mGestureListener = mGestureListener;
        mDetector = new GestureDetector(getContext(), listener);
    }
}

