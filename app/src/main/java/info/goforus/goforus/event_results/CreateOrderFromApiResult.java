package info.goforus.goforus.event_results;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import info.goforus.goforus.GoForUs;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;
import info.goforus.goforus.models.orders.Order;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class CreateOrderFromApiResult {
    Order mOrder;
    JSONObject mResponse;
    Conversation mConversation;

    public CreateOrderFromApiResult(JSONObject response) {
        mResponse = response;

        try {
            Conversation conversation = Conversation.findOrCreateFromJson(response.getJSONObject("conversation"));
            mConversation = conversation;

            ArrayList<Message> messages = new ArrayList<>();
            Message message = Message.updateOrCreateFromJson(response.getJSONObject("message"), mConversation.externalId);
            messages.add(message);
            EventBus.getDefault().post(new MessagesUpdateServiceResult(mConversation.externalId, messages));
        } catch (JSONException e) {
            Logger.e(e.toString() + "\n" + mResponse);
        }
    }

    public Conversation getConversation() { return mConversation; }

    public Order getOrder() { return mOrder; }

    public JSONObject getResponse() { return mResponse; }
}
