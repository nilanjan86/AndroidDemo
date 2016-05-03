package telstrademo.android.wipro.com.telstrademo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Nilanjan Biswas on 05/02/2016.
 */
public class Utils {

    private static final String TAG = "Utils";

    public static boolean isNetworkConnectionAvailable(Context context) {
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            Log.e(TAG, "checkConnection - no connection found");
            return false;
        }
        return true;
    }

}
