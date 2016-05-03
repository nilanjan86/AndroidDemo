package telstrademo.android.wipro.com.telstrademo.backgroundtasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import telstrademo.android.wipro.com.telstrademo.interfaces.INetworkFeedCallback;

/**
 * Created by Nilanjan Biswas on 05/02/2016.
 */
public class ImageDownloaderTask extends AsyncTask<String,Void,Bitmap>{

    private static final String TAG = "TELSTRA_IDT";

    private INetworkFeedCallback mNetworkFeedCallback = null;
    private String mUrl = null;

    private static final int IO_BUFFER_SIZE = 8 * 1024;

    public ImageDownloaderTask(INetworkFeedCallback instance) {
        mNetworkFeedCallback = instance;
    }

    @Override
    protected Bitmap doInBackground(String... url) {
        mUrl = url[0];
        Log.d(TAG,"doInBackground::mUrl = "+mUrl);
        return downloadBitmap(mUrl);
    }

    @Override
    protected void onPostExecute(Bitmap image) {
        super.onPostExecute(image);
        Log.d(TAG,"onPostExecute::image = "+image);
        mNetworkFeedCallback.onImageDownloaded(mUrl,image);
    }

    public Bitmap downloadBitmap(String urlString/*, OutputStream outputStream*/) {
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);

            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            if(responseCode == 301){
                Log.d(TAG," responseCode = "+ responseCode);
                String location = urlConnection.getHeaderField("Location");
                Log.d(TAG," location = "+ location);
                url = new URL(location);
                urlConnection = (HttpURLConnection) url.openConnection();
            }

            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);

        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
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
        return bitmap;
    }


}
