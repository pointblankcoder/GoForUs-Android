package info.goforus.goforus.models.driver;


import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

class DriverInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    // These a both viewgroups containing an ImageView with id "badge" and two TextViews with id
    // "title" and "snippet".
    private final View mWindow;
    private final View mContents;

    DriverInfoWindowAdapter(View mWindow, View mContents) {
        //mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        //mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        this.mWindow = mWindow;
        this.mContents = mContents;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        //if (mOptions.getCheckedRadioButtonId() != R.id.custom_info_window) {
        //    // This means that getInfoContents will be called.
        //    return null;
        //}
        //render(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        //if (mOptions.getCheckedRadioButtonId() != R.id.custom_info_contents) {
        //    // This means that the default info contents will be used.
        //    return null;
        //}
        //render(marker, mContents);
        return mContents;
    }

    private void render(Marker marker, View view) {

        //String title = marker.getTitle();
        //TextView titleUi = ((TextView) view.findViewById(R.id.title));
        //if (title != null) {
        //    // Spannable string allows us to edit the formatting of the text.
        //    SpannableString titleText = new SpannableString(title);
        //    titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
        //    titleUi.setText(titleText);
        //} else {
        //    titleUi.setText("");
        //}

        //String snippet = marker.getSnippet();
        //TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));

        //if (snippet != null && snippet.length() > 12) {
        //    SpannableString snippetText = new SpannableString(snippet);
        //    snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
        //    snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
        //    snippetUi.setText(snippetText);
        //} else {
        //  snippetUi.setText("");
        //}
    }
}