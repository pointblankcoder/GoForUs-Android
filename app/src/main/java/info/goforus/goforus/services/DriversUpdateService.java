package info.goforus.goforus.services;

import android.app.IntentService;
import android.content.Intent;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.models.accounts.Account;

public class DriversUpdateService extends IntentService {
    public DriversUpdateService() {
        super("DriversUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Account account = Account.currentAccount();
        if (account != null) {
            Utils.LOCATIONS_API.getNearbyDrivers(account.lat, account.lng);
        }
    }
}
