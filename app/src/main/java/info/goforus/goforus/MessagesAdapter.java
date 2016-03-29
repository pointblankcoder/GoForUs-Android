package info.goforus.goforus;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.DataDistributor;
import info.goforus.goforus.models.conversations.Message;

public class MessagesAdapter extends ArrayAdapter<Message> {
    private NavigationActivity mContext;

    private static class ViewHolder {
        ImageView imageOther;
        ImageView imageMe;
        TextView body;
        boolean isMe;
        boolean isUnread;
    }

    public MessagesAdapter(Context context, List<Message> messages, Conversation conversation) {
        super(context, 0, messages);
        mContext = (NavigationActivity) context;
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
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Display the profile image to the right for our user, left for other users.
        if (message.isMe) {
            viewHolder.imageMe.setVisibility(View.VISIBLE);
            viewHolder.imageOther.setVisibility(View.GONE);
            viewHolder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        } else {
            viewHolder.imageOther.setVisibility(View.VISIBLE);
            viewHolder.imageMe.setVisibility(View.GONE);
            viewHolder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        }

        viewHolder.isMe = message.isMe;
        viewHolder.isUnread = message.isUnread;
        viewHolder.body.setText(message.body);

        return convertView;
    }
}
