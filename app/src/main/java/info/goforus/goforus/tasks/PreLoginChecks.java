package info.goforus.goforus.tasks;

import android.content.res.Resources;
import android.os.AsyncTask;

import info.goforus.goforus.Application;

public class PreLoginChecks extends AsyncTask<Object, String, Float[]> {

    public PreLoginChecks() {
    }

    @Override
    protected void onPreExecute() {
    }

    /*
     * Required
     *      Check GPS Signal
     *      Check Current location
     *      Check connectivity status of network
     * Preferred:-
     *      Sync account details
     *      Sync messages
     */
    protected Float[] doInBackground(Object... objects) {
        final Application application = Application.getInstance();
        final Resources resources = Application.getInstance().getResources();


        return null;
    }


    protected void onProgressUpdate(String... status) {
    }


    protected void onPostExecute(Float[] xyh) {
    }
}
