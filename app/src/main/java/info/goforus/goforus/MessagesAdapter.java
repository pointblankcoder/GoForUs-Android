package info.goforus.goforus;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

import java.util.List;

import info.goforus.goforus.models.conversations.Message;

public class MessagesAdapter extends ArrayAdapter<Message> {
    private final List<Message> mMessages;
    private ListView mListView;
    private NavigationActivity mContext;

    private static class ViewHolder {
        int rowType;
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
        mMessages = messages;
    }

    static final int MY_ROW_TYPE = 1;
    static final int THEIR_ROW_TYPE = 0;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        final ViewHolder viewHolder;
        int rowType = getItemViewType(position);


        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());

            if (rowType == MY_ROW_TYPE) {
                convertView = inflater.inflate(R.layout.item_message_mine, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.item_message_theirs, parent, false);
            }

            viewHolder.rowType = rowType;
            viewHolder.view = convertView;
            viewHolder.body = (TextView) convertView.findViewById(R.id.tvBody);
            viewHolder.status = (TextView) convertView.findViewById(R.id.tvStatus);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (message.isMe) {
            viewHolder.status.setVisibility(View.VISIBLE);
            viewHolder.status.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

            // Set Status only if we are the last sent message
            if (position == (getCount() - 1)) {
                if (message.waitingForConfirmation)
                    viewHolder.status.setText(R.string.message_item_sent);
                else if (message.readByReceiver)
                    viewHolder.status.setText(R.string.message_item_seen);
                else
                    viewHolder.status.setText(R.string.message_item_delivered);

            } else {
                viewHolder.status.setVisibility(View.GONE);
            }


        } else {
            viewHolder.status.setVisibility(View.GONE);
        }

        viewHolder.isMe = message.isMe;
        viewHolder.readByReceiver = message.readByReceiver;
        viewHolder.readBySender = message.readBySender;
        viewHolder.body.setText(message.body);


        if (message.shouldAnimateIn) {
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
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    })
                    .playOn(viewHolder.view);
        }


        final View finalConvertView = convertView;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(finalConvertView.getWindowToken(), 0);
            }
        });


        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(mMessages.get(position).isMe){
            return MY_ROW_TYPE;  // our layout
        }
        return THEIR_ROW_TYPE; // their layout
    }
}
