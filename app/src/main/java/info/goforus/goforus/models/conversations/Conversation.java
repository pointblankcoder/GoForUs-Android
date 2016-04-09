package info.goforus.goforus.models.conversations;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.orhanobut.logger.Logger;

import org.parceler.Parcel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.accounts.Account;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

@Parcel(Parcel.Serialization.BEAN)
@Table(name = "Conversations")
public class Conversation extends Model {
    @Column(name = "externalId", index = true, unique = true)  public int externalId;
    @Column(name = "partnerId", index = true) public int partnerId;
    @Column(name = "customerId", index = true) public int customerId;
    @Column(name = "Account", index = true)  public long account;

    public Conversation() {
        super();
    }

    public Conversation(JSONObject conversation) {
        super();

        try {
            this.externalId = conversation.getInt("id");
            this.customerId = conversation.getInt("customer_id");
            this.partnerId = conversation.getInt("partner_id");
            this.account = Account.currentAccount().getId();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused") // This is used internally for
    private void setId(Parcel in) {
        try {
            Field idField = Model.class.getDeclaredField("mId");
            idField.setAccessible(true);
            idField.set(this, in.value());
        } catch (Exception e) {
            throw new RuntimeException("Reflection failed to get the Active Android ID", e);
        }
    }

    public static Conversation last() {
        return new Select().from(Conversation.class).orderBy("externalId DESC").executeSingle();
    }

    // Finds existing Message based on remoteId or creates new user and returns
    public static Conversation findOrCreateFromJson(JSONObject json) {
        int externalId = 0;

        try {
            externalId = json.getInt("id");
        } catch (JSONException e) {
            Logger.e(e.toString());
        }

        Conversation existingConversation =
                new Select().from(Conversation.class).where("externalId = ?", externalId).executeSingle();
        if (existingConversation != null) {
            return existingConversation;
        } else {
            Conversation conversation = new Conversation(json);
            conversation.save();
            return conversation;
        }
    }

    public static List<Conversation> findOrCreateAllFromJson(JSONArray response) {
        List<Conversation> conversations = new ArrayList<>();

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject conversationJson = response.getJSONObject(i);
                Conversation conversation = Conversation.findOrCreateFromJson(conversationJson);
                conversations.add(conversation);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return conversations;
    }

    public static Conversation findByExternalId(int externalId) {
        return new Select().from(Conversation.class).where("externalId = ?", externalId).executeSingle();
    }

    public List<Message> messages() {
        return new Select().from(Message.class).where("Conversation = ?", getId()).orderBy("externalId ASC").execute();
    }

    public int messagesCount() {
        return new Select().from(Message.class).where("Conversation = ?", getId()).count();
    }

    public Message lastMessage() {
        return new Select().from(Message.class).where("Conversation = ? AND confirmedReceived = ?", getId(), true).orderBy("externalId DESC").executeSingle();
    }

    public Message firstMessage() {
        return new Select().from(Message.class).where("Conversation = ? AND confirmedReceived = ?", getId(), true).orderBy("externalId ASC").executeSingle();
    }

    public int unreadMessageCount() {
        return new Select().from(Message.class).where("Conversation = ? AND isRead = ? AND isMe = ?", getId(), false, false).count();
    }

    public static int totalUnreadMessagesCount() {
        int count = 0;
        for (Conversation c : Account.currentAccount().conversations()) {
            for (Message m : c.messages()) {
                if (!m.isRead && !m.isMe)
                    count++;
            }
        }
        return count;
    }

}
