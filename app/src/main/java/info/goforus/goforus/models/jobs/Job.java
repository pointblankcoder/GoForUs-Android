package info.goforus.goforus.models.jobs;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.orders.Order;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

@Table(name = "Jobs")
public class Job extends Model {
    @Column(name = "externalId", index = true) public int externalId;
    @Column(name = "orderId", index = true) public int orderId; // External
    @Column(name = "partnerId", index = true) public int partnerId; // External
    @Column(name = "customerId", index = true) public int customerId; // External
    @Column(name = "accepted") public boolean accepted = false;
    @Column(name = "declined") public boolean declined = false;
    @Column(name = "respondedTo") public boolean respondedTo = false;

    public Job() { super(); }


    public Job(JSONObject job) {
        super();
        try {
            this.externalId = job.getInt("id");
            this.orderId = job.getInt("order_id");
            this.partnerId = job.getInt("partner_id");
            this.customerId = job.getInt("customer_id");
            this.accepted = job.getBoolean("accepted");
            this.declined = job.getBoolean("declined");
            this.respondedTo = job.getBoolean("responded_to");

        } catch (JSONException e) {
            Logger.e(e.toString());
        }
    }

    public static Job findByOrder(Order order) {
        return new Select().from(Job.class).where("partnerId = ? AND orderId = ? AND customerId = ?", Account
                .currentAccount().externalId, order.externalId, order.customerId).executeSingle();
    }

    public static Job findByExternalId(int externalId) {
        return new Select().from(Job.class).where("partnerId = ? AND externalId = ?", Account
                .currentAccount().externalId, externalId).executeSingle();
    }

    public static List<Job> orderedByRecent() {
        return new Select().from(Job.class).where("partnerId = ?", Account.currentAccount().externalId).orderBy("externalId DESC").execute();
    }

    public static int count() {
        return new Select().from(Job.class).where("partnerId = ?", Account.currentAccount().externalId).count();
    }

    public static Job last() {
        return new Select().from(Job.class).where("partnerId = ?", Account.currentAccount().externalId).orderBy("externalId ASC").executeSingle();
    }

    public static Job updateOrCreateFromJson(JSONObject json) {
        Job job =  new Job(json);

        Job existingJob = new Select().from(Job.class)
                                              .where("externalId = ? AND partnerId = ?", job.externalId, Account.currentAccount().externalId)
                                              .executeSingle();
        if (existingJob != null) {

            existingJob.externalId = job.externalId;
            existingJob.declined = job.declined;
            existingJob.accepted = job.accepted;
            existingJob.partnerId = job.partnerId;
            existingJob.customerId = job.customerId;
            existingJob.respondedTo = job.respondedTo;
            existingJob.orderId = job.orderId;
            existingJob.save();

            return existingJob;
        } else {
            job.save();
            return job;
        }
    }

    public static List<Job> updateOrCreateAllFromJson(JSONArray jobsJSON) {
        List<Job> jobs = new ArrayList<>();

        for (int i = 0; i < jobsJSON.length(); i++) {
            try {
                JSONObject jobJSON = jobsJSON.getJSONObject(i);
                Job job = updateOrCreateFromJson(jobJSON);
                jobs.add(job);
            } catch (JSONException e) {
                Logger.e(e.toString());
            }
        }
        return jobs;
    }
}
