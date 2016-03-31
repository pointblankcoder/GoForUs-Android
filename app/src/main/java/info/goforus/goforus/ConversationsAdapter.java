package info.goforus.goforus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.greenrobot.eventbus.util.AsyncExecutor;

import java.util.List;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;

public class ConversationsAdapter extends ArrayAdapter<Conversation> {
    private final NavigationActivity mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView subject;
    }

    public ConversationsAdapter(Context context, List<Conversation> conversations) {
        super(context, 0, conversations);
        mContext = (NavigationActivity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Conversation conversation = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_conversation, parent, false);
            viewHolder.subject = (TextView) convertView.findViewById(R.id.tvSubject);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AsyncExecutor.create().execute(new AsyncExecutor.RunnableEx() {
                    @Override
                    public void run() throws Exception {
                        for(Message m : conversation.messages()) {
                            if(!m.readByReceiver) {
                                Utils.MessagesApi.markRead(conversation, m);
                                m.readByReceiver = true;
                                m.save();
                            }
                        }
                    }
                });

                mContext.showMessagesFragment(conversation);
            }
        });

        // Populate the data into the template view using the data object
        viewHolder.subject.setText(String.format("%s (%s)",conversation.subject, conversation.unreadMessageCount()));

        // Return the completed view to render on screen
        return convertView;
    }
}
