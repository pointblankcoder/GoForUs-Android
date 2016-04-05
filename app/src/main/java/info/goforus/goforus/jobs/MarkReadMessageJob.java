package info.goforus.goforus.jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.GoForUs;
import info.goforus.goforus.R;
import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.event_results.MessageMarkReadResult;
import info.goforus.goforus.event_results.MessageSentResult;

public class MarkReadMessageJob extends Job {
    public static final int PRIORITY = 1;
    private int messageId;
    private int conversationId;


    public MarkReadMessageJob(int conversationId, int messageId) {
        super(new Params(PRIORITY).requireNetwork().persist());
        this.messageId = messageId;
        this.conversationId = conversationId;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        Utils.MessagesApi.markRead(conversationId, messageId);
        EventBus.getDefault().post(new MessageMarkReadResult(conversationId, messageId));
    }

    @Override
    protected void onCancel(int cancelReason) {
        // TODO: Remove the message from waitingConfirmation list in MessagesFragment
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        if(runCount > 10) {
            EventBus.getDefault().post(new MessageSentResult(MessageSentResult.RESULT_FAILURE, GoForUs
                    .getInstance().getString(
                    R.string.difficulty_sending_failure_response)));
        }

        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
}
