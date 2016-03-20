package info.goforus.goforus;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import info.goforus.goforus.tasks.BrandExposureTask;

public class BrandExposure extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_exposure);

        BrandExposureTask task = new BrandExposureTask();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
        } else {
            task.execute(this);
        }
    }
}
