package info.goforus.goforus.apis;

import java.io.IOException;

import info.goforus.goforus.apis.listeners.LogoutResponseListener;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.put;

public class Sessions {
    public static final String loginURI = Utils.BaseURI + "login";


    private static final Sessions sessions = new Sessions();

    private Sessions() {
    }

    public static Sessions getInstance() {
        return sessions;
    }

    public JSONObject logIn(final String email, final String password) throws JSONException, IOException {
        JSONObject baseJson = new JSONObject();
        JSONObject customerData = new JSONObject();

        customerData.put("email", email);
        customerData.put("password", password);
        baseJson.put("customer", customerData);

        return Utils.resty.json(loginURI, put(content(baseJson))).object();
    }


    public static final String logoutURI = Utils.BaseURI + "logout";

    public JSONObject logOut() throws IOException, JSONException {
        return Utils.resty.json(logoutURI + Utils.tokenParams(), put(content(""))).object();
    }

    public static final String registerURI = Utils.BaseURI + "register";

    public JSONObject register(String email, String password) throws JSONException, IOException {
        JSONObject response;
        JSONObject baseJson = new JSONObject();
        JSONObject customerData = new JSONObject();

        customerData.put("email", email);
        customerData.put("password", password);
        baseJson.put("customer", customerData);

        response = Utils.resty.json(registerURI, put(content(baseJson))).object();

        return response;
    }
}
