package info.goforus.goforus.models.orders;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;

@Table(name = "Orders")
public class Order extends Model {

    @Column(name = "externalId", index = true) public int externalId;
    @Column(name = "partnerId", index = true) public int partnerId; // External
    @Column(name = "customerId", index = true) public int customerId; // External
    @Column(name = "conversationId") public long conversationId; // External
    @Column(name = "estimatedCost") public float estimatedCost;
    @Column(name = "finalCost") public float finalCost;
    @Column(name = "accepted") public boolean accepted = false;
    @Column(name = "inProgress") public boolean inProgress = false;
    @Column(name = "description") public String description;

    @Column(name = "pickupLocationLat") public double pickupLocationLat;
    @Column(name = "pickupLocationLng") public double pickupLocationLng;

    @Column(name = "dropOffLocationLat") public double dropOffLocationLat;
    @Column(name = "dropOffLocationLng") public double dropOffLocationLng;

    public Order() {
        super();
    }

    public static Order lastAwaitingConfirmed(int partnerId) {
        return new Select().from(Order.class)
                           .where("partnerId = ? AND accepted = ? AND customerId = ?", partnerId, false, Account
                                   .currentAccount().externalId).executeSingle();
    }

    public static Order findByConversation(Conversation conversation) {
        return new Select().from(Order.class).where("customerId = ? AND conversationId = ?", Account
                .currentAccount().externalId, conversation.externalId).executeSingle();
    }
}
