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
import info.goforus.goforus.event_results.JobsFromApiResult;
import info.goforus.goforus.jobs.GetJobsJob;
import info.goforus.goforus.models.jobs.Job;

public class JobsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = "Jobs Fragment";
    private BaseActivity mActivity;
    private JobsAdapter mAdapter;
    private List<Job> mJobs;
    @Bind(R.id.swipeLayout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.lvJobs) ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mActivity = (BaseActivity) getActivity();
        mActivity.setTitle("My Jobs");
        View view = inflater.inflate(R.layout.fragment_jobs, container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setOnRefreshListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mJobs = Job.orderedByRecent();
        mAdapter = new JobsAdapter(mActivity, mJobs);
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
        GoForUs.getInstance().getJobManager().addJobInBackground(new GetJobsJob());
        swipeRefreshLayout.setRefreshing(true);
        Logger.d("We are refreshing our jobs");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onJobsUpdate(JobsFromApiResult result) {

        if (swipeRefreshLayout.isRefreshing()) {
            Toast.makeText(getContext(), "Jobs Refreshed", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
