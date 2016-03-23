package info.goforus.goforus;

import android.content.Intent;
import android.os.Bundle;

import info.goforus.goforus.models.account.Account;

public class BrandExposure extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_exposure);
    }

    @Override
    protected void onStart() {
        super.onStart();

        new Thread(new Runnable() {
            public void run() {
                while (!mApplication.isReady()) {
                }

                Intent intent;
                if (Account.currentAccount() != null) {
                    intent = new Intent(BrandExposure.this, NavigationActivity.class);
                }else {
                    intent = new Intent(BrandExposure.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }).start();
    }
}
