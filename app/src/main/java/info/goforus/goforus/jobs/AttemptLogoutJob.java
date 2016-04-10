package info.goforus.goforus.jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.event_results.LoginFromApiResult;
import info.goforus.goforus.event_results.LogoutFromApiResult;
import info.goforus.goforus.models.accounts.Account;
import us.monoid.json.JSONObject;

public class AttemptLogoutJob extends Job {
    private static final int PRIORITY = 20;


    public AttemptLogoutJob() {
        super(new Params(PRIORITY).requireNetwork());
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        if(Account.currentAccount().isPartner()) {
            Utils.PartnerApi.goOnline(false);
        }
        JSONObject response = Utils.SessionsApi.logOut();
        Account.currentAccount().markAsLoggedOut();

        EventBus.getDefault().post(new LogoutFromApiResult(response));
    }

    @Override
    protected void onCancel(int cancelReason) {
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        JSONObject error = Utils.errorToJson(throwable.getMessage());
        EventBus.getDefault().post(new LogoutFromApiResult(error));
        return RetryConstraint.CANCEL;
    }
}
