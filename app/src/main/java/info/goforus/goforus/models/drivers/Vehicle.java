package info.goforus.goforus.models.drivers;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.orhanobut.logger.Logger;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

@Table(name = "Vehicles")
public class Vehicle extends Model {

    @Column(name = "externalId", index = true) public Integer externalId;
    @Column(name = "registration") public String registration;
    @Column(name = "make") public String make;
    @Column(name = "model") public String model;
    @Column(name = "vehicleType") public String vehicleType;
    @Column(name = "onlineWith") public boolean onlineWith;
    @Column(name = "Driver") public long driver;

    public Vehicle() { super(); }

    public Vehicle(JSONObject json) {
        super();

        try {
            this.externalId = json.getInt("id");
            this.registration = json.getString("registration");
            this.make = json.getString("make");
            this.model = json.getString("model");
            this.vehicleType = json.getString("vehicle_type");
            this.onlineWith = json.getBoolean("online_with");
        } catch (Exception e) {
            Logger.e(e.toString());
        }
    }


    public static Vehicle updateOrCreateFromJson(JSONObject json) {
        int externalId = 0;

        try {
            externalId = json.getInt("id");
        } catch (JSONException e) {
            Logger.e(e.toString());
        }

        Vehicle existingVehicle =
                new Select().from(Vehicle.class).where("externalId = ?", externalId).executeSingle();
        if (existingVehicle != null) {
            Vehicle vehicle = new Vehicle(json);
            if(vehicle.registration != existingVehicle.registration) existingVehicle.registration = vehicle.registration;
            if(vehicle.make != existingVehicle.make) existingVehicle.make = vehicle.make;
            if(vehicle.model != existingVehicle.model) existingVehicle.model = vehicle.model;
            if(vehicle.onlineWith != existingVehicle.onlineWith) existingVehicle.onlineWith = vehicle.onlineWith;
            if(vehicle.vehicleType != existingVehicle.vehicleType) existingVehicle.vehicleType = vehicle.vehicleType;
            existingVehicle.save();
            return existingVehicle;
        } else {
            Vehicle vehicle = new Vehicle(json);
            vehicle.save();
            return vehicle;
        }
    }
}
