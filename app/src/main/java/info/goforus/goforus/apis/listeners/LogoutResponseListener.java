package info.goforus.goforus.apis.listeners;

import us.monoid.json.JSONObject;

public interface LogoutResponseListener {
    void onLogoutResponse(JSONObject response);
}
