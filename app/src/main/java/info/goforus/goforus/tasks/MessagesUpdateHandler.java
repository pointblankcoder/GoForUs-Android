package info.goforus.goforus.tasks;

import android.os.Handler;

import info.goforus.goforus.Application;
import info.goforus.goforus.jobs.GetMessagesJob;

public class MessagesUpdateHandler {
    private static final long REPEAT_TIME = 10_000;
    private static MessagesUpdateHandler ourInstance = new MessagesUpdateHandler();
    private int mConversationId;

    public static MessagesUpdateHandler getInstance() {
        return ourInstance;
    }

    private MessagesUpdateHandler() {
    }

    Handler mHandler = new Handler();

    public void startUpdates(int conversationId){
        mConversationId = conversationId;
        mHandler.postDelayed(task, REPEAT_TIME);
    }

    public void stopUpdates(){
        mHandler.removeCallbacks(task);
    }

    final Runnable task = new Runnable() {
        @Override
        public void run() {
            Application.getInstance().getJobManager().addJobInBackground(new GetMessagesJob(mConversationId));
            mHandler.postDelayed(task, REPEAT_TIME);
        }
    };
}
