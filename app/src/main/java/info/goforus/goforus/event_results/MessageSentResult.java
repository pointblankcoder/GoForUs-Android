package info.goforus.goforus.event_results;

public class MessageSentResult {
    private final int mResultCode;
    private final String mMessage;
    public static int RESULT_FAILURE = 0;
    public static int RESULT_OK = 1;

    public MessageSentResult(int resultCode, String message){
        mResultCode = resultCode;
        mMessage = message;
    }

    public int getResultCode(){
        return mResultCode;
    }

    public String getMessage(){
        return mMessage;
    }
}
