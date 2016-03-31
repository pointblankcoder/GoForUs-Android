package info.goforus.goforus.apis.listeners;

import info.goforus.goforus.models.conversations.Conversation;
import us.monoid.json.JSONArray;

public interface MessagesResponseListener {
    void onMessagesResponse(JSONArray response, Conversation conversation);
}
