package info.goforus.goforus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.goforus.goforus.event_results.MessageSentResult;
import info.goforus.goforus.event_results.MessagesFromApiResult;
import info.goforus.goforus.jobs.GetMessagesJob;
import info.goforus.goforus.jobs.MarkReadMessageJob;
import info.goforus.goforus.jobs.PostMessageJob;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;
import info.goforus.goforus.tasks.MessagesUpdateHandler;

public class MessagesFragment extends Fragment {
    @Bind(R.id.etMessage) EditText etMessage;
    @Bind(R.id.btSend) ImageButton btSend;
    @Bind(R.id.lvChat) ListView lvChat;
    BaseActivity mActivity;
    JobManager mJobManager;
    private MessagesAdapter mAdapter;
    public static Conversation mConversation;
    List<Message> waitingForConfirmation = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        MessagesUpdateHandler.getInstance().stopUpdates();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        mActivity = (BaseActivity) getActivity();
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        mJobManager = GoForUs.getInstance().getJobManager();

        // auto focus the send message box with indicator.
        etMessage.requestFocus();

        /* Automatically scroll to the bottom when a data set change notification is received
         and only if the last item is already visible on screen. Don't scroll to the bottom otherwise.
          */
        lvChat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mAdapter = new MessagesAdapter(mActivity, mConversation.messages());
        lvChat.setAdapter(mAdapter);
        lvChat.setSelection(mAdapter.getCount() - 1);
    }

    @OnClick(R.id.btSend)
    public void onSendClick(){
        String data = etMessage.getText().toString();

        if (TextUtils.isEmpty(data) || data.trim().length() <= 2) {
            etMessage.clearFocus();
            Toast.makeText(mActivity, "Message is too short.", Toast.LENGTH_LONG).show();
        } else {

            Message message = new Message();
            message.conversation = mConversation;
            message.isMe = true;
            message.body = data;
            message.shouldAnimateIn = true;
            message.save();
            waitingForConfirmation.add(message);
            mAdapter.add(message);

            mJobManager
                    .addJobInBackground(new PostMessageJob(message.body, mConversation.externalId));

            // TODO show some indicator that we are sending the message
            btSend.setEnabled(false);
            etMessage.setEnabled(false);
            etMessage.clearFocus();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        MessagesUpdateHandler.getInstance().startUpdates(mConversation.externalId);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MessagesUpdateHandler.getInstance().stopUpdates();
        EventBus.getDefault().unregister(this);
    }

    // This is where we show the fragment again from an on click,
    // TODO: think of a better way around handing the ListView new data from outside of the fragment
    // without hooking into the show() hide() methods of the Fragme
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            MessagesUpdateHandler.getInstance().stopUpdates();
        } else {
            MessagesUpdateHandler.getInstance().startUpdates(mConversation.externalId);
            mAdapter.clear();
            mAdapter.addAll(mConversation.messages());
            mAdapter.notifyDataSetChanged();

            // Request focus without starting up the keyboard
            etMessage.requestFocus();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageSent(MessageSentResult result) {
        if (result.getResultCode() == MessageSentResult.RESULT_OK) {
            btSend.setEnabled(true);
            etMessage.setEnabled(true);
            etMessage.setText(null);
            GoForUs.getInstance().getJobManager()
                   .addJobInBackground(new GetMessagesJob(mConversation.externalId));
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
        if (result.getConversationId() == mConversation.externalId && result.getMessages()
                                                                            .size() > 0) {
            List<Message> messages = result.getMessages();

            // we don't add messages that are stored already locally. We just confirm that they have been received.
            for (Message message : messages) {
                if (!message.isMe) {
                    mAdapter.add(message);
                } else {
                    if (message.confirmedReceived) {
                        int position = mAdapter.getPosition(message);
                        if (position > 0) {
                            mAdapter.getItem(position).confirmedReceived = true;
                        }
                    } else {
                        mAdapter.add(message);
                    }
                }

                GoForUs.getInstance().getJobManager()
                       .addJobInBackground(new MarkReadMessageJob(mConversation.externalId, message.externalId));
                message.confirmedReceived = true;
                if (isVisible()) {
                    message.readByReceiver = true;
                }
                message.save();
            }

            mAdapter.notifyDataSetChanged();
        }
    }
}
