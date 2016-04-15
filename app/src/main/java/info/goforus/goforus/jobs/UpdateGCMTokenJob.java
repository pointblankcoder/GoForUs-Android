package info.goforus.goforus.jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.event_results.LoginFromApiResult;
import us.monoid.json.JSONObject;

public class UpdateGCMTokenJob extends Job {
    private static final int PRIORITY = 20;
    private final int userId;
    private final String gcmToken;


    public UpdateGCMTokenJob(int userId, String gcmToken) {
        super(new Params(PRIORITY).requireNetwork());
        this.userId = userId;
        this.gcmToken = gcmToken;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        Utils.SessionsApi.updateGcmToken(userId, gcmToken);
    }

    @Override
    protected void onCancel(int cancelReason) {
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 500);
    }
}
