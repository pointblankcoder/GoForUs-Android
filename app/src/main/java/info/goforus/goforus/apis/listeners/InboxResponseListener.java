package info.goforus.goforus.apis.listeners;

import us.monoid.json.JSONArray;

public interface InboxResponseListener{
    void onInboxResponse(JSONArray response);
}
