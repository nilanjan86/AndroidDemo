package telstrademo.android.wipro.com.telstrademo.backgroundtasks;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import telstrademo.android.wipro.com.telstrademo.interfaces.INetworkFeedCallback;
import telstrademo.android.wipro.com.telstrademo.util.Constants;
import telstrademo.android.wipro.com.telstrademo.util.LogManager;

/**
 * Created by Nilanjan Biswas on 05/02/2016.
 */
public class NewsFeedDownloaderTask  extends AsyncTask<String,Void,String> {

    private static final String TAG = "TELSTRA_NDT";

    private INetworkFeedCallback mNetworkFeedCallback = null;

    public NewsFeedDownloaderTask(INetworkFeedCallback instance) {
        mNetworkFeedCallback = instance;
    }

    @Override
    protected String doInBackground(String... url) {
        return getJSONFromUrl(url[0]);
    }


    @Override
    protected void onPostExecute(String newsJson) {
        super.onPostExecute(newsJson);
        if(newsJson == null || newsJson.length() == 0){
            mNetworkFeedCallback.onDataDownloadError(Constants.ERROR_GENERIC);
        }else{
            mNetworkFeedCallback.onDataReceived(newsJson);
        }
    }

    public String getJSONFromUrl(String newsurl) {
        String jsonString = "";
        URL url = null;
        HttpURLConnection urlConnection = null;
        InputStream in = null;
        try {
            url = new URL(newsurl);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    in, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            jsonString = sb.toString();
        } catch (MalformedURLException e) {
            LogManager.e(TAG,"Error = "+ e);
        } catch (IOException e) {
            LogManager.e(TAG,"Error = "+ e);
        } finally {
            try {
                in.close();
                urlConnection.disconnect();
            } catch (Exception e) {
                LogManager.e(TAG,"Error = "+ e);
            }
        }
        return jsonString;
    }

}