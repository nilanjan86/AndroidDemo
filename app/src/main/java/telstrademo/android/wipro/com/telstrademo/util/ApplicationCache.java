package telstrademo.android.wipro.com.telstrademo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Nilanjan Biswas on 05/02/2016.
 */
public class ApplicationCache {

    private static final String TAG = "TELSTRA_AppCache";

    private static ApplicationCache ourInstance ;

    private LruCache<String, Bitmap> mLRUCache;
    private static final String CACHE_DIR = "telstra_cache";
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;
    private static final int DISK_CACHE_INDEX = 0;

    private DiskLruCache mDiskLruCache;
    private File mCacheDir;

    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;

    private Context mContext;
    private SharedPreferences mSharedPref;

    private ApplicationCache(Context context) {
        mContext = context;
        init(mContext);
    }

    public static synchronized ApplicationCache getInstance(Context context) {
        if(null == ourInstance){
            ourInstance = new ApplicationCache(context);
        }
        return ourInstance;
    }


    public void cacheNewsFeed(String newsJsonString){
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(Constants.KEY_NEWSFEED_CACHE, newsJsonString);
        editor.commit();
    }

    public String getNewsFeedCache(){
        return mSharedPref.getString(Constants.KEY_NEWSFEED_CACHE,"");
    }

    public void cacheBitmap(String key, Bitmap bitmap) {
        addBitmapToLRUCache(key, bitmap);
        addBitmapToDiskCache(key, bitmap);
    }

    public Bitmap getBitmap(String key){
        Bitmap temp = getBitmapFromLRUCache(key);
        if(temp == null) {
            temp = getBitmapFromDiskCache(key);
        }

        if(temp == null){
            Log.d(TAG,"Image not found in cache for key : "+key);
        }
        return temp;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new CloneNotSupportedException();

    }

    private void init(Context context){
        mDiskCacheStarting = true;
        final int cacheSize = DEFAULT_MEM_CACHE_SIZE;
        mLRUCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        mCacheDir = getDiskCacheDir(context, CACHE_DIR);
        new InitDiskCacheTask().execute();


        mSharedPref = context.getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);

    }

    private void addBitmapToLRUCache(String key, Bitmap bitmap){
        if (mLRUCache.get(key) == null) {
            mLRUCache.put(key, bitmap);
        }
    }

    private void addBitmapToDiskCache(String key, Bitmap bitmap){
        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String hashed_key = hashKeyForDisk(key);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(hashed_key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(hashed_key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            bitmap.compress(
                                    DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } catch (Exception e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {}
                }
            }
        }
    }

    private Bitmap getBitmapFromLRUCache(String key) {
        return mLRUCache.get(key);
    }

    private Bitmap getBitmapFromDiskCache(String key) {
        Log.d(TAG,"getBitmapFromDiskCache::key = "+key);
        final String hashed_key = hashKeyForDisk(key);
        Log.d(TAG,"getBitmapFromDiskCache::hashed_key = "+hashed_key);
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(hashed_key);
                    if (snapshot != null) {
                        Log.d(TAG,"getBitmapFromDiskCache::snapshot!=null");
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            Log.d(TAG,"getBitmapFromDiskCache::inputStream!=null");
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();
                            bitmap = decodeBitmapFromDescriptor(fd);
                        }
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "getBitmapFromDiskCache - " + e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {}
                }
            }
            return bitmap;
        }
    }

    /****************Utility APIs************/
    private File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    private boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    private File getExternalCacheDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return context.getExternalCacheDir();
        }
        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    private long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    private String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private Bitmap decodeBitmapFromDescriptor(FileDescriptor fileDescriptor) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inMutable = true;

        //options.inBitmap = tempBitmap; //Hmmmm.Lets not make this complex for this demo
        //options.inSampleSize

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    class InitDiskCacheTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // Set up disk cache
            synchronized (mDiskCacheLock) {
                if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                    File diskCacheDir = mCacheDir;
                    if (diskCacheDir != null) {
                        if (!diskCacheDir.exists()) {
                            diskCacheDir.mkdirs();
                        }
                        if (getUsableSpace(diskCacheDir) > ApplicationCache.DEFAULT_DISK_CACHE_SIZE) {
                            try {
                                mDiskLruCache = DiskLruCache.open(
                                        diskCacheDir, 1, 1,DEFAULT_DISK_CACHE_SIZE);
                            } catch (final IOException e) {
                                Log.e(TAG, "initDiskCache - " + e);
                            }
                        }
                    }
                }
                mDiskCacheStarting = false;
                mDiskCacheLock.notifyAll();
            }
            return null;
        }
    }

}
