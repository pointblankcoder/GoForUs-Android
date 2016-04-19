package info.goforus.goforus.managers;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.DialogPlusBuilder;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.goforus.goforus.BaseActivity;
import info.goforus.goforus.GoForUs;
import info.goforus.goforus.NavigationActivity;
import info.goforus.goforus.R;
import info.goforus.goforus.event_results.AcceptedOrderResult;
import info.goforus.goforus.event_results.DeclinedOrderResult;
import info.goforus.goforus.event_results.MessagesFromApiResult;
import info.goforus.goforus.jobs.PostOrderJob;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.drivers.Driver;
import info.goforus.goforus.models.orders.Order;

public class ContactDriverManager implements OnDismissListener {
    private static ContactDriverManager ourInstance = new ContactDriverManager();
    private boolean dismissedContactThroughMessageClick = false;
    private final OrderModeManager orderModelManager = OrderModeManager.getInstance();

    public static ContactDriverManager getInstance() { return ourInstance; }

    private ContactDriverManager() {
        EventBus.getDefault().register(this);
    }

    Order mOrder;
    BaseActivity mActivity;
    Driver mDriver;
    DialogPlus contactDialog;
    DialogPlus waitingForReplyDialog;
    DialogPlusBuilder waitingForReplyDialogBuilder;
    @Nullable @Bind(R.id.messageSendBtn) ImageButton messageSendBtn;
    @Nullable @Bind(R.id.message) TextView message;
    @Nullable @Bind(R.id.status) TextView status;
    @Nullable @Bind(R.id.accepted) ImageView accepted;
    @Nullable @Bind(R.id.declined) ImageView declined;
    @Nullable @Bind(R.id.progress) ProgressBar progress;
    @Nullable @Bind(R.id.complete) View complete;
    @Nullable @Bind(R.id.quickLocationSelection) LinearLayout quickLocationSelection;
    @Nullable @Bind(R.id.estimatedCost) TextView estimatedCost;

    public void setup(BaseActivity activity, Order order, Driver driver) {
        mDriver = driver;
        mActivity = activity;
        mOrder = order;

        contactDialog = DialogPlus.newDialog(mActivity)
                                  .setContentBackgroundResource(R.color.primary_material_dark_1)
                                  .setContentHolder(new ViewHolder(R.layout.dialog_contact_driver_content))
                                  .setOnDismissListener(this)
                                  .setFooter(R.layout.dialog_contact_driver_footer)
                                  .setGravity(Gravity.TOP).setCancelable(true).create();

        waitingForReplyDialogBuilder = DialogPlus.newDialog(mActivity)
                                                 .setContentBackgroundResource(R.color.primary_material_dark_1)
                                                 .setContentHolder(new ViewHolder(R.layout.dialog_wait_for_driver_response_content))
                                                 .setOnDismissListener(this)
                                                 .setGravity(Gravity.CENTER).setCancelable(false);

        ButterKnife.bind(this, contactDialog.getFooterView());
        ButterKnife.bind(this, contactDialog.getHolderView());
    }

    public void show() {
        contactDialog.show();
        if (message != null) message.requestFocus();
    }

    @Nullable
    @OnClick(R.id.messageSendBtn)
    public void onMessageSendClick() {
        mOrder.description = message.getText().toString();
        if (mOrder.description.isEmpty()) {
            return;
        }

        contactDialog.dismiss();
        dismissedContactThroughMessageClick = true;
        mOrder.save();

        if (quickLocationSelection != null) quickLocationSelection.setVisibility(View.GONE);
        if (complete != null) complete.setVisibility(View.GONE);
        if (estimatedCost != null) estimatedCost.setVisibility(View.GONE);


        GoForUs.getInstance().getJobManager().addJobInBackground(new PostOrderJob(mOrder.getId()));
    }

    @Override
    public void onDismiss(DialogPlus dialog) {
        if (dialog == contactDialog && dismissedContactThroughMessageClick) {
            waitingForReplyDialog = waitingForReplyDialogBuilder.create();

            ButterKnife.bind(this, waitingForReplyDialog.getHolderView());
            waitingForReplyDialog.show();

            View view = mActivity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) mActivity
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccepted(AcceptedOrderResult result) {
        if (status != null) status.setText("Accepted!");

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (progress != null) progress.setVisibility(View.GONE);
                if (accepted != null) accepted.setVisibility(View.VISIBLE);
                if (status != null) {
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            DriversOnMapManager.getInstance().getSelectedDriver().goToWithInfoWindow(1000);
                            if (waitingForReplyDialog != null) waitingForReplyDialog.dismiss();
                            OrderModeManager.getInstance().exitOrderMode();
                            Toast.makeText(mActivity, "Keep an on your inbox in case the driver has any complications or questions!", Toast.LENGTH_LONG)
                                 .show();
                            Toast.makeText(mActivity, "You can also contact the driver by heading to your inbox!", Toast.LENGTH_LONG)
                                 .show();

                        }
                    }, 2000);
                }
            }
        };

        handler.postDelayed(runnable, 2000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeclined(final DeclinedOrderResult result) {
        if (status != null) status.setText("Declined!");
        if (progress != null) progress.setVisibility(View.GONE);
        if (accepted != null) accepted.setVisibility(View.GONE);
        if (declined != null) declined.setVisibility(View.VISIBLE);

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // We are not the customer this does not need to trigger
                if (result.getOrder().customerId == Account.currentAccount().externalId) {
                    if (waitingForReplyDialog != null) waitingForReplyDialog.dismiss();
                    OrderModeManager.getInstance().exitOrderMode();
                    Toast.makeText(mActivity, "Sorry for the inconvenience, you have to do that all over again!", Toast.LENGTH_LONG)
                         .show();
                }
            }
        };

        handler.postDelayed(runnable, 2000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageReceived(final MessagesFromApiResult result) {
        final Conversation conversation = Conversation.findByExternalId(result.getConversationId());
        Order order = Order.findByConversation(conversation);
        if(order != null && mOrder != null && order.externalId == mOrder.externalId) {
            Logger.i("We have received a message from the partner related to this order. Let's allow the customer to reply");
            if (progress != null) progress.setVisibility(View.GONE);
            if (status != null) status.setText("You've received a message from the Driver!");

            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // We are not the customer this does not need to trigger
                    if (waitingForReplyDialog != null) waitingForReplyDialog.dismiss();
                    OrderModeManager.getInstance().exitOrderMode();
                    ((NavigationActivity)mActivity).showMessagesFragment(conversation);
                }
            };

            handler.postDelayed(runnable, 2000);
        }
    }
}
