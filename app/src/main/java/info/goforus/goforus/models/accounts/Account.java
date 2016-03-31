package info.goforus.goforus.models.accounts;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcel;

import java.util.List;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;
import info.goforus.goforus.event_results.LocationUpdateServiceResult;
import us.monoid.json.JSONObject;

@Parcel(analyze = Account.class, value = Parcel.Serialization.BEAN)
@Table(name = "Accounts")
public class Account extends Model {
    private static final String TAG = "Account";

    @Column(name = "externalId", index = true, unique = true)
    public Integer externalId;
    @Column(name = "name")
    public String name;
    @Column(name = "email")
    public String email;
    @Column(name = "phoneNumber")
    public String phoneNumber;
    @Column(name = "apiToken")
    public String apiToken;
    @Column(name = "lat")
    public double lat;
    @Column(name = "lng")
    public double lng;

    public Account() {
        super();
        EventBus.getDefault().register(this);
    }

    public Account(JSONObject accountObject) {
        super();

        try {
            this.externalId = Integer.parseInt(accountObject.get("id").toString());
            this.email = accountObject.get("email").toString();
            this.apiToken = accountObject.get("authentication_token").toString();

            if (accountObject.has("mobile_number"))
                this.phoneNumber = accountObject.get("mobile_number").toString();
            if (accountObject.has("name"))
                this.name = accountObject.get("name").toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Account currentAccount() {
        return new Select().from(Account.class).orderBy("id DESC").executeSingle();
    }

    // order our conversations by the ones with the most recent messages within them
    public List<Conversation> conversationsOrderedByRecentMessages() {
        return new Select().from(Conversation.class).as("conversations").
                innerJoin(Message.class).on("Messages.Conversation = conversations.id").
                where("conversations.Account = ?", getId()).groupBy("conversations.id").
                orderBy("Messages.externalId").execute();
    }

    public List<Conversation> conversations() {
        return getMany(Conversation.class, "Account");
    }

    public int conversationsCount() {
        return new Select().from(Conversation.class).where("Account = ?", getId()).count();
    }

    public LatLng location(){
        return new LatLng(lat, lng);
    }

    public void updateLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.save();
    }

    public boolean hasLocation(){
        return lat != 0 && lng != 0;
    }

    @Subscribe
    public void onLocationUpdate(LocationUpdateServiceResult result) {
        if(result.getAccountId() == externalId) {
            updateLocation(result.getLocation().latitude, result.getLocation().longitude);
        }
    }

}
