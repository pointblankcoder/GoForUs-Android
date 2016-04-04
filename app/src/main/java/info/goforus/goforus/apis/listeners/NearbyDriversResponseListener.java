package info.goforus.goforus.apis.listeners;

import us.monoid.json.JSONArray;

public interface NearbyDriversResponseListener {
    void onResponse(JSONArray response);
}
