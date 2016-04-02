package info.goforus.goforus.event_results;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;

public class MessageMarkReadResult {
    private final Conversation mConversation;
    private final Message mMessage;
    public static int RESULT_FAILURE = 0;
    public static int RESULT_OK = 1;

    public MessageMarkReadResult(Conversation conversation, Message message){
        mConversation = conversation;
        mMessage = message;
    }

    public Conversation getConversation(){
        return mConversation;
    }

    public Message getMessage(){
       return mMessage;
    }
}
