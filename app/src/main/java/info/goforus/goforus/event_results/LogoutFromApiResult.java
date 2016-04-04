package info.goforus.goforus.event_results;

import us.monoid.json.JSONObject;

public class LogoutFromApiResult {
    final JSONObject mResponse;

    public LogoutFromApiResult(JSONObject response) {
        mResponse = response;
    }

    public JSONObject getResponse(){
        return mResponse;
    }
}

