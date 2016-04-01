package info.goforus.goforus.event_results;

import com.orhanobut.logger.Logger;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class LoginFromApiResult {
    final JSONObject mResponse;
    final boolean failed;
    String mFailedEmailMessage;
    String mFailedPasswordMessage;
    String mFailedMessage;

    public LoginFromApiResult(JSONObject response) {
        mResponse = response;
        if (response.has("error") || response == null) {
            failed = true;
            try {
                JSONObject error = response.getJSONObject("error");
                if(error.has("password")){
                    mFailedPasswordMessage = error.getString("password");
                } else if (error.has("email")){
                    mFailedEmailMessage = error.getString("email");
                } else if (error.has("base")) {
                    Logger.e("Base errors come from our internal api interaction only. Something went wrong: \n %s", response);
                    mFailedMessage = error.getString("base");
                } else {
                    mFailedMessage = "Something went wrong. Please try again.";
                }
            } catch (JSONException e) {
                Logger.e(e.toString());
                mFailedMessage = "An unknown error has occurred, please contact pointblankcoder@gmail.com with more information to report this issue.";
            }

        } else {
            failed = false;
        }
    }
    public String failedMessage() {
        return mFailedMessage;
    }

    public String failedEmailMessage() {
        return mFailedEmailMessage;
    }

    public String failedPasswordMessage() {
        return mFailedPasswordMessage;
    }

    public boolean hasFailure() {
        return failed;
    }

    public JSONObject getResponse(){
        return mResponse;
    }
}

