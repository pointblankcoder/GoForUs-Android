package info.goforus.goforus.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.SupportMapFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import info.goforus.goforus.BaseActivity;
import info.goforus.goforus.R;

@SuppressWarnings("ResourceType")
public class SimulateMyLocationClickTask extends AsyncTask<Object, Void, View> {
    SupportMapFragment mapFragment;
    BaseActivity activity;
    SlidingUpPanelLayout mainLayout;
    View myLocationButton;
    boolean readyToClick;

    @Override
    protected View doInBackground(Object... params) {
        // This method is running off the UI thread.
        // Safe to stop execution here.
        mapFragment = (SupportMapFragment) params[0];
        activity = (BaseActivity) params[1];
        mainLayout = (SlidingUpPanelLayout) activity.findViewById(R.id.sliding_layout);

        while (myLocationButton == null) {
            View view1 = mapFragment.getView();
            View view2 = (view1 != null ? view1.findViewById(1) : null);
            View parent = (View) (view2 != null ? view1.getParent() : null);
            myLocationButton = (parent != null ? parent.findViewById(2) : null);
        }

        readyToClick = true;
        return myLocationButton;
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
    }

    @Override
    protected void onPostExecute(View v) {
        while(!v.isClickable() && !v.isShown() && !v.isEnabled()) {}
        v.callOnClick();
    }
}
