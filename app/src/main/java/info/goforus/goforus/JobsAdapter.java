package info.goforus.goforus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.goforus.goforus.models.jobs.Job;

public class JobsAdapter extends ArrayAdapter<Job> {
    private final NavigationActivity mContext;

    // View lookup cache
    static class ViewHolder {
        @Bind(R.id.tvSubject) TextView subject;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public JobsAdapter(Context context, List<Job> jobs) {
        super(context, 0, jobs);
        mContext = (NavigationActivity) context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // Get the data item for this position
        final Job job = getItem(position);
        final ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.item_job, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: show popup menue with more information and so they can accept it or not
            }
        });

        viewHolder.subject.setText("Job Offer");

        // Return the completed view to render on screen
        return view;
    }

    private String withoutLineBreaks(String body) {
        String newBody = body.replaceAll("\r", "..").replaceAll("\n", "..");
        return newBody;
    }
}
