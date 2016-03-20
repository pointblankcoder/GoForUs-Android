package info.goforus.goforus.tasks;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.maps.SupportMapFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import info.goforus.goforus.R;

public class SimulateMyLocationClickTask extends AsyncTask<Object, Void, Void> {
    SupportMapFragment mapFragment;
    AppCompatActivity activity;
    SlidingUpPanelLayout mainLayout;

    @Override
    protected Void doInBackground(Object... params) {
        // This method is running off the UI thread.
        // Safe to stop execution here.
        mapFragment = (SupportMapFragment) params[0];
        activity = (AppCompatActivity) params[1];
        mainLayout = (SlidingUpPanelLayout) activity.findViewById(R.id.sliding_layout);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
        // This method is running on the UI thread.
        // Do not stop thread here, but safe to modify the UI.
    }

    @SuppressWarnings("ResourceType")
    @Override
    protected void onPostExecute(Void v) {
        // Also on UI thread, executed once doInBackground()
        // finishes.

        // Click on the myLocationButton to bring myLocation into center of screen
        View view1 = mapFragment.getView();
        View view2 = (view1 != null ? view1.findViewById(1) : null);
        View parent = (View) (view2 != null ? view1.getParent() : null);
        View myLocationButton = (parent != null ? parent.findViewById(2) : null);

        if (myLocationButton != null)
            myLocationButton.callOnClick();

        // show our slide panel as it's closed by default for better looking loading
        mainLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
}
