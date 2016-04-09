package info.goforus.goforus.event_results;

import java.util.List;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;

public class MessagesUpdateServiceResult {

    int mConversationId;
    List<Message> mMessages;

    public MessagesUpdateServiceResult(int conversationId, List<Message> messages) {
        mConversationId = conversationId;
        mMessages = messages;
    }

    public int getConversationId() { return mConversationId; }

    public List<Message> getMessages() { return mMessages; }
}
