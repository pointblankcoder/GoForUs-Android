package info.goforus.goforus.services;

import android.app.IntentService;
import android.content.Intent;

import info.goforus.goforus.Application;
import info.goforus.goforus.jobs.GetConversationsJob;
import info.goforus.goforus.models.accounts.Account;

public class ConversationsUpdateService extends IntentService {
    private Account mAccount;


    public ConversationsUpdateService() {
        super("ConversationsUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mAccount = Account.currentAccount();

        if (mAccount != null) {
            Application.getInstance().getJobManager().addJobInBackground(new GetConversationsJob());
        }
    }
}

