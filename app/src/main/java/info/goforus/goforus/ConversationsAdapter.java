package info.goforus.goforus;

import android.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.DataDistributor;

public class ConversationsAdapter extends ArrayAdapter<Conversation> implements DataDistributor.ConversationsUpdates {
    private final NavigationActivity mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView subject;
    }

    public ConversationsAdapter(Context context, List<Conversation> conversations) {
        super(context, 0, conversations);
        mContext = (NavigationActivity) context;
        DataDistributor.getInstance().addListener(this);
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
                mContext.showMessagesFragment(conversation);
            }
        });

        // Populate the data into the template view using the data object
        viewHolder.subject.setText(conversation.subject);

        // Return the completed view to render on screen
        return convertView;
    }


    @Override
    public void onConversationsUpdate(final List<Conversation> conversations) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clear();
                addAll(conversations);
                notifyDataSetChanged();
            }
        });
    }
}
