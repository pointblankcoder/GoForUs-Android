package info.goforus.goforus.apis.listeners;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;


public interface LoginResponseListener {
    void onLoginResponse(JSONObject response);
}


