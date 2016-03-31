package info.goforus.goforus.services;

import android.app.IntentService;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import info.goforus.goforus.apis.Messages;
import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;
import us.monoid.json.JSONArray;

public class ConversationsUpdateService extends IntentService {
    private Account mAccount;


    public ConversationsUpdateService() {
        super("ConversationsUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mAccount = Account.currentAccount();
        // Handle conversations first so we have our conversations saved before fetching messages
        // for them conversations
        if(mAccount != null) {
            handleConversations();
            handleMessages();
        }
    }

    private void handleConversations() {
        int currentConversationsCount = mAccount.conversationsCount();

        if (currentConversationsCount == 0) {
            Utils.ConversationsApi.getInbox();
        } else {
             Utils.ConversationsApi.getInboxSinceId(Conversation.last().externalId);
        }
    }

    private void handleMessages() {
        int currentConversationsCount = mAccount.conversationsCount();

        if (currentConversationsCount != 0) {
            for(Conversation c : mAccount.conversations()) {
                int currentMessagesCount = c.messagesCount();

                if (currentMessagesCount == 0) {
                    Utils.MessagesApi.getMessages(c);
                } else {
                    Message lastMessageInConversation = c.lastMessage();
                    Utils.MessagesApi.getMessagesSince(c, lastMessageInConversation.externalId);
                }
            }
        }
    }
}
