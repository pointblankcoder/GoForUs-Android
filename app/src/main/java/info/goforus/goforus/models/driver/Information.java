package info.goforus.goforus.models.driver;

import android.os.Parcel;
import android.os.Parcelable;

public class Information implements Parcelable {
    public Integer externalId;
    public String name;
    public String email;
    public double lat;
    public double lng;
    public String short_bio;
    public String mobile_number;
    public Integer rating;

    Information(Driver driver){
        this.externalId = driver.externalId;
        this.name = driver.name;
        this.email = driver.email;
        this.lat = driver.lat;
        this.lng = driver.lng;
        this.short_bio = driver.short_bio;
        this.mobile_number = driver.mobile_number;
        this.rating = driver.rating;
    }

    /* ===================== Parceable ======================= */
    protected Information(Parcel in) {
        externalId = in.readByte() == 0x00 ? null : in.readInt();
        rating = in.readByte() == 0x00 ? null : in.readInt();
        name = in.readString();
        email = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        short_bio = in.readString();
        mobile_number = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (externalId == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(externalId);
        }

        if (rating == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(rating);
        }
        dest.writeString(name);
        dest.writeString(email);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeString(short_bio);
        dest.writeString(mobile_number);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Information> CREATOR = new Parcelable.Creator<Information>() {
        @Override
        public Information createFromParcel(Parcel in) {
            return new Information(in);
        }

        @Override
        public Information[] newArray(int size) {
            return new Information[size];
        }
    };
}
