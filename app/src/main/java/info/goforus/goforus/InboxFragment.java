package info.goforus.goforus;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.orhanobut.logger.Logger;

import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;

public class InboxFragment extends Fragment {
    public static final String TAG = "Inbox Activity";
    private BaseActivity mActivity;


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

    private void populateConversationsList() {
        // Construct the data source
        List<Conversation> conversationsFromDB = Account.currentAccount().conversations();
        Logger.d("conversations from the database are (%s)", conversationsFromDB);

        // Create the adapter to convert the array to views
        ConversationsAdapter adapter = new ConversationsAdapter(mActivity, conversationsFromDB);

        // Attach the adapter to a ListView
        ListView listView = (ListView) mActivity.findViewById(R.id.lvConversations);
        listView.setAdapter(adapter);
    }
}
