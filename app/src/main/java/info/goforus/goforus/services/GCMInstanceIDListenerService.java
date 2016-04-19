package info.goforus.goforus.services;

import com.google.android.gms.iid.InstanceIDListenerService;

import info.goforus.goforus.managers.GCMTokenManager;

public class GCMInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "GCMInstanceIDListenerService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        GCMTokenManager.getInstance().update();
    }
}

