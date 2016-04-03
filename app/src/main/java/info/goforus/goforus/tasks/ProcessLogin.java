package info.goforus.goforus.tasks;

import android.content.Intent;
import android.os.AsyncTask;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import info.goforus.goforus.Application;
import info.goforus.goforus.LoginActivity;
import info.goforus.goforus.NavigationActivity;
import info.goforus.goforus.event_results.ConversationsFromApiResult;
import info.goforus.goforus.event_results.LocationUpdateServiceResult;
import info.goforus.goforus.event_results.LoginFromApiResult;
import info.goforus.goforus.jobs.AttemptLoginJob;
import info.goforus.goforus.jobs.AttemptRegisterJob;
import info.goforus.goforus.jobs.GetConversationsJob;
import info.goforus.goforus.models.accounts.Account;
import us.monoid.json.JSONException;

public class ProcessLogin extends AsyncTask<Object, String, Void> {
    private final LoginActivity mLoginActivity;
    private final String mEmail;
    private final String mPassword;
    private final boolean mRegistering;
    private boolean loggedIn = false;
    private boolean collectMessagesComplete = false;
    private boolean hasGpsLocation;

    public ProcessLogin(LoginActivity activity, String email, String password, boolean registering) {
        this.mLoginActivity = activity;
        this.mEmail = email;
        this.mPassword = password;
        this.mRegistering = registering;
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPreExecute() {
    }

    protected Void doInBackground(Object... objects) {
        final Application application = Application.getInstance();

        if (mRegistering) {
            publishProgress("Getting you registered");
            application.getJobManager().addJobInBackground(new AttemptRegisterJob(mEmail, mPassword));
        } else {
            publishProgress("Logging In");
            application.getJobManager().addJobInBackground(new AttemptLoginJob(mEmail, mPassword));
        }


        while (!loggedIn) {
        }

        publishProgress("Getting your messages");
        Application.getInstance().getJobManager().addJobInBackground(new GetConversationsJob());
        while(!collectMessagesComplete){}

        publishProgress("Making sure we know where you are! (look out your window)");
        LocationUpdateHandler.getInstance().forceUpdate();
        while(!hasGpsLocation){}

        publishProgress("All done, enjoy!");

        return null;
    }


    protected void onProgressUpdate(String... status) {
        mLoginActivity.tvLoginStatus.setText(status[0]);
    }

    protected void onPostExecute(Void _null) {
        Intent intent = new Intent(mLoginActivity, NavigationActivity.class);
        mLoginActivity.startActivity(intent);
        mLoginActivity.finish();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginResult(LoginFromApiResult result) {
        if (result.hasFailure()) {
            mLoginActivity.showProgress(false);

            if (result.failedEmailMessage() != null) {
                mLoginActivity.showErrorOn(mLoginActivity.mEmailView, result.failedEmailMessage());
            }

            if (result.failedPasswordMessage() != null) {
                mLoginActivity.showErrorOn(mLoginActivity.mPasswordView, result.failedPasswordMessage());
            }

            // TODO: add base message failures, this can happen if there's no server response,
            //       unknown error has happened (parsing json for example), or we do not support the error response just yet
            // mBaseMessageView.setError(result.failedMessage())

        } else {

            Account currentAccount = Account.currentAccount();
            if (currentAccount != null)
                // Ensure we are logged out
                currentAccount.markAsLoggedOut();

            try {
                currentAccount = Account.findOrCreateFromApi(result.getResponse());
                currentAccount.updateFromApi(result.getResponse());
                currentAccount.markAsLoggedIn();

                LocationUpdateHandler.getInstance().turnUpdatesOn();
                LocationUpdateHandler.getInstance().forceUpdate();
                loggedIn = true;
            } catch (JSONException e) {
                Logger.e(e.toString());
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onLocationUpdated(LocationUpdateServiceResult result) {
        hasGpsLocation = true;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onConversationsUpdate(ConversationsFromApiResult result) {
        collectMessagesComplete = true;
    }
}
