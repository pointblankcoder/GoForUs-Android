package info.goforus.goforus;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.goforus.goforus.tasks.ProcessLoginTask;

public class TaskFragment extends Fragment {

    Activity mActivity;
    ProcessLoginTask mProcessLoginTaskTask;


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mActivity = context instanceof Activity ? (Activity) context : null;

        if (mProcessLoginTaskTask != null && mActivity != null) {
            mProcessLoginTaskTask.onAttach(mActivity);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (mProcessLoginTaskTask != null && mActivity != null) {
            mProcessLoginTaskTask.onDetach();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setLoginRequirements(String email, String password, boolean isRegister) {
        if (mActivity != null) {
            mProcessLoginTaskTask = new ProcessLoginTask((LoginActivity) mActivity, email, password, isRegister);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }
    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void startTask() {
        mProcessLoginTaskTask.execute();
    }
}
