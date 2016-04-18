package info.goforus.goforus.models.accounts;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;
import info.goforus.goforus.event_results.LocationUpdateServiceResult;
import info.goforus.goforus.models.orders.Order;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

@Parcel(analyze = Account.class, value = Parcel.Serialization.BEAN)
@Table(name = "Accounts")
public class Account extends Model {
    private static final String TAG = "Account";
    public static final String CUSTOMER = "Customer";
    public static final String PARTNER = "Partner";

    @Column(name = "externalId", index = true, unique = true) public Integer externalId;
    @Column(name = "name") public String name;
    @Column(name = "email") public String email;
    @Column(name = "phoneNumber") public String phoneNumber;
    @Column(name = "apiToken") public String apiToken;
    @Column(name = "type") public String type;
    @Column(name = "gcmToken") public String gcmToken;
    @Column(name = "lat") public double lat;
    @Column(name = "lng") public double lng;
    @Column(name = "loggedIn") public boolean loggedIn = false;
    @Column(name = "online") public boolean online = false;
    @Column(name = "available") public boolean available = false;
    @Column(name = "showMapTips") public boolean showMapTips = true;
    @Column(name = "showOrderModeTips") public boolean showOrderModeTips = true;
    @Column(name = "showMiniProfileDriverTip") public boolean showMiniProfileDriverTip = true;


    public Account() {
        super();
        EventBus.getDefault().register(this);
    }

    public Account(JSONObject accountObject) {
        super();
        EventBus.getDefault().register(this);

        try {
            this.externalId = accountObject.getInt("id");
            this.email = accountObject.getString("email");
            this.apiToken = accountObject.getString("authentication_token");
            this.type = accountObject.getString("user_type");
            this.online = accountObject.getBoolean("online");
            this.available = accountObject.getBoolean("available");

            if (accountObject.has("mobile_number"))
                this.phoneNumber = accountObject.getString("mobile_number");
            if (accountObject.has("name")) this.name = accountObject.getString("name");
        } catch (Exception e) {
            Logger.e(e.toString());
        }
    }

    public static Account currentAccount() {
        return new Select().from(Account.class).where("loggedIn = ?", true).orderBy("id DESC")
                           .executeSingle();
    }

    public boolean isPartner() {
        return type.equals(PARTNER);
    }

    public boolean isCustomer() {
        return type.equals(CUSTOMER);
    }

    public static Account findByExternalId(int externalId) {
        return new Select().from(Account.class).where("externalId = ?", externalId).executeSingle();
    }

    public static Account findOrCreateFromApi(JSONObject json) throws JSONException {
        int externalId = json.getInt("id");
        Account existingAccount = Account.findByExternalId(externalId);
        if (existingAccount != null) {
            return existingAccount;
        } else {
            Account newAccount = new Account(json);
            newAccount.externalId = externalId;
            newAccount.save();
            return newAccount;
        }
    }

    public void updateFromApi(JSONObject json) throws JSONException {
        this.externalId = json.getInt("id");
        this.apiToken = json.getString("authentication_token");
        this.name = json.getString("name");
        this.email = json.getString("email");
        this.phoneNumber = json.getString("mobile_number");
        this.type = json.getString("user_type");
        this.online = json.getBoolean("online");
        this.available = json.getBoolean("available");
    }

    // TODO: move this to the conversations model
    // order our conversations by the ones with the most recent messages within them
    public List<Conversation> conversationsOrderedByRecentMessages() {
        return new Select().from(Conversation.class).as("conversations").
                leftJoin(Message.class).on("Messages.Conversation = conversations.id")
                           .where("conversations.Account = ?", getId()).groupBy("conversations.id")
                           .orderBy("Messages.externalId DESC").execute();
    }

    public List<Conversation> conversations() {
        return getMany(Conversation.class, "Account");
    }

    public List<Conversation> conversationsForInbox() {
        List<Conversation> conversations = conversationsOrderedByRecentMessages();
        ListIterator<Conversation> conversationsIterator = conversations.listIterator();
        int myExternalId = Account.currentAccount().externalId;

        List<Conversation> conversationsToReturn = new ArrayList<>();

        while (conversationsIterator.hasNext()) {
            Conversation conversation = conversationsIterator.next();
            Order order = Order.findByConversation(conversation);
            if (order != null && order.partnerId == myExternalId && conversation
                    .messagesCount() != 1) {
                conversationsToReturn.add(conversation);
            } else if (order != null && order.customerId == myExternalId) {
                conversationsToReturn.add(conversation);
            }
        }

        return conversationsToReturn;
    }

    public int conversationsCount() {
        return new Select().from(Conversation.class).where("Account = ?", getId()).count();
    }

    public LatLng location() {
        return new LatLng(lat, lng);
    }

    public void updateLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.save();
    }

    public boolean hasLocation() {
        return lat != 0 && lng != 0;
    }


    public void markAsLoggedOut() {
        this.loggedIn = false;
        this.available = false;
        this.online = false;
        this.save();
    }

    public void markAsLoggedIn() {
        this.loggedIn = true;
        this.save();
    }

    @Subscribe
    public void onLocationUpdate(LocationUpdateServiceResult result) {
        updateLocation(result.getLocation().latitude, result.getLocation().longitude);
    }
}
