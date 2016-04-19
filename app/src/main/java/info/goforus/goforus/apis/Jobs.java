package info.goforus.goforus.apis;

import java.io.IOException;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.put;

public class Jobs {
    private static Jobs ourInstance = new Jobs();

    public static Jobs getInstance() { return ourInstance; }

    private Jobs() {}


    public static String getJobsUri() { return Utils.getBaseUri() + "jobs"; }

    public static String acceptJobUri(int jobId) {
        return Utils.getBaseUri() + String.format("jobs/%s/accept", jobId);
    }

    public static String declineJobUri(int jobId) {
        return Utils.getBaseUri() + String.format("jobs/%s/decline", jobId);
    }

    public JSONArray getJobs() throws JSONException, IOException {
        return Utils.resty.json(getJobsUri() + Utils.tokenParams()).array();
    }

    public JSONArray getJobsSinceId(int externalId) throws IOException, JSONException {
        return Utils.resty.json(getJobsUri() + Utils.tokenParams() + String
                .format("&since_id=%s", externalId)).array();
    }

    public JSONObject acceptJob(int jobId) throws IOException, JSONException {
        return Utils.resty.json(acceptJobUri(jobId) + Utils.tokenParams(), put(content(""))).object();
    }

    public JSONObject declineJob(int jobId) throws IOException, JSONException {
        return Utils.resty.json(declineJobUri(jobId) + Utils.tokenParams(), put(content(""))).object();
    }
}
