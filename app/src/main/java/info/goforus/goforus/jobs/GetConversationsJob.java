package info.goforus.goforus.jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.GoForUs;
import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.event_results.ConversationsFromApiResult;
import info.goforus.goforus.event_results.LoginFromApiResult;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

public class GetConversationsJob extends Job {
    public static final int PRIORITY = 1;


    public GetConversationsJob() {
        super(new Params(PRIORITY).requireNetwork().persist());
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        Account mAccount = Account.currentAccount();
        int currentConversationsCount = mAccount.conversationsCount();

        if (currentConversationsCount == 0) {
            JSONArray response = Utils.ConversationsApi.getInbox();
            EventBus.getDefault().post(new ConversationsFromApiResult(response));
        } else {
            JSONArray response = Utils.ConversationsApi.getInboxSinceId(Conversation.last().externalId);
            EventBus.getDefault().post(new ConversationsFromApiResult(response));
        }

        for(Conversation c : mAccount.conversations()) {
            GoForUs.getInstance().getJobManager().addJobInBackground(new GetMessagesJob(c.externalId));
        }
    }

    @Override
    protected void onCancel(int cancelReason) {}

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }

    public static class AttemptRegisterJob extends Job {
        private static final int PRIORITY = 20;
        private final String email;
        private final String password;


        public AttemptRegisterJob(String email, String password) {
            super(new Params(PRIORITY).requireNetwork());
            this.email = email;
            this.password = password;
        }

        @Override
        public void onAdded() {
        }

        @Override
        public void onRun() throws Throwable {
            JSONObject response = Utils.SessionsApi.register(email, password);

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
}
