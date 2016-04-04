package info.goforus.goforus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.goforus.goforus.event_results.ConversationsFromApiResult;
import info.goforus.goforus.event_results.MessageMarkReadResult;
import info.goforus.goforus.event_results.MessagesFromApiResult;
import info.goforus.goforus.jobs.GetConversationsJob;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;

public class InboxFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = "Inbox Activity";
    private BaseActivity mActivity;
    private ConversationsAdapter mAdapter;
    private List<Conversation> mConversations;
    @Bind(R.id.swipeLayout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.lvConversations) ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mActivity = (BaseActivity) getActivity();
        mActivity.setTitle("Inbox");
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setOnRefreshListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mConversations = Account.currentAccount().conversationsOrderedByRecentMessages();
        mAdapter = new ConversationsAdapter(mActivity, mConversations);
        listView.setAdapter(mAdapter);
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
        }
    }

    @Override
    public void onRefresh() {
        Application.getInstance().getJobManager().addJobInBackground(new GetConversationsJob());
        swipeRefreshLayout.setRefreshing(true);
        Logger.d("We are refreshing our inbox");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConversationsUpdate(ConversationsFromApiResult result) {
        if (result.getConversations().size() > 0) {
            for (Conversation c : result.getConversations()) {
                if(!mConversations.contains(c)) {
                    mAdapter.add(c);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessagesUpdate(MessagesFromApiResult result) {
        Conversation resultConversation = Conversation.findByExternalId(result.getConversationId());

        if (mConversations.contains(resultConversation) && result.getMessages().size() > 0) {
            for (Message message : result.getMessages()) {
                if (!message.readByReceiver) {
                    final int position = mAdapter.getPosition(resultConversation);

                    mConversations.remove(position);
                    mConversations.add(0, resultConversation);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }


        if (swipeRefreshLayout.isRefreshing()) {
            Toast.makeText(getContext(), "Inbox Refreshed", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageRead(MessageMarkReadResult result) {
        Conversation resultConversation = Conversation.findByExternalId(result.getConversationId());
        if (mConversations.contains(resultConversation)) {
            mConversations = Account.currentAccount().conversationsOrderedByRecentMessages();
            mAdapter.notifyDataSetChanged();
        }
    }
}
