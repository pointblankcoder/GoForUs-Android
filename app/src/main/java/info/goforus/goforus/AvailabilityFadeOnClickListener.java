package info.goforus.goforus;

import android.view.View;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class AvailabilityFadeOnClickListener implements View.OnClickListener {
    SlidingUpPanelLayout mLayout;

    public AvailabilityFadeOnClickListener(SlidingUpPanelLayout layout) {
        mLayout = layout;
    }

    @Override
    public void onClick(View view) {
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
}
