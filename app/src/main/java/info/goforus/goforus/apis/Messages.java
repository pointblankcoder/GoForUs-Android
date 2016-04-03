package info.goforus.goforus.apis;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.event_results.MessageMarkReadResult;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;
import info.goforus.goforus.event_results.MessagesFromApiResult;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;

import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.put;

public class Messages {
    private static final Messages messages = new Messages();

    private Messages() {
    }

    public static Messages getInstance() {
        return messages;
    }

    public JSONArray getMessages(final int conversationId) {
        JSONArray response = null;

        try {
            String uri = String.format("%s/%s/messages%s", Conversations.conversationsURI, conversationId, Utils.tokenParams());
            response = Utils.resty.json(uri).array();
        } catch (Exception e) {
            Logger.e(e.toString());
        }

        return response;
    }

    public JSONArray getMessagesSince(int conversationId, final int sinceId) {
        JSONArray response = null;

        try {
            String uri = String.format("%s/%s/messages%s&since_id=%s", Conversations.conversationsURI, conversationId, Utils.tokenParams(), sinceId);
            JSONResource _response = Utils.resty.json(uri);
            response = _response.array();
        } catch (Exception e) {
            Logger.e(e.toString());
        }

        return response;
    }

    public JSONObject markRead(int conversationId, final Message message) {
        JSONObject response = null;
        try {
            String uri = String.format("%s/%s/messages/%s/mark_read%s", Conversations.conversationsURI, conversationId, message.externalId, Utils.tokenParams());
            response = Utils.resty.json(uri, put(content(new JSONObject("{}")))).object();
        } catch (Exception e) {
            Logger.e(e.toString());
        }

        EventBus.getDefault().post(new MessageMarkReadResult(conversationId, message));
        return response;
    }
}
