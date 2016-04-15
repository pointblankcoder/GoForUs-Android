package info.goforus.goforus.tasks;

import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GcmPubSub;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import info.goforus.goforus.GoForUs;
import info.goforus.goforus.LoginActivity;
import info.goforus.goforus.NavigationActivity;
import info.goforus.goforus.event_results.ConversationsFromApiResult;
import info.goforus.goforus.event_results.LocationUpdateServiceResult;
import info.goforus.goforus.event_results.LoginFromApiResult;
import info.goforus.goforus.jobs.LoginJob;
import info.goforus.goforus.jobs.GetConversationsJob;
import info.goforus.goforus.managers.GCMTokenManager;
import info.goforus.goforus.models.accounts.Account;
import us.monoid.json.JSONException;

public class ProcessLogin extends AsyncTask<Object, String, Void> {
    private static final String[] TOPICS = {"global", "conversations", "messages", "jobs"};

    private final LoginActivity mLoginActivity;
    private final String mEmail;
    private final String mPassword;
    private final boolean mRegistering;
    private final ProcessLogin me;
    private boolean loggedIn = false;
    private boolean collectMessagesComplete = false;
    private boolean hasGpsLocation;

    public ProcessLogin(LoginActivity activity, String email, String password, boolean registering) {
        this.mLoginActivity = activity;
        this.mEmail = email;
        this.mPassword = password;
        this.mRegistering = registering;
        this.me = this;
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPreExecute() {
    }

    protected Void doInBackground(Object... objects) {
        final GoForUs goForUs = GoForUs.getInstance();

        if (mRegistering) {
            publishProgress("Getting you registered");
            goForUs.getJobManager()
                   .addJobInBackground(new GetConversationsJob.AttemptRegisterJob(mEmail, mPassword));
        } else {
            publishProgress("Logging In");
            goForUs.getJobManager().addJobInBackground(new LoginJob(mEmail, mPassword));
        }


        while (!loggedIn && !isCancelled()) {
        }

        if (!isCancelled()) {
            publishProgress("Getting your messages");
            GoForUs.getInstance().getJobManager().addJobInBackground(new GetConversationsJob());
        }

        while (!collectMessagesComplete && !isCancelled()) {
        }

        if (!isCancelled()) {
            publishProgress("Trying to find where you are");
            LocationUpdateHandler.getInstance().forceUpdate();
        }

        int count = 0;
        while (!hasGpsLocation && !isCancelled()) {
            if (count > 10) {
                // this catches and keeps our loop going if we are having trouble finding the local
                // without publishing constantly
            } else if (count == 10) {
                publishProgress("We are having trouble finding your location \n Moving around can help if you're using GPS");
                count++; // push the count over so we don't keep publishing our progress
            } else {
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // we don't care
                }
            }
        }

        publishProgress("Seeing if Google likes you");

        if (!GCMTokenManager.getInstance().update()) cancel(true);
        try {
            subscribeTopics();
        } catch (IOException e) {
            Logger.e(e.toString());
        }

        if (!isCancelled()) {
            publishProgress("All done, enjoy!");
        }

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
                mLoginActivity
                        .showErrorOn(mLoginActivity.mPasswordView, result.failedPasswordMessage());
            }

            // TODO: add base message failures, this can happen if there's no server response,
            //       unknown error has happened (parsing json for example), or we do not support the error response just yet
            // mBaseMessageView.setError(result.failedMessage())

            me.cancel(true);
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

    private void subscribeTopics() throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(GoForUs.getInstance());
        if (pubSub != null && Account.currentAccount() != null) {
            for (String topic : TOPICS) {
                pubSub.subscribe(Account.currentAccount().gcmToken, "/topics/" + topic, null);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onLocationUpdated(LocationUpdateServiceResult result) { hasGpsLocation = true; }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onConversationsUpdate(ConversationsFromApiResult result) {
        collectMessagesComplete = true;
    }
}
