package info.goforus.goforus.apis;

import com.orhanobut.logger.Logger;

import info.goforus.goforus.apis.listeners.InboxResponseListener;
import info.goforus.goforus.models.conversations.Message;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;

import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.put;

public class Conversation {
    public static final String inboxURI = Utils.BaseURI + "conversations/inbox";
    public static final String replyURI = Utils.BaseURI + "conversations/reply";

    private static final Conversation conversation = new Conversation();

    private Conversation() {
    }

    public static Conversation getInstance() {
        return conversation;
    }

    public void getInbox(InboxResponseListener listener) {
        JSONArray response;
        try {
            response = Utils.resty.json(inboxURI + Utils.tokenParams()).array();
        } catch (Exception e) {
            response = new JSONArray();
        }
        listener.onInboxResponse(response);
    }


    public void sendMessage(final Message messageToSend) {
        new Thread(new Runnable() {
            public void run() {
                JSONResource response;
                JSONObject baseJson = new JSONObject();
                try {
                    baseJson.put("conversation_id", messageToSend.conversation.externalId);
                    baseJson.put("message", messageToSend.body);
                } catch (JSONException e) {
                    Logger.e(e.toString());
                }

                try {
                    response = Utils.resty.json(replyURI + Utils.tokenParams(), put(content(baseJson)));
                } catch (Exception e) {
                    Logger.e(e.toString());
                }
            }
        }).start();
    }
}
