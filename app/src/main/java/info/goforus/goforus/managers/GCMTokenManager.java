package info.goforus.goforus.managers;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.orhanobut.logger.Logger;

import java.io.IOException;

import info.goforus.goforus.GoForUs;
import info.goforus.goforus.R;
import info.goforus.goforus.models.accounts.Account;

public class GCMTokenManager {
    private static GCMTokenManager ourInstance = new GCMTokenManager();

    public static GCMTokenManager getInstance() {
        return ourInstance;
    }

    private GCMTokenManager() {
    }

    public boolean update(){
        try {
            InstanceID instanceID = InstanceID.getInstance(GoForUs.getInstance().getCurrentActivity());
            String token = instanceID.getToken(GoForUs.getInstance().getCurrentActivity()
                    .getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            // TODO: Send the token to the sever
            Account account = Account.currentAccount();
            account.gcmToken = token;
            account.save();
            return true;
        } catch (IOException e) {
            Logger.d("Failed to complete token refresh", e);
            return false;
        }

    }
}
