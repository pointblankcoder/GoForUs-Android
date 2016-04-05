package info.goforus.goforus.settings;

public class DebugSettings {
    private static DebugSettings ourInstance = new DebugSettings();
    public static DebugSettings getInstance() {
        return ourInstance;
    }

    private DebugSettings() {
    }

    private String mApiUrl = "http://dev.goforus.info/api/v1/";

    public String getApiUrl() {
        return mApiUrl;
    }

    public void setApiUrl(String url) {
        mApiUrl = url;
    }
}
