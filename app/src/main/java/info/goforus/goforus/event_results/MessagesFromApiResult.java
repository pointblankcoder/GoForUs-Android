package info.goforus.goforus.event_results;


import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;
import us.monoid.json.JSONArray;

public class MessagesFromApiResult {
    int mConversationId;
    List<Message> mMessages = new ArrayList<>();

    public MessagesFromApiResult(JSONArray json, int conversationId) {
        mConversationId = conversationId;
        if (json != null) {
            mMessages = Message.updateOrCreateAllFromJson(json, conversationId);
            if (mMessages.size() > 0) {
                EventBus.getDefault()
                        .post(new MessagesUpdateServiceResult(conversationId, mMessages));
            }
        }
    }

    public List<Message> getMessages() { return mMessages; }

    public int getConversationId() { return mConversationId; }
}
