package info.goforus.goforus;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import info.goforus.goforus.event_results.MessageMarkReadResult;
import info.goforus.goforus.event_results.MessagesFromApiResult;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.event_results.ConversationsFromApiResult;

public class InboxFragment extends Fragment {
    public static final String TAG = "Inbox Activity";
    private BaseActivity mActivity;
    private ConversationsAdapter mAdapter;
    private List<Conversation> mConversations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mActivity = (BaseActivity) getActivity();
        setTitle();

        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        populateConversationsList();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
        } else {
            mAdapter.notifyDataSetChanged();
            setTitle();
        }
    }

    private void setTitle(){
        if(Conversation.totalUnreadMessagesCount() > 0) {
            mActivity.setTitle(String.format("Inbox (%s)", Conversation.totalUnreadMessagesCount()));
        } else {
            mActivity.setTitle("Inbox");
        }
    }

    private void populateConversationsList() {
        // Construct the data source
        mConversations = Account.currentAccount().conversationsOrderedByRecentMessages();
        // Create the adapter to convert the array to views
        mAdapter = new ConversationsAdapter(mActivity, mConversations);

        // Attach the adapter to a ListView
        ListView listView = (ListView) mActivity.findViewById(R.id.lvConversations);
        listView.setAdapter(mAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConversationsUpdate(ConversationsFromApiResult result) {
        if (result.getConversations().size() > 0) {
            mAdapter.addAll(result.getConversations());
            mAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessagesUpdate(MessagesFromApiResult result) {
        if (result.getMessages().size() > 0) {
            if (mConversations.contains(result.getConversation())) {
                mActivity.setTitle("Inbox");
                final int position = mAdapter.getPosition(result.getConversation());

                mConversations.remove(position);
                mConversations.add(0, result.getConversation());
                mAdapter.notifyDataSetChanged();
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageRead(MessageMarkReadResult result) {
        if (mConversations.contains(result.getConversation())) {
            setTitle();
            mConversations = Account.currentAccount().conversationsOrderedByRecentMessages();
            mAdapter.notifyDataSetChanged();
        }
    }
}
