package telstrademo.android.wipro.com.telstrademo.backgroundtasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import telstrademo.android.wipro.com.telstrademo.interfaces.INetworkFeedCallback;
import telstrademo.android.wipro.com.telstrademo.util.LogManager;

/**
 * Created by Nilanjan Biswas on 05/02/2016.
 */
public class ImageDownloaderTask extends AsyncTask<String,Void,Bitmap>{

    private static final String TAG = "TELSTRA_IDT";

    private INetworkFeedCallback mNetworkFeedCallback = null;
    private String mUrl = null;
    private static final int IO_BUFFER_SIZE = 20 * 1024;

    public ImageDownloaderTask(INetworkFeedCallback instance) {
        mNetworkFeedCallback = instance;
    }

    @Override
    protected Bitmap doInBackground(String... url) {
        mUrl = url[0];
        LogManager.d(TAG,"doInBackground::mUrl = "+mUrl);
        return downloadBitmap(mUrl);
    }

    @Override
    protected void onPostExecute(Bitmap image) {
        super.onPostExecute(image);
        LogManager.d(TAG,"onPostExecute::image = "+image);
        mNetworkFeedCallback.onImageDownloaded(mUrl,image);
    }

    public Bitmap downloadBitmap(String urlString) {
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        Bitmap bitmap = null;
        URL url = null;
        WeakReference<Bitmap> wrb = null;
        try {
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            if(responseCode == 301){
                LogManager.d(TAG," responseCode = "+ responseCode);
                String location = urlConnection.getHeaderField("Location");
                LogManager.d(TAG," REDIRECT URL = "+ location);
                url = new URL(location);
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
            wrb = new WeakReference<Bitmap>(bitmap);
        } catch (final IOException e) {
            LogManager.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {}
        }
        if(bitmap != null)//might need to implement retry logic if required
            return wrb.get();
        else
            return null;
    }


}
