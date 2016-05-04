package telstrademo.android.wipro.com.telstrademo.util;

import android.util.Log;

/**
 * Created by nbiswa on 5/5/2016.
 */
public class LogManager {

    public static void v(String TAG, String msg) {
        if (Constants.LOG_LEVEL <= Constants.LOG_LEVEL_VERBOSE) {
            Log.v(TAG, msg);
        }
    }

    public static void i(String TAG, String msg) {
        if (Constants.LOG_LEVEL <= Constants.LOG_LEVEL_INFO) {
            Log.i(TAG, msg);
        }
    }

    public static void d(String TAG, String msg) {
        if (Constants.LOG_LEVEL <= Constants.LOG_LEVEL_DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void w(String TAG, String msg) {
        if (Constants.LOG_LEVEL <= Constants.LOG_LEVEL_WARNING) {
            Log.w(TAG, msg);
        }
    }

    public static void e(String TAG, String msg) {
        if (Constants.LOG_LEVEL <= Constants.LOG_LEVEL_ERROR) {
            Log.e(TAG, msg);
        }
    }

}