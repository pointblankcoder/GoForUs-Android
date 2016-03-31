package info.goforus.goforus;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.event_results.ConversationsFromApiResult;

public class InboxFragment extends Fragment {
    public static final String TAG = "Inbox Activity";
    private BaseActivity mActivity;
    private ConversationsAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mActivity = (BaseActivity) getActivity();

        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }

    @Override
    public void onStart(){
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

    private void populateConversationsList() {
        // Construct the data source
        List<Conversation> conversationsFromDB = Account.currentAccount().conversations();
        Logger.d("conversations from the database are (%s)", conversationsFromDB);

        // Create the adapter to convert the array to views
        mAdapter = new ConversationsAdapter(mActivity, conversationsFromDB);

        // Attach the adapter to a ListView
        ListView listView = (ListView) mActivity.findViewById(R.id.lvConversations);
        listView.setAdapter(mAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConversationsUpdate(ConversationsFromApiResult result) {
        if(result.getConversations().size() > mAdapter.getCount()) {
            mAdapter.addAll(result.getConversations());
            mAdapter.notifyDataSetChanged();
        }
    }
}
