package info.goforus.goforus.jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.event_results.DeclineJobResult;
import info.goforus.goforus.models.orders.Order;
import us.monoid.json.JSONObject;

public class DeclineJobJob extends Job {
    public static final int PRIORITY = 100;
    int jobId;


    public DeclineJobJob(int jobId) {
        super(new Params(PRIORITY).requireNetwork().persist());
        this.jobId = jobId;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        // TODO: add error check here, there are 3-4 potential errors that can occur on the server related to accepting a job
        JSONObject response = Utils.JobsApi.declineJob(jobId);
        info.goforus.goforus.models.jobs.Job job = info.goforus.goforus.models.jobs.Job.findByExternalId(jobId);
        job.respondedTo = true;
        job.accepted = false;
        job.declined = true;
        job.save();

        Order order = Order.findByExternalId(job.orderId);
        order.accepted = false;
        order.respondedTo = true;
        order.save();

        EventBus.getDefault().post(new DeclineJobResult(job));
    }

    @Override
    protected void onCancel(int cancelReason) {}

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
}
