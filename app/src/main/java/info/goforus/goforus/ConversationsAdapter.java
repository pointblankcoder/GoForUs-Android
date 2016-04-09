package info.goforus.goforus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.goforus.goforus.jobs.MarkReadMessageJob;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;

public class ConversationsAdapter extends ArrayAdapter<Conversation> {
    private final NavigationActivity mContext;

    // View lookup cache
    static class ViewHolder {
        @Bind(R.id.tvSubject) TextView subject;
        @Bind(R.id.tvLastMessageSummary) TextView lastMessageSummary;
        @Bind(R.id.conversationWrapper) RelativeLayout conversationWrapper;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public ConversationsAdapter(Context context, List<Conversation> conversations) {
        super(context, 0, conversations);
        mContext = (NavigationActivity) context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // Get the data item for this position
        final Conversation conversation = getItem(position);
        final ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.item_conversation, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Message m : conversation.messages()) {
                    if(!m.readByReceiver) {
                        GoForUs.getInstance().getJobManager().addJobInBackground(new MarkReadMessageJob(conversation.externalId, m.externalId));
                        m.readByReceiver = true;
                        m.save();
                    }
                }
                mContext.showMessagesFragment(conversation);
            }
        });

        // Populate the data into the template view using the data object
        viewHolder.subject.setText(conversation.subject);
        if (conversation.lastMessage() != null) {
            viewHolder.lastMessageSummary.setText(withoutLineBreaks(conversation.lastMessage().body));
        }

        if (conversation.unreadMessageCount() > 0) {
            viewHolder.conversationWrapper.setBackgroundResource(R.color.accent_material_dark_1);
            YoYo.with(Techniques.SlideInDown).duration(500).playOn(viewHolder.conversationWrapper);
        } else {
            viewHolder.conversationWrapper.setBackgroundResource(R.color.primary_material_dark_1);
        }

        // Return the completed view to render on screen
        return view;
    }

    private String withoutLineBreaks(String body) {
        String newBody = body.replaceAll("\r", "..").replaceAll("\n", "..");
        return newBody;
    }
}
