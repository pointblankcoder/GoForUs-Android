package info.goforus.goforus.jobs;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.Application;
import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;

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
            Utils.ConversationsApi.getInbox();
        } else {
            Utils.ConversationsApi.getInboxSinceId(Conversation.last().externalId);
        }

        for(Conversation c : mAccount.conversations()) {
            Application.getInstance().getJobManager().addJobInBackground(new GetMessagesJob(c.externalId));
        }
    }

    @Override
    protected void onCancel(int cancelReason) {}

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
}
