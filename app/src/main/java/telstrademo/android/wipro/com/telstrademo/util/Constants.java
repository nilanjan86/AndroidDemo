package telstrademo.android.wipro.com.telstrademo.util;

/**
 * Created by Nilanjan Biswas on 05/02/2016.
 */
public class Constants {

    public static final int ACTION_FETCH_NEWS = 1;
    public static final int ACTION_FETCH_IMAGE = 2;

    public static final String NEWS_FEED_URL = "https://dl.dropboxusercontent.com/u/746330/facts.json";

    public static final String KEY_NEWS_URL = "KEY_NEWS_URL";
    public static final String KEY_IMAGE_URL = "KEY_IMAGE_URL";
    public static final String KEY_NEWSFEED_CACHE = "KEY_NEWSFEED_CACHE";

    public static final String KEY_FEED_TITLE = "title";
    public static final String KEY_JSON_ROW = "rows";


    public static final int ERROR_GENERIC = 1;

    public static final String SHARED_PREF_FILE = "TELSTRA_PREF";

    public static final int LOG_LEVEL_VERBOSE = 1;
    public static final int LOG_LEVEL_INFO = 2;
    public static final int LOG_LEVEL_DEBUG = 3;
    public static final int LOG_LEVEL_WARNING = 4;
    public static final int LOG_LEVEL_ERROR = 5;

    public static int LOG_LEVEL = LOG_LEVEL_ERROR;

}
