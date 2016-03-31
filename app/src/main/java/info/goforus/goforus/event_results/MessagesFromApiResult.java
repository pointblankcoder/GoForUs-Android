package info.goforus.goforus.event_results;


import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;
import us.monoid.json.JSONArray;

public class MessagesFromApiResult {
    Conversation mConversation;
    List<Message> mMessages = new ArrayList<>();

    public MessagesFromApiResult(JSONArray json, Conversation conversation){
        mConversation = conversation;
        if(json != null) {
            mMessages = Message.findOrCreateAllFromJson(json, conversation);
            if (mMessages.size() > 0) {
                EventBus.getDefault().post(new MessagesUpdateServiceResult(conversation, mMessages));
            }
        }
    }

    public List<Message> getMessages(){
        return mMessages;
    }

    public Conversation getConversation(){
        return mConversation;
    }
}
