package info.goforus.goforus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import info.goforus.goforus.tasks.ProcessLogin;

public class LoginActivity extends BaseActivity {

    @Bind(R.id.email) public AutoCompleteTextView mEmailView;
    @Bind(R.id.password) public EditText mPasswordView;
    @Bind(R.id.login_form) public View mLoginFormView;
    @Bind(R.id.tvLoginStatus) public TextView tvLoginStatus;
    @Bind(R.id.login_progress) View mProgressView;
    @Bind(R.id.progressWrapper) RelativeLayout progressWrapper;

    private boolean cancelAttempt;
    private View currentFocusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.signInButton)
    public void onLoginClick(){
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();
        checkValidity(email, password);
        attemptLogin(email, password);
    }


    @OnClick(R.id.registerButton)
    public void onRegisterClick(){
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();
        checkValidity(email, password);
        attemptRegister(email, password);
    }

    @OnEditorAction(R.id.password)
    public boolean onPasswordEditorAction(TextView textView, int id, KeyEvent keyEvent){
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
            final String email = mEmailView.getText().toString();
            final String password = mPasswordView.getText().toString();
            checkValidity(email, password);
            attemptLogin(email, password);
            return true;
        }
        return false;
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
        super.onResume();
    }

    @Override
    public void onPause() {
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
        return password.length() >= 3 && !TextUtils.isEmpty(password);
    }


    private void attemptLogin(final String email, final String password) {
        if (cancelAttempt) {
            currentFocusView.requestFocus();
        } else {
            showProgress(true);
            new ProcessLogin(this, email, password, false).execute();
        }
    }

    private void attemptRegister(final String email, final String password) {
        if (cancelAttempt) {
            currentFocusView.requestFocus();
        } else {
            showProgress(true);
            new ProcessLogin(this, email, password, true).execute();
        }
    }

    public void showProgress(final boolean show) {
        View focus = getCurrentFocus();
        if (focus != null) {
            focus.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }

        if (show) {
            progressWrapper.setVisibility(View.VISIBLE);
            tvLoginStatus.setVisibility(View.VISIBLE);
        } else {
            progressWrapper.setVisibility(View.GONE);
            tvLoginStatus.setVisibility(View.GONE);
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

    public void showErrorOn(TextView view, String errorMessage) {
        view.setError(null);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.accent_material_dark_1));
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(errorMessage);
        ssBuilder.setSpan(colorSpan, 0, errorMessage.toString().length(), 0);
        view.setError(ssBuilder);
        view.requestFocus();
    }
}
