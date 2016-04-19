package info.goforus.goforus.jobs;

import com.activeandroid.query.Select;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.event_results.CreateOrderFromApiResult;
import info.goforus.goforus.event_results.LogoutFromApiResult;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.orders.Order;
import us.monoid.json.JSONObject;

public class PostOrderJob extends Job {
    private static final int PRIORITY = 40;
    private final long orderId;


    public PostOrderJob(long orderId) {
        super(new Params(PRIORITY).requireNetwork());
        this.orderId = orderId;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        Order order = Order.load(Order.class, orderId);
        if (order == null) {
            Logger.e("Can't find the Order... orderID = %s", orderId);
        } else {
            JSONObject response = Utils.OrdersApi.postOrder(order);
            Order.updateOrderFromPost(order, response);
            EventBus.getDefault().post(new CreateOrderFromApiResult(response));
        }
    }

    @Override
    protected void onCancel(int cancelReason) {
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }
}
