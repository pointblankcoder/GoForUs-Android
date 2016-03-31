package info.goforus.goforus.tasks;

import android.os.Handler;

import org.greenrobot.eventbus.util.AsyncExecutor;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;

public class MessagesUpdateHandler {
    private static final long REPEAT_TIME = 1000;
    private static MessagesUpdateHandler ourInstance = new MessagesUpdateHandler();
    private Conversation mConversation;
    private Message mMessage;

    public static MessagesUpdateHandler getInstance() {
        return ourInstance;
    }
    private MessagesUpdateHandler() {
    }

    Handler mHandler = new Handler();

    public void startUpdates(Conversation conversation){
        mConversation = conversation;
        mHandler.postDelayed(task, REPEAT_TIME);
    }

    public void stopUpdates(){
        mHandler.removeCallbacks(task);
    }

    final Runnable task = new Runnable() {
        @Override
        public void run() {
            AsyncExecutor.create().execute(new AsyncExecutor.RunnableEx() {
                @Override
                public void run() throws Exception {
                    Account account = Account.currentAccount();
                    if (account != null) {
                        if(mConversation.lastMessage() == null){
                            Utils.MessagesApi.getMessages(mConversation);
                        } else {
                            Utils.MessagesApi.getMessagesSince(mConversation, mConversation.lastMessage().externalId);
                        }
                    }
                    mHandler.postDelayed(task, REPEAT_TIME);
                }
            });
        }
    };
}
