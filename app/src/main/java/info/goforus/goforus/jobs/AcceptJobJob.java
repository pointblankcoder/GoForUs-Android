package info.goforus.goforus.jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.event_results.AcceptJobResult;
import info.goforus.goforus.event_results.JobsFromApiResult;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.orders.Order;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

public class AcceptJobJob extends Job {
    public static final int PRIORITY = 100;
    int jobId;


    public AcceptJobJob(int jobId) {
        super(new Params(PRIORITY).requireNetwork().persist());
        this.jobId = jobId;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        // TODO: add error check here, there are 3-4 potential errors that can occur on the server related to accepting a job
        JSONObject response = Utils.JobsApi.acceptJob(jobId);
        info.goforus.goforus.models.jobs.Job job = info.goforus.goforus.models.jobs.Job.findByExternalId(jobId);
        job.respondedTo = true;
        job.accepted = true;
        job.declined = false;
        job.save();
        Order order = Order.findByExternalId(job.orderId);
        order.accepted = true;
        order.respondedTo = true;
        order.save();
        EventBus.getDefault().post(new AcceptJobResult(job));
    }

    @Override
    protected void onCancel(int cancelReason) {}

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
}
