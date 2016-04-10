package info.goforus.goforus.jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.event_results.JobsFromApiResult;
import info.goforus.goforus.models.accounts.Account;
import us.monoid.json.JSONArray;

public class GetJobsJob extends Job {
    public static final int PRIORITY = 50;


    public GetJobsJob() {
        super(new Params(PRIORITY).requireNetwork().persist());
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        Account mAccount = Account.currentAccount();
        int currentJobsCount = info.goforus.goforus.models.jobs.Job.count();

        if (currentJobsCount == 0) {
            JSONArray response = Utils.JobsApi.getJobs();
            EventBus.getDefault().post(new JobsFromApiResult(response));
        } else {
            JSONArray response = Utils.JobsApi
                    .getJobsSinceId(info.goforus.goforus.models.jobs.Job.last().externalId);
            EventBus.getDefault().post(new JobsFromApiResult(response));
        }
    }

    @Override
    protected void onCancel(int cancelReason) {}

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
}
