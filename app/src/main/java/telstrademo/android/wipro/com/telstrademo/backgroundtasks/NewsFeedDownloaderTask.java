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

/**
 * Created by Nilanjan Biswas on 05/02/2016.
 */
public class NewsFeedDownloaderTask  extends AsyncTask<String,Void,String> {
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
        mNetworkFeedCallback.onDataReceived(newsJson);
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                in.close();
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jsonString;
    }

}