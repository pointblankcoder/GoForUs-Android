package info.goforus.goforus.event_results;

import info.goforus.goforus.models.accounts.Account;

public class UpdatedAccountResult {

    private final Account mAccount;

    public UpdatedAccountResult(Account account) { mAccount = account; }

    public Account getAccount() { return mAccount; }
}