package info.goforus.goforus.jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;

public class GetMessagesJob extends Job {
    public static final int PRIORITY = 1;
    private final int conversationId;


    public GetMessagesJob(int conversationId) {
        super(new Params(PRIORITY).requireNetwork().persist());
        this.conversationId = conversationId;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        Conversation c = Conversation.findByExternalId(conversationId);
        int currentMessagesCount = c.messagesCount();

        if (currentMessagesCount == 0) {
            Utils.MessagesApi.getMessages(c);
        } else {
            Message lastMessageInConversation = c.lastMessage();
            Utils.MessagesApi.getMessagesSince(c, lastMessageInConversation.externalId);
        }
    }

    @Override
    protected void onCancel(int cancelReason) {
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
}
