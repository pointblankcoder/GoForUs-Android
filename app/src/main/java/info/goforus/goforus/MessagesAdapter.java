package info.goforus.goforus;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

import java.util.List;

import info.goforus.goforus.apis.Messages;
import info.goforus.goforus.models.conversations.Message;

public class MessagesAdapter extends ArrayAdapter<Message> {
    private ListView mListView;
    private NavigationActivity mContext;

    private static class ViewHolder {
        ImageView imageOther;
        ImageView imageMe;
        TextView body;
        boolean isMe;
        boolean readBySender;
        boolean readByReceiver;
        boolean waitingForConfirmation;
        TextView status;
    }

    public MessagesAdapter(Context context, ListView listView, List<Message> messages) {
        super(context, 0, messages);
        mListView = listView;
        mContext = (NavigationActivity) context;
    }

    @Override
    public void remove(Message message){
        View view = getViewByPosition(getPosition(message), mListView);
        final boolean[] ended = {false};
        YoYo.with(Techniques.Tada)
                .duration(700)
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ended[0] = true;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                })
                .playOn(view);

        super.remove(message);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_message, parent, false);

            viewHolder.imageOther = (ImageView) convertView.findViewById(R.id.ivProfileOther);
            viewHolder.imageMe = (ImageView) convertView.findViewById(R.id.ivProfileMe);
            viewHolder.body = (TextView) convertView.findViewById(R.id.tvBody);
            viewHolder.status = (TextView) convertView.findViewById(R.id.tvStatus);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.white, mContext.getTheme()));
        } else {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }

        if (message.isMe) {
            viewHolder.imageMe.setVisibility(View.VISIBLE);
            viewHolder.imageOther.setVisibility(View.GONE);
            viewHolder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            viewHolder.status.setVisibility(View.VISIBLE);
            viewHolder.status.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

            // Set Status only if we are the last sent message
            if (position == (getCount() - 1)) {
                if (message.waitingForConfirmation)
                    viewHolder.status.setText(R.string.message_item_sent);
                else
                    viewHolder.status.setText(R.string.message_item_delivered);

            } else {
                viewHolder.status.setVisibility(View.GONE);
            }

            // Set background color if we are waiting for confirmation
            if (message.waitingForConfirmation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.colorGreyOut, mContext.getTheme()));
                } else {
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.colorGreyOut));
                }
            }
        } else {
            viewHolder.imageOther.setVisibility(View.VISIBLE);
            viewHolder.imageMe.setVisibility(View.GONE);
            viewHolder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            viewHolder.status.setVisibility(View.GONE);
        }

        viewHolder.waitingForConfirmation = message.waitingForConfirmation;
        viewHolder.isMe = message.isMe;
        viewHolder.readByReceiver = message.readByReceiver;
        viewHolder.readBySender = message.readBySender;
        viewHolder.body.setText(message.body);

        return convertView;
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
