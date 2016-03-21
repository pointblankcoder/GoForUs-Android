package info.goforus.goforus;

import android.content.Intent;
import android.os.Bundle;

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

                // Also on UI thread, executed once doInBackground()
                // finishes.
                Intent intent = new Intent(BrandExposure.this, AvailabilityActivity.class);
                startActivity(intent);
            }
        }).start();
    }
}
