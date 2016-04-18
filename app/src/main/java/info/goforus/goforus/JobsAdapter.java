package info.goforus.goforus;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.goforus.goforus.jobs.AcceptJobJob;
import info.goforus.goforus.jobs.DeclineJobJob;
import info.goforus.goforus.managers.OrderModeManager;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.jobs.Job;
import info.goforus.goforus.models.orders.Order;

public class JobsAdapter extends ArrayAdapter<Job> {
    private final NavigationActivity mContext;
    private boolean shouldShowMap = false;

    // View lookup cache
    static class ViewHolder {
        @Bind(R.id.tvSubject) TextView subject;
        @Bind(R.id.actionIndication) ImageView actionIndication;
        @Bind(R.id.tvJobSummary) TextView jobSummary;
        View view;
        long timeLeft = -1;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
            this.view = view;
        }
    }

    public JobsAdapter(Context context, List<Job> jobs) {
        super(context, 0, jobs);
        mContext = (NavigationActivity) context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // Get the data item for this position
        final Job job = getItem(position);
        final ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.item_job, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (job.respondedTo) {
            if (job.declined) {
                viewHolder.view.setBackgroundResource(R.color.accent_material_red_400);
                viewHolder.actionIndication.setImageResource(R.drawable.ic_cancel_white_48dp);
            } else if (job.accepted) {
                viewHolder.view.setBackgroundResource(R.color.accent_material_light_green_400);
                viewHolder.actionIndication.setImageResource(R.drawable.ic_check_circle_white_48dp);
            }
        } else {
            viewHolder.view.setBackgroundResource(R.color.accent_material_dark_1);
            viewHolder.actionIndication.setImageResource(R.drawable.ic_chevron_right_white_48dp);
        }

        Order order = Order.findByExternalId(job.orderId);
        if (order != null) {
            String pickupText = String.format("Pickup:\n%s", order.pickupAddress);
            String dropOffText = String.format("Drop Off:\n%s", order.dropOffAddress);
            String summaryText = String.format("%s\n%s", pickupText, dropOffText);

            viewHolder.jobSummary.setText(summaryText);
        }


        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Order order = Order.findByExternalId(job.orderId);
                if (order != null && !job.declined) {
                    final Conversation conversation = Conversation
                            .findByExternalId((int) order.conversationId);
                    DialogPlus replyDialog = DialogPlus.newDialog(getContext())
                                                       .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.dialog_reply_to_order))
                                                       .setGravity(Gravity.CENTER)
                                                       .setCancelable(true)
                                                       .setContentBackgroundResource(R.color.primary_material_dark_1)
                                                       .setOnClickListener(new OnClickListener() {
                                                           @Override
                                                           public void onClick(DialogPlus dialog, View view) {
                                                               Button acceptBtn = (Button) dialog
                                                                       .findViewById(R.id.acceptBtn);
                                                               Button declineBtn = (Button) dialog
                                                                       .findViewById(R.id.declineBtn);
                                                               Button messageBtn = (Button) dialog
                                                                       .findViewById(R.id.messageBtn);
                                                               Button showOnMapBtn = (Button) dialog
                                                                       .findViewById(R.id.showOnMap);
                                                               if (view == acceptBtn) {
                                                                   GoForUs.getInstance()
                                                                          .getJobManager()
                                                                          .addJobInBackground(new AcceptJobJob(job.externalId));
                                                                   dialog.dismiss();
                                                               } else if (view == declineBtn) {
                                                                   GoForUs.getInstance()
                                                                          .getJobManager()
                                                                          .addJobInBackground(new DeclineJobJob(job.externalId));
                                                                   dialog.dismiss();
                                                               } else if (view == messageBtn) {
                                                                   mContext.showMessagesFragment(conversation);
                                                                   dialog.dismiss();
                                                               } else if (view == showOnMapBtn) {
                                                                   MapFragment mapFragment = mContext
                                                                           .showMapFragment();
                                                                   mapFragment
                                                                           .setJourneyToShow(order);
                                                                   dialog.dismiss();
                                                               }
                                                           }
                                                       })
                                                       .setOnDismissListener(new OnDismissListener() {
                                                           @Override
                                                           public void onDismiss(DialogPlus dialog) {
                                                           }
                                                       }).create();
                    if (job.respondedTo) {
                        Button acceptBtn = (Button) replyDialog.findViewById(R.id.acceptBtn);
                        Button declineBtn = (Button) replyDialog.findViewById(R.id.declineBtn);
                        Button messageBtn = (Button) replyDialog.findViewById(R.id.messageBtn);

                        acceptBtn.setVisibility(View.GONE);
                        declineBtn.setVisibility(View.GONE);
                    }

                    replyDialog.show();

                    final TextView dropOffAt = (TextView) replyDialog.getHolderView()
                                                                     .findViewById(R.id.dropOffAt);
                    final TextView pickupFrom = (TextView) replyDialog.getHolderView()
                                                                      .findViewById(R.id.pickupFrom);
                    TextView estimatedPrice = (TextView) replyDialog.getHolderView()
                                                                    .findViewById(R.id.estimatedPrice);

                    final TextView timerCountdown = (TextView) replyDialog.getHolderView()
                                                                          .findViewById(R.id.timerCountdown);
                    final Button acceptBtn = (Button) replyDialog.findViewById(R.id.acceptBtn);
                    final Button declineBtn = (Button) replyDialog.findViewById(R.id.declineBtn);
                    final Button messageBtn = (Button) replyDialog.findViewById(R.id.messageBtn);

                    if (!order.respondedTo) {
                        new CountDownTimer(Job
                                .findByExternalId(job.externalId).timeRemaining, 1000) {
                            public void onTick(long millisUntilFinished) {
                                long totalSeconds = millisUntilFinished / 1000;
                                long minutes = (totalSeconds % 3600) / 60;
                                long seconds = totalSeconds % 60;
                                timerCountdown
                                        .setText(String.format("%02d:%02d", minutes, seconds));
                            }

                            public void onFinish() {
                                if (!order.respondedTo) {
                                    timerCountdown.setText(R.string.too_late_called_order_for_you);
                                    acceptBtn.setVisibility(View.GONE);
                                    declineBtn.setVisibility(View.GONE);
                                    messageBtn.setVisibility(View.GONE);
                                }
                            }
                        }.start();
                    }


                    dropOffAt.setText(String.format("%s\n%s", mContext
                            .getString(R.string.drop_off_at), order.dropOffAddress));
                    pickupFrom.setText(String.format("%s\n%s", mContext
                            .getString(R.string.pickup_from), order.pickupAddress));
                    estimatedPrice.setText(String.format("%s\n%s", mContext
                            .getString(R.string.estimated_price), order.estimatedCost));
                }
            }
        });

        viewHolder.subject.setText("Job Offer");

        // Return the completed view to render on screen
        return view;
    }

    private String withoutLineBreaks(String body) {
        String newBody = body.replaceAll("\r", "..").replaceAll("\n", "..");
        return newBody;
    }
}
