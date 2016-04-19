package info.goforus.goforus.event_results;

public class IndicatorUpdateResult {

    public float x;
    public float y;
    public float heading;
    public float viewId;

    public IndicatorUpdateResult(float x, float y, float heading, int viewId) {
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.viewId = viewId;
    }
}
