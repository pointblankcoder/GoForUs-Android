package info.goforus.goforus.event_results;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;

public class MessageMarkReadResult {
    private final int mConversationId;
    private final Message mMessage;
    public static int RESULT_FAILURE = 0;
    public static int RESULT_OK = 1;

    public MessageMarkReadResult(int conversationId, Message message){
        mConversationId = conversationId;
        mMessage = message;
    }

    public int getConversationId(){
        return mConversationId;
    }

    public Message getMessage(){
       return mMessage;
    }
}
