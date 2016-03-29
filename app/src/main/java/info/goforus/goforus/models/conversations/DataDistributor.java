package info.goforus.goforus.models.conversations;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.accounts.Account;

public class DataDistributor {
    static final int POLL_INTERVAL = 1000;
    Handler mHandler = new Handler();
    private List<MessagesUpdates> mMessagesListeners = new ArrayList<>();
    private List<ConversationsUpdates> mConversationsListeners = new ArrayList<>();

    private static final DataDistributor mDataDistributor = new DataDistributor();
    private DataDistributor(){}
    public static DataDistributor getInstance(){
        return mDataDistributor;
    }

    /* start: Listeners */
    public interface MessagesUpdates {
        void onMessagesUpdate(List<Message> messages);
    }

    public interface ConversationsUpdates {
        void onConversationsUpdate(List<Conversation> conversations);
    }

    public void addListener(MessagesUpdates listener) {
        mMessagesListeners.add(listener);
    }

    public void addListener(ConversationsUpdates listener) {
        mConversationsListeners.add(listener);
    }
    public void removeListener(MessagesUpdates listener) {
        mMessagesListeners.remove(listener);
    }

    public void removeListener(ConversationsUpdates listener) {
        mConversationsListeners.add(listener);
    }
    /* end: Listeners */


    /* start: Distribution Actions */
    public void startDistribution(){
        mHandler.postDelayed(dataDistributorRunnable, POLL_INTERVAL);
    }
    public void stopDistribution(){
        mHandler.removeCallbacks(dataDistributorRunnable);
    }
    /* end: Distribution Actions */


    /* start: Distribution Technique */
    Runnable dataDistributorRunnable = new Runnable() {
        @Override
        public void run() {
            Account account = Account.currentAccount();
            if(account != null) {
                List<Conversation> conversations = account.conversations();
                updateAllConversationsListeners(conversations);

                for(Conversation conversation : conversations) {
                    List<Message> messages = conversation.messages();
                    updateAllMessagesListeners(messages);
                }
            }
            // If we don't have an account we dont want to queue back up the Distributor
            mHandler.postDelayed(this, POLL_INTERVAL);
        }
    };

    private void updateAllConversationsListeners(List<Conversation> conversations) {
        for(ConversationsUpdates listener : mConversationsListeners) {
            listener.onConversationsUpdate(conversations);
        }
    }

    private void updateAllMessagesListeners(List<Message> messages) {
        for(MessagesUpdates listener : mMessagesListeners) {
            listener.onMessagesUpdate(messages);
        }
    }
    /* end: Distribution Technique */
}
