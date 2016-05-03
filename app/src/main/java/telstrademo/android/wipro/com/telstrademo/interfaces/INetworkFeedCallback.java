package telstrademo.android.wipro.com.telstrademo.interfaces;

import android.graphics.Bitmap;

/**
 * Created by Nilanjan Biswas on 05/02/2016.
 */
public interface INetworkFeedCallback {

    public void onDataReceived(String jsonData);

    public void onImageDownloaded(String url, Bitmap image);

    public void onDataDownloadError(int errorCode);

    public void onImageDownloadError(String url, int errorCode);

}
