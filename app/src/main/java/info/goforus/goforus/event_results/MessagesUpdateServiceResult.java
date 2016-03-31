package info.goforus.goforus.event_results;

import java.util.List;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;

public class MessagesUpdateServiceResult {

    int mChannelId;
    Conversation mConversation;
    List<Message> mMessages;

    public MessagesUpdateServiceResult(Conversation conversation, List<Message> messages) {
        mConversation = conversation;
        mChannelId = conversation.externalId;
        mMessages = messages;
    }

    public int getChannel() {
        return mChannelId;
    }

    public Conversation getConversation() {
        return mConversation;
    }

    public List<Message> getMessages() {
        return mMessages;
    }
}
