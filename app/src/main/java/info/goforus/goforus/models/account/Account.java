package info.goforus.goforus.models.account;

import com.orm.SugarRecord;

public class Account extends SugarRecord {
    Integer external_id;
    public String name;
    public String email;
    public String password;
    public String phoneNumber;
    public String apiToken;

    public Account(){

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
}
