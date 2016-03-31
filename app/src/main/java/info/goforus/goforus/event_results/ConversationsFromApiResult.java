package info.goforus.goforus.event_results;

import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;
import us.monoid.json.JSONArray;

public class ConversationsFromApiResult {

    List<Conversation> mConversations = new ArrayList<>();

    public ConversationsFromApiResult(JSONArray conversationsJSON) {
        if (conversationsJSON != null) {
            mConversations = Conversation.findOrCreateAllFromJson(conversationsJSON);
        }
    }

    public List<Conversation> getConversations() {
        return mConversations;
    }
}