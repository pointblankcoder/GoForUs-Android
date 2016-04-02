package info.goforus.goforus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.tasks.LocationUpdateHandler;

public class BrandExposure extends BaseActivity {
    Handler mHandler = new Handler();
    static final int POLL_INTERVAL = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_exposure);
        mHandler.postDelayed(readyCheckRunnable, POLL_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(readyCheckRunnable);
    }

    /* start: Check is the application is ready */
    Runnable readyCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if(mApplication.isReady()) {
                Intent intent;
                if (Account.currentAccount() != null) {
                    LocationUpdateHandler.getInstance().turnUpdatesOn();
                    intent = new Intent(BrandExposure.this, NavigationActivity.class);
                }else {
                    intent = new Intent(BrandExposure.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            } else {
                mHandler.postDelayed(readyCheckRunnable, POLL_INTERVAL);
            }
        }
    };
    /* end: Check is the application is ready */
}
