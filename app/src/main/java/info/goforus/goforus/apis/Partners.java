package info.goforus.goforus.apis;

import java.io.IOException;

import info.goforus.goforus.models.orders.Order;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.put;

public class Partners {
    private static Partners ourInstance = new Partners();
    public static Partners getInstance() { return ourInstance; }
    private Partners() {}

    public static String onlineUri() { return Utils.getBaseUri() + "partners/online"; }

    public JSONObject goOnline(boolean goOnline) throws JSONException, IOException {
        JSONObject response;
        JSONObject baseJson = new JSONObject();
        baseJson.put("online", goOnline);

        response = Utils.resty.json(onlineUri() + Utils.tokenParams(), put(content(baseJson))).object();

        return response;
    }
}
