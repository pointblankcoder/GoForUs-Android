package info.goforus.goforus.models.drivers;

@org.parceler.Parcel
public class Information  {
    public Integer externalId;
    public String name;
    public String email;
    public double lat;
    public double lng;
    public String mobileNumber;
    public Integer rating;

    Information(){}

    Information(Driver driver){
        this.externalId = driver.externalId;
        this.name = driver.name;
        this.email = driver.email;
        this.lat = driver.lat;
        this.lng = driver.lng;
        this.mobileNumber = driver.mobileNumber;
        this.rating = driver.rating;
    }
}
