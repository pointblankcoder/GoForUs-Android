package info.goforus.goforus.models.drivers;

import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import info.goforus.goforus.BaseActivity;
import info.goforus.goforus.R;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = "InfoWindowAdapter";
    private final View mWindow;

    public InfoWindowAdapter(BaseActivity activity) {
        this.mWindow = activity.getLayoutInflater().inflate(R.layout.driver_info_window_popup, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private void render(Marker marker, View view) {
        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        titleUi.setText(title);
    }
}