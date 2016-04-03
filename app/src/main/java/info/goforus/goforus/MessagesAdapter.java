package info.goforus.goforus;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.goforus.goforus.models.conversations.Message;

public class MessagesAdapter extends ArrayAdapter<Message> {
    private final List<Message> mMessages;
    private LayoutInflater inflater;

    static class ViewHolder {
        int rowType;
        @Bind(R.id.tvBody) TextView body;
        @Bind(R.id.tvStatus) ImageView status;
        View view;
        boolean isMe;
        boolean readBySender;
        boolean readByReceiver;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }

    public MessagesAdapter(Activity context, List<Message> messages) {
        super(context, 0, messages);
        mMessages = messages;
        inflater = context.getLayoutInflater();
    }

    static final int MY_ROW_TYPE = 1;
    static final int THEIR_ROW_TYPE = 0;

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Message message = getItem(position);

        final ViewHolder viewHolder;
        int rowType = getItemViewType(position);

        if (view == null) {
            if (rowType == MY_ROW_TYPE) {
                view = inflater.inflate(R.layout.item_message_mine, parent, false);
            } else {
                view = inflater.inflate(R.layout.item_message_theirs, parent, false);
            }

            viewHolder = new ViewHolder(view);
            viewHolder.rowType = rowType;
            viewHolder.view = view;
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (message.isMe) {
            viewHolder.status.setVisibility(View.VISIBLE);

            // Set Status only if we are the last sent message
            if (position == (getCount() - 1)) {
                if (message.confirmedReceived) viewHolder.status.setImageDrawable(ContextCompat
                        .getDrawable(getContext(), R.drawable.check_double));
                else viewHolder.status.setImageDrawable(ContextCompat
                        .getDrawable(getContext(), R.drawable.check));
            } else {
                viewHolder.status.setVisibility(View.GONE);
            }
        } else {
            viewHolder.status.setVisibility(View.GONE);
        }

        viewHolder.isMe = message.isMe;
        viewHolder.readByReceiver = message.readByReceiver;
        viewHolder.readBySender = message.readBySender;
        viewHolder.body.setText(message.body);

        if (message.shouldAnimateIn) {
            YoYo.with(Techniques.FadeInUp).duration(500).playOn(viewHolder.view);
            message.shouldAnimateIn = false;
        }

        final View finalView = view;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(finalView.getWindowToken(), 0);
            }
        });

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessages.get(position).isMe) {
            return MY_ROW_TYPE;  // our layout
        }
        return THEIR_ROW_TYPE; // their layout
    }
}
