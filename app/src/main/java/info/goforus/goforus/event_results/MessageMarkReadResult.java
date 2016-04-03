package info.goforus.goforus.event_results;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;

public class MessageMarkReadResult {
    public static int RESULT_FAILURE = 0;
    public static int RESULT_OK = 1;

    private final int mConversationId;
    private final int mMessageId;

    public MessageMarkReadResult(int conversationId, int messageId){
        mConversationId = conversationId;
        mMessageId = messageId;
    }

    public int getConversationId(){
        return mConversationId;
    }

    public int getMessageId(){
       return mMessageId;
    }
}
