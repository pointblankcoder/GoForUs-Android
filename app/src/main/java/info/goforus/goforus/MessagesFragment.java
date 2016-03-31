package info.goforus.goforus;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.eventbus.util.AsyncExecutor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;
import info.goforus.goforus.event_results.MessagesFromApiResult;
import info.goforus.goforus.event_results.MessageSentResult;
import info.goforus.goforus.tasks.MessagesUpdateHandler;

public class MessagesFragment extends Fragment {
    private EditText etMessage;
    private ImageButton btSend;
    public ListView lvChat;
    private MessagesAdapter mAdapter;
    public static Conversation mConversation;
    private BaseActivity mActivity;
    private long lastConnectionTime = 0;
    private RelativeLayout connectivityLayout;
    Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mActivity = (BaseActivity) getActivity();

        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    List<Message> waitingForConfirmation = new ArrayList<>();

    @Override
    public void onStart() {
        super.onStart();
        etMessage = (EditText) mActivity.findViewById(R.id.etMessage);
        btSend = (ImageButton) mActivity.findViewById(R.id.btSend);
        lvChat = (ListView) mActivity.findViewById(R.id.lvChat);
        connectivityLayout = (RelativeLayout) mActivity.findViewById(R.id.connectivityLayout);

        /* Automatically scroll to the bottom when a data set change notification is received
         and only if the last item is already visible on screen. Don't scroll to the bottom otherwise.
          */
        lvChat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mAdapter = new MessagesAdapter(mActivity, lvChat, mConversation.messages());
        lvChat.setAdapter(mAdapter);
        lvChat.setSelection(mAdapter.getCount() - 1);

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = etMessage.getText().toString();

                if (TextUtils.isEmpty(data) || data.length() <= 2) {
                    etMessage.clearFocus();
                    Toast.makeText(mActivity, "Message is too short.", Toast.LENGTH_LONG).show();
                } else {

                    Message message = new Message();
                    message.conversation = mConversation;
                    message.isMe = true;
                    message.body = data;
                    message.waitingForConfirmation = true;
                    message.shouldAnimateIn = true;
                    waitingForConfirmation.add(message);
                    mAdapter.add(message);

                    // TODO show some indicator that we are sending the message
                    btSend.setEnabled(false);
                    etMessage.setEnabled(false);
                    etMessage.clearFocus();

                    final Message messageToSend = message;
                    AsyncExecutor.create().execute(new AsyncExecutor.RunnableEx() {
                        @Override
                        public void run() throws Exception {
                            Utils.ConversationsApi.sendMessage(messageToSend);
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        MessagesUpdateHandler.getInstance().startUpdates(mConversation);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        MessagesUpdateHandler.getInstance().stopUpdates();
    }

    // This is where we show the fragment again from an on click,
    // TODO: think of a better way around handing the ListView new data from outside of the fragment
    // without hooking into the show() hide() methods of the Fragme
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            mAdapter.clear();
            mAdapter.addAll(mConversation.messages());
            mAdapter.notifyDataSetChanged();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageSent(MessageSentResult result) {
        if (result.getResultCode() == MessageSentResult.RESULT_OK) {
            btSend.setEnabled(true);
            etMessage.setEnabled(true);
            etMessage.setText(null);
            AsyncExecutor.create().execute(new AsyncExecutor.RunnableEx() {
                @Override
                public void run() throws Exception {
                    Utils.MessagesApi.getMessages(mConversation);
                }
            });
        } else {
            // Pop that last message from waiting confirmation! Something went wrong!
            waitingForConfirmation.remove(waitingForConfirmation.size() - 1);
            btSend.setEnabled(true);
            etMessage.setEnabled(true);
            Toast.makeText(mActivity, result.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessagesUpdate(MessagesFromApiResult result) {
        if (result.getConversation().externalId == mConversation.externalId && result.getMessages().size() > 0) {
            List<Message> messages = result.getMessages();

            for (Message newMessage : messages) {
                for (Message waitingMessage : waitingForConfirmation) {
                    if (waitingMessage.body.equals(newMessage.body)) {
                        waitingForConfirmation.remove(waitingMessage);
                        mAdapter.remove(waitingMessage);
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            mAdapter.addAll(messages);
            mAdapter.notifyDataSetChanged();
        }
    }
}