package info.goforus.goforus.event_results;

import android.net.NetworkInfo;

public class NetworkUpdateResult {

    private final NetworkInfo mActiveNetwork;
    private final boolean mIsConnected;

    public NetworkUpdateResult(NetworkInfo activeNetwork, boolean isConnected) {
        mActiveNetwork = activeNetwork;
        mIsConnected = isConnected;
    }

    public boolean getIsConnected() { return mIsConnected; }

    public NetworkInfo getActveNetwork() { return mActiveNetwork; }
}
