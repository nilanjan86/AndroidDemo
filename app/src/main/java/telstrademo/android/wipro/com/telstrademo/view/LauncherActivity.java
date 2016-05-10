package telstrademo.android.wipro.com.telstrademo.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import telstrademo.android.wipro.com.telstrademo.R;
import telstrademo.android.wipro.com.telstrademo.adapter.NewsAdapter;
import telstrademo.android.wipro.com.telstrademo.controller.TaskMediator;
import telstrademo.android.wipro.com.telstrademo.data.NewsFeed;
import telstrademo.android.wipro.com.telstrademo.interfaces.INetworkFeedCallback;
import telstrademo.android.wipro.com.telstrademo.util.Constants;
import telstrademo.android.wipro.com.telstrademo.util.LogManager;
import telstrademo.android.wipro.com.telstrademo.util.Utils;

public class LauncherActivity extends AppCompatActivity implements INetworkFeedCallback {

    private static final String TAG = "TELSTRA_LauncherAct";

    private RecyclerView mRecyclerView;
    private NewsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Please wait...Trying to fetching latest feed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if(!Utils.isNetworkConnectionAvailable(LauncherActivity.this)){
                    Snackbar.make(mRecyclerView, "Ooops...no network connection", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else{
                    Snackbar.make(mRecyclerView, "Please wait...Trying to fetching latest feed", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Bundle actionData = new Bundle();
                    actionData.putString(Constants.KEY_NEWS_URL,Constants.NEWS_FEED_URL);
                    TaskMediator.getInstance(LauncherActivity.this).doAction(Constants.ACTION_FETCH_NEWS,actionData,LauncherActivity.this);
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NewsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        if(!Utils.isNetworkConnectionAvailable(this)){
            Snackbar.make(mRecyclerView, R.string.network_error_message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }else{
            Snackbar.make(mRecyclerView, R.string.loading_message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            Bundle actionData = new Bundle();
            actionData.putString(Constants.KEY_NEWS_URL,Constants.NEWS_FEED_URL);
            TaskMediator.getInstance(this).doAction(Constants.ACTION_FETCH_NEWS,actionData,this);
        }
    }


    @Override
    public void onDataReceived(String jsonData) {
        if(jsonData == null){
            LogManager.d(TAG,"json data is NULL");
            return;
        }
        LogManager.d(TAG,"json data = "+jsonData);

        try{
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(jsonData);
            if(element!= null && element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if(obj != null){
                    setTitle(obj.get(Constants.KEY_FEED_TITLE).getAsString());
                    JsonArray arr = obj.getAsJsonArray(Constants.KEY_JSON_ROW);
                    if(arr != null && arr.size()>0){
                        ArrayList<NewsFeed> data = new Gson().fromJson(arr.toString(), new TypeToken<ArrayList<NewsFeed>>(){}.getType());
                        if(data!= null && data.size()>0) {
                            for(int i =0;i<data.size();i++){
                                if(data.get(i).title==null || data.get(i).title.length()==0)
                                    data.remove(i);
                            }
                            //Start downloading images in background
                            for(NewsFeed nf : data){
                                Bundle actionData = new Bundle();
                                actionData.putString(Constants.KEY_IMAGE_URL,nf.imageHref);
                                TaskMediator.getInstance(LauncherActivity.this).doAction(Constants.ACTION_FETCH_IMAGE, actionData,null);
                                LogManager.d(TAG,"Image Download Start for url = "+nf.imageHref);
                            }
                            mAdapter.setDataSet(data);
                            mAdapter.notifyDataSetChanged();
                        }else{
                            Snackbar.make(mRecyclerView, R.string.generic_error_message, Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }else{
                        Snackbar.make(mRecyclerView, R.string.generic_error_message, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }else{
                    Snackbar.make(mRecyclerView, R.string.generic_error_message, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }else{
                Snackbar.make(mRecyclerView, R.string.generic_error_message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }catch(Exception e){
            e.printStackTrace();
            Snackbar.make(mRecyclerView, R.string.generic_error_message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    @Override
    public void onImageDownloaded(String url, Bitmap image) {}

    @Override
    public void onDataDownloadError(int errorCode) {
        Snackbar.make(mRecyclerView, R.string.download_error_message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onImageDownloadError(String url, int errorCode) {}
}
