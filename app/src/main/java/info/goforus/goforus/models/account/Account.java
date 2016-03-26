package info.goforus.goforus.models.account;

import com.orm.SugarRecord;

import us.monoid.json.JSONObject;

public class Account extends SugarRecord {
    Integer external_id;
    public String name;
    public String email;
    public String password;
    public String phoneNumber;
    public String apiToken;
    public double lat;
    public double lng;

    public Account(){

    }

    public Account(JSONObject accountObject){
        try {
            this.external_id = Integer.parseInt(accountObject.get("id").toString());
            this.email = accountObject.get("email").toString();
            this.apiToken = accountObject.get("authentication_token").toString();
            if (!accountObject.get("mobile_number").equals(null))
                this.phoneNumber = accountObject.get("mobile_number").toString();
            if (!accountObject.get("name").equals(null))
                this.name = accountObject.get("name").toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Account(Integer external_id, String name, String email, String password, String phoneNumber, String apiToken){
        this.external_id = external_id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.apiToken = apiToken;
    }

    public static Account currentAccount() {
        Account account = Account.last(Account.class);
        return account;
    }

    public void updateLocation(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
        this.save();
    }
}
