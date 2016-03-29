package info.goforus.goforus.tasks;

import android.os.Handler;
import com.orhanobut.logger.Logger;

import java.util.List;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.apis.listeners.InboxResponseListener;
import info.goforus.goforus.apis.listeners.NearbyDriversResponseListener;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;
import us.monoid.json.JSONArray;

public class ConversationsUpdatesTask implements Runnable {
    private static final String TAG = "ConversationsUpdatesTask";

    private static Handler mHandler;
    private static int mInterval;

    public ConversationsUpdatesTask(Handler handler, int intervalTimeInMilliseconds) {
        mHandler = handler;
        mInterval = intervalTimeInMilliseconds;
    }

    @Override
    public void run(){
        Account account = Account.currentAccount();
        if (account != null) {
            try {
                Utils.ConversationsApi.getInbox(new InboxResponseListener() {
                    @Override
                    public void onInboxResponse(JSONArray response) {
                       Conversation.findOrCreateAllFromJson(response);
                    }
                });
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
        mHandler.postDelayed(this, mInterval);
    }
}
