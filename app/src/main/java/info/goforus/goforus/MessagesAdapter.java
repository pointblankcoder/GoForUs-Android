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
import com.orhanobut.logger.Logger;

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
        TextView status;
        View view;
    }

    public MessagesAdapter(Context context, ListView listView, List<Message> messages) {
        super(context, 0, messages);
        mListView = listView;
        mContext = (NavigationActivity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_message, parent, false);

            viewHolder.view = convertView;
            viewHolder.imageOther = (ImageView) convertView.findViewById(R.id.ivProfileOther);
            viewHolder.imageMe = (ImageView) convertView.findViewById(R.id.ivProfileMe);
            viewHolder.body = (TextView) convertView.findViewById(R.id.tvBody);
            viewHolder.status = (TextView) convertView.findViewById(R.id.tvStatus);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
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
                    if (message.readByReceiver)
                        viewHolder.status.setText(R.string.message_item_seen);
                    else
                    viewHolder.status.setText(R.string.message_item_delivered);

            } else {
                viewHolder.status.setVisibility(View.GONE);
            }


        } else {
            viewHolder.imageOther.setVisibility(View.VISIBLE);
            viewHolder.imageMe.setVisibility(View.GONE);
            viewHolder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            viewHolder.status.setVisibility(View.GONE);
        }

        viewHolder.isMe = message.isMe;
        viewHolder.readByReceiver = message.readByReceiver;
        viewHolder.readBySender = message.readBySender;
        viewHolder.body.setText(message.body);


        if (message.shouldAnimateIn){
            YoYo.with(Techniques.FadeInUp)
                    .duration(500)
                    .withListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {}

                        @Override
                        public void onAnimationRepeat(Animator animation) {}
                    })
                    .playOn(viewHolder.view);
        }

        return convertView;
    }
}
