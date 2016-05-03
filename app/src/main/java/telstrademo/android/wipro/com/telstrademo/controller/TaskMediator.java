package telstrademo.android.wipro.com.telstrademo.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;

import telstrademo.android.wipro.com.telstrademo.backgroundtasks.ImageDownloaderTask;
import telstrademo.android.wipro.com.telstrademo.backgroundtasks.NewsFeedDownloaderTask;
import telstrademo.android.wipro.com.telstrademo.interfaces.INetworkFeedCallback;
import telstrademo.android.wipro.com.telstrademo.util.ApplicationCache;
import telstrademo.android.wipro.com.telstrademo.util.Constants;

/**
 * Created by Nilanjan Biswas on 05/02/2016.
 */
public class TaskMediator implements INetworkFeedCallback {
    private static final String TAG = "TELSTRA_TaskMediator";

    private static TaskMediator ourInstance = null;
    private static ArrayList<INetworkFeedCallback> mCallbackList = new ArrayList<INetworkFeedCallback>();

    private Hashtable<String,INetworkFeedCallback> mImageLoaderMap = new Hashtable<String,INetworkFeedCallback>();


    private Context mContext;

    private TaskMediator(Context context) {
        mContext = context;
    }

    public static synchronized TaskMediator getInstance(Context context) {
        if(null == ourInstance){
            ourInstance = new TaskMediator(context);
        }
        return ourInstance;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void registerImageListener(String url,INetworkFeedCallback callback){
        mImageLoaderMap.put(url,callback);
    }

    public void unregisterImageListener(String url){
        mImageLoaderMap.remove(url);
    }

    public void registerDataListener(INetworkFeedCallback instance){
        boolean isAlreadyRegistered = false;
        for(INetworkFeedCallback callback : mCallbackList){
            if(callback.getClass().equals(instance.getClass())){
                isAlreadyRegistered = true;
            }
        }
        if(!isAlreadyRegistered)
            mCallbackList.add(instance);
    }

    public void unregisterDataListener(INetworkFeedCallback instance){
        for(INetworkFeedCallback callback : mCallbackList){
            if(callback.getClass().equals(instance.getClass())){
                mCallbackList.remove(callback);
            }
        }
    }



    public void doAction(int action, Bundle actionData, INetworkFeedCallback callback){
        Log.d(TAG,"Action = "+action);
        switch(action){
            case Constants.ACTION_FETCH_NEWS : {
                //if cache is available,display cached data and simultaneously
                //proceed with serve fetch operation foe new data
                String cachedNewsData = ApplicationCache.getInstance(mContext).getNewsFeedCache();
                if(cachedNewsData != null && cachedNewsData.length()>0){
                    callback.onDataReceived(cachedNewsData);
                    Log.d(TAG,"News found in cache ");
                }
                registerDataListener(callback);
                new NewsFeedDownloaderTask(this).execute(actionData.getString(Constants.KEY_NEWS_URL));
                break;
            }
            case Constants.ACTION_FETCH_IMAGE : {
                String url = actionData.getString(Constants.KEY_IMAGE_URL);
                if(null != url && url.length()>0){
                    Bitmap tmp = ApplicationCache.getInstance(mContext).getBitmap(url);
                    if(tmp != null){
                        Log.d(TAG,"IMAGE found in cache url = "+url);
                        if(callback != null)
                            callback.onImageDownloaded(url,tmp);
                    }else{
                        if(callback != null){
                            registerImageListener(url, callback);
                        }
                        new ImageDownloaderTask(this).execute(url);
                    }
                }else {
                    //onImageDownloadError(url,Constants.ERROR_BADURL);
                }
                break;
            }
            default:
                break;

        }
    }


    @Override
    public void onDataReceived(String jsonData) {
        if(jsonData != null && jsonData.length()>0){
            ApplicationCache.getInstance(mContext).cacheNewsFeed(jsonData);
        }
        for(INetworkFeedCallback callback : mCallbackList){
            callback.onDataReceived(jsonData);
        }
    }

    @Override
    public void onImageDownloaded(String url, Bitmap image) {
        Log.d(TAG,"onImageDownloaded::url = "+url+" image = "+image);
        if(image != null){
            ApplicationCache.getInstance(mContext).cacheBitmap(url,image);
            if(mImageLoaderMap.get(url) != null){
                mImageLoaderMap.get(url).onImageDownloaded(url,image);
            }
        }else{
            if(mImageLoaderMap.get(url) != null){
                mImageLoaderMap.get(url).onImageDownloadError(url,Constants.ERROR_GENERIC);
            }
        }


    }

    @Override
    public void onDataDownloadError(int errorCode) {
        for(INetworkFeedCallback callback : mCallbackList){
            callback.onDataDownloadError(errorCode);
        }
    }

    @Override
    public void onImageDownloadError(String url, int errorCode) {
        mImageLoaderMap.get(url).onImageDownloadError(url,errorCode);
    }
}
