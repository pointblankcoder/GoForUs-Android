package info.goforus.goforus.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import info.goforus.goforus.AvailabilityActivity;

public class BrandExposureTask extends AsyncTask<AppCompatActivity, Void, Void> {
    AppCompatActivity activity;

    @Override
    protected Void doInBackground(AppCompatActivity... params) {
        // This method is running off the UI thread.
        // Safe to stop execution here.
        activity = params[0];

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
        // This method is running on the UI thread.
        // Do not stop thread here, but safe to modify the UI.
    }

    @Override
    protected void onPostExecute(Void v) {
        // Also on UI thread, executed once doInBackground()
        // finishes.
        Intent intent = new Intent(activity, AvailabilityActivity.class);
        Log.d("BrandExposureTask", "Starting Activity - AvailabilityActivity");
        activity.startActivity(intent);
    }
}
