package info.goforus.goforus.jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.event_results.JobsFromApiResult;
import info.goforus.goforus.models.accounts.Account;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

public class GoOnlineJob extends Job {
    public static final int PRIORITY = 40;
    private final boolean mGoOnline;


    public GoOnlineJob(boolean goOnline) {
        super(new Params(PRIORITY).requireNetwork().persist());
        mGoOnline = goOnline;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        Utils.PartnerApi.goOnline(mGoOnline);
        Account account = Account.currentAccount();
        account.available = true;
        account.online = true;
        account.save();
    }

    @Override
    protected void onCancel(int cancelReason) {}

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {

        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
}
