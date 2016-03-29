package info.goforus.goforus;

import android.app.VoiceInteractor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;
import com.orhanobut.logger.Logger;

import org.parceler.Parcels;

import java.util.List;

import info.goforus.goforus.apis.Location;
import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.apis.listeners.InboxResponseListener;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.DataDistributor;
import info.goforus.goforus.models.conversations.Message;
import info.goforus.goforus.tasks.ConversationsUpdatesTask;
import us.monoid.json.JSONArray;

public class MessagesFragment extends Fragment implements DataDistributor.MessagesUpdates {
    private EditText etMessage;
    private ImageButton btSend;
    private ListView lvChat;
    private List<Message> mMessages;
    private boolean mFirstLoad;
    private MessagesAdapter mAdapter;
    public static Conversation mConversation;
    private BaseActivity mActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mConversation = Parcels.unwrap(getArguments().getParcelable("Conversation"));
        mMessages = mConversation.messages();
        mActivity = (BaseActivity) getActivity();
        DataDistributor.getInstance().addListener(this);

        Handler handler = new Handler();

        handler.postDelayed(new ConversationsUpdatesTask(handler, 1000), 1000);


        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        etMessage = (EditText) mActivity.findViewById(R.id.etMessage);
        btSend = (ImageButton) mActivity.findViewById(R.id.btSend);
        lvChat = (ListView) mActivity.findViewById(R.id.lvChat);

        Logger.d("our Conversation are (%s)", mMessages);
        /* Automatically scroll to the bottom when a data set change notification is received
         and only if the last item is already visible on screen. Don't scroll to the bottom otherwise.
          */
        lvChat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mFirstLoad = true;
        mAdapter = new MessagesAdapter(mActivity, mMessages, mConversation);
        lvChat.setAdapter(mAdapter);

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = etMessage.getText().toString();
                Message message = new Message();
                message.conversation = mConversation;
                message.isMe = true;
                message.body = data;

                Utils.ConversationsApi.sendMessage(message);

                // TODO: Add sending message to server
                Toast.makeText(mActivity, "Message was sent", Toast.LENGTH_SHORT).show();

                etMessage.setText(null);
            }
        });
    }


    @Override
    public void onMessagesUpdate(final List<Message> messages) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (messages.size() > 0 && MessagesFragment.mConversation != null &&
                            messages.get(0).conversation.externalId == mConversation.externalId &&
                            messages.size() > mAdapter.getCount()) {
                        mAdapter.clear();
                        mAdapter.addAll(messages);
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (NullPointerException e) {
                    Logger.e(e.toString());
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void onStart() {
        super.onStart();
    }
}
