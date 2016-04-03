package info.goforus.goforus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import info.goforus.goforus.event_results.LocationUpdateServiceResult;
import info.goforus.goforus.event_results.LoginFromApiResult;
import info.goforus.goforus.jobs.AttemptLoginJob;
import info.goforus.goforus.jobs.AttemptRegisterJob;
import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.tasks.LocationUpdateHandler;
import us.monoid.json.JSONException;

public class LoginActivity extends BaseActivity {

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private boolean cancelAttempt;
    private View currentFocusView;
    private Button mLoginButton;
    private Button mRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginFormView = findViewById(R.id.login_form);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginButton = (Button) findViewById(R.id.signInButton);
        mRegisterButton = (Button) findViewById(R.id.registerButton);
        mProgressView = findViewById(R.id.login_progress);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    final String email = mEmailView.getText().toString();
                    final String password = mPasswordView.getText().toString();
                    checkValidity(email, password);
                    attemptLogin(email, password);
                    return true;
                }
                return false;
            }
        });

        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmailView.getText().toString();
                final String password = mPasswordView.getText().toString();
                checkValidity(email, password);
                attemptLogin(email, password);
            }
        });

        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmailView.getText().toString();
                final String password = mPasswordView.getText().toString();
                checkValidity(email, password);
                attemptRegister(email, password);
                // TODO: Attempt register
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }


    // TODO: check text color of the error message
    private void checkValidity(final String email, final String password) {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        cancelAttempt = false;
        currentFocusView = null;

        if (!isPasswordValid(password)) {
            showErrorOn(mPasswordView, getString(R.string.error_invalid_password));
            currentFocusView = mPasswordView;
            cancelAttempt = true;
        }

        if (!isEmailValid(email)) {
            showErrorOn(mEmailView, getString(R.string.error_field_required));
            currentFocusView = mEmailView;
            cancelAttempt = true;
        }
    }

    // TODO: Add better email validation
    private boolean isEmailValid(String email) {
        return email.contains("@") && !TextUtils.isEmpty(email);
    }

    // TODO: Add better password validation
    private boolean isPasswordValid(String password) {
        return  password.length() >= 3&& !TextUtils.isEmpty(password);
    }


    private void attemptLogin(final String email, final String password) {
        if (cancelAttempt) {
            currentFocusView.requestFocus();
        } else {
            showProgress(true);
            Application.getInstance().getJobManager().addJobInBackground(new AttemptLoginJob(email, password));
        }
    }

    private void attemptRegister(final String email, final String password) {
        if (cancelAttempt) {
            currentFocusView.requestFocus();
        } else {
            showProgress(true);
            Application.getInstance().getJobManager().addJobInBackground(new AttemptRegisterJob(email, password));
        }
    }

    private void showProgress(final boolean show) {
        View focus = getCurrentFocus();
        if (focus != null) {
            focus.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showErrorOn(TextView view, String errorMessage) {
        view.setError(null);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.accent_material_dark_1));
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(errorMessage);
        ssBuilder.setSpan(colorSpan, 0, errorMessage.toString().length(), 0);
        view.setError(ssBuilder);
        view.requestFocus();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginResult(LoginFromApiResult result) {

        if (result.hasFailure()) {
            showProgress(false);

            if (result.failedEmailMessage() != null) {
                showErrorOn(mEmailView, result.failedEmailMessage());
            }

            if (result.failedPasswordMessage() != null) {
                showErrorOn(mPasswordView, result.failedPasswordMessage());
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
            } catch (JSONException e) {
                Logger.e(e.toString());
            }
        }
    }

    // TODO: Replace this with PreLogin Task method to check for location updates
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationUpdated(LocationUpdateServiceResult result) {
        if (Account.currentAccount() != null && Account.currentAccount().hasLocation()) {
            Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
