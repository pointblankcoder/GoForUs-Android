package info.goforus.goforus.jobs;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.event_results.JobsFromApiResult;
import info.goforus.goforus.event_results.OrdersFromApiResult;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.orders.Order;
import us.monoid.json.JSONArray;

public class GetOrdersJob extends Job {
    public static final int PRIORITY = 50;


    public GetOrdersJob() {
        super(new Params(PRIORITY).requireNetwork().persist());
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        JSONArray response = Utils.OrdersApi.getOrders();
        EventBus.getDefault().post(new OrdersFromApiResult(response));
    }

    @Override
    protected void onCancel(int cancelReason) {}

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }
}
