package info.goforus.goforus.event_results;

import info.goforus.goforus.models.drivers.Driver;

public class DriverUpdateResult {

    Driver mDriver;

    public DriverUpdateResult(Driver driver) { mDriver = driver; }

    public Driver getDriver() { return mDriver; }
}
