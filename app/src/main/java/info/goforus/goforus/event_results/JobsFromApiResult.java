package info.goforus.goforus.event_results;

import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.jobs.Job;
import us.monoid.json.JSONArray;

public class JobsFromApiResult {

    List<Job> mJobs = new ArrayList<>();

    public JobsFromApiResult(JSONArray jobsJSON) {
        if (jobsJSON!= null) {
            mJobs = Job.updateOrCreateAllFromJson(jobsJSON);
        }
    }

    public List<Job> getJobs() { return mJobs; }
}