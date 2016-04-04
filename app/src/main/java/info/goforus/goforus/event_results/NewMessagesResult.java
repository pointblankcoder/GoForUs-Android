package info.goforus.goforus.event_results;
import java.util.BitSet;
import java.util.List;

import info.goforus.goforus.models.conversations.Message;

public class NewMessagesResult {
    private final List<Message> mNewMessages;
    private BitSet messages;

    public NewMessagesResult(List<Message> newMessages) {
        mNewMessages = newMessages;
    }

    public List<Message> getNewMessages() {
        return mNewMessages;
    }
}

