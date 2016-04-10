package info.goforus.goforus.apis;

import java.io.IOException;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.put;

public class Sessions {
    public static String loginURI = getLoginUri();


    private static final Sessions sessions = new Sessions();

    private Sessions() {}

    public static String getLoginUri() { return Utils.getBaseUri() + "login"; }

    public static Sessions getInstance() { return sessions; }

    public JSONObject logIn(final String email, final String password) throws JSONException, IOException {
        JSONObject baseJson = new JSONObject();
        JSONObject userData = new JSONObject();

        userData.put("email", email);
        userData.put("password", password);
        baseJson.put("user", userData);

        return Utils.resty.json(loginURI, put(content(baseJson))).object();
    }


    public static final String logoutURI = Utils.getBaseUri() + "logout";

    public JSONObject logOut() throws IOException, JSONException {
        return Utils.resty.json(logoutURI + Utils.tokenParams(), put(content(""))).object();
    }

    public static final String registerURI = Utils.getBaseUri() + "register";

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
