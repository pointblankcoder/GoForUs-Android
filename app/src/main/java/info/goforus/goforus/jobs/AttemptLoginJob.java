package info.goforus.goforus.jobs;

import android.renderscript.RenderScript;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.event_results.LoginFromApiResult;
import us.monoid.json.JSONObject;

public class AttemptLoginJob extends Job {
    private static final int PRIORITY = 20;
    private final String email;
    private final String password;


    public AttemptLoginJob(String email, String password) {
        super(new Params(PRIORITY).requireNetwork());
        this.email = email;
        this.password = password;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        JSONObject response = Utils.SessionsApi.logIn(email, password);
        EventBus.getDefault().post(new LoginFromApiResult(response));
    }

    @Override
    protected void onCancel(int cancelReason) {
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        JSONObject error = Utils.errorToJson(throwable.getMessage());
        EventBus.getDefault().post(new LoginFromApiResult(error));
        return RetryConstraint.CANCEL;
    }
}
