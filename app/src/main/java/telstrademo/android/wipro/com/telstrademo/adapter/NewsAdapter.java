package telstrademo.android.wipro.com.telstrademo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import telstrademo.android.wipro.com.telstrademo.R;
import telstrademo.android.wipro.com.telstrademo.controller.TaskMediator;
import telstrademo.android.wipro.com.telstrademo.data.NewsFeed;
import telstrademo.android.wipro.com.telstrademo.interfaces.INetworkFeedCallback;
import telstrademo.android.wipro.com.telstrademo.util.ApplicationCache;

/**
 * Created by Nilanjan Biswas on 5/2/2016.
 */
public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "TELSTRA_NewsAdapter";
    private ArrayList<NewsFeed> mDataSet = new ArrayList<NewsFeed>();
    private Context mContext;
    public NewsAdapter(Context context) {mContext = context;}

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        protected TextView mTitle;
        protected TextView mDesc;
        protected ImageView mImage;

        public NewsViewHolder(View v) {
            super(v);
            mTitle =  (TextView) v.findViewById(R.id.tv_title);
            mDesc = (TextView)  v.findViewById(R.id.tv_desc);
            mImage = (ImageView)  v.findViewById(R.id.iv_thumbnail);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_layout, parent, false);

        return new NewsViewHolder(itemView);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.d(TAG,"onViewAttachedToWindow");
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Log.d(TAG,"onViewDetachedFromWindow");
        clearHolder(holder);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final NewsFeed nf = (NewsFeed)mDataSet.get(position);
        ((NewsViewHolder)holder).mTitle.setText(nf.title);
        Log.d(TAG,"onBindViewHolder::title = "+ nf.title);
        ((NewsViewHolder) holder).mDesc.setText(nf.description);
        if(null != nf.imageHref && nf.imageHref.length()>0){
            Log.d(TAG,"onBindViewHolder::url = "+ nf.imageHref);
            Bitmap bitmap = ApplicationCache.getInstance(mContext).getBitmap(nf.imageHref);
            if(null != bitmap){
                ((NewsViewHolder)holder).mImage.setImageBitmap(bitmap);
            }else{
                /*Bundle actionData = new Bundle();
                  actionData.putString(Constants.KEY_IMAGE_URL,nf.imageHref);
                  TaskMediator.getInstance(mContext).doAction(Constants.ACTION_FETCH_IMAGE, actionData,null); */
                TaskMediator.getInstance(mContext).registerImageListener(nf.imageHref,new INetworkFeedCallback() {
                    @Override
                    public void onDataReceived(String jsonData)  { /* no action*/}

                    @Override
                    public void onImageDownloaded(String url, Bitmap image) {
                        Log.d(TAG,"##############################################");
                        Log.d(TAG,"onImageDownloaded::url = "+url+" image = "+image + " nf = "+nf.title + " holder = "+((NewsViewHolder) holder).mTitle.getText());
                        if(nf.imageHref!= null && nf.imageHref.length()>0 && nf.imageHref.equals(url)){
                            ((NewsViewHolder)holder).mImage.setImageBitmap(image);
                        }else{
                            Log.d(TAG,"Ooops..view not visible.Ignore downloaded image");
                        }
                    }

                    @Override
                    public void onDataDownloadError(int errorCode) { /* no action*/}

                    @Override
                    public void onImageDownloadError(String url, int errorCode)  { /* no action*/}
                });
            }
        }else{
            ((NewsViewHolder)holder).mImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.def_thumnail));
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(TAG,"onAttachedToRecyclerView");
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        Log.d(TAG,"onDetachedFromRecyclerView");
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setDataSet(ArrayList<NewsFeed> ds){
        mDataSet = ds;
    }

    private void clearHolder(RecyclerView.ViewHolder holder){
        ((NewsViewHolder)holder).mImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.def_thumnail));
        ((NewsViewHolder)holder).mTitle.setText("");
        ((NewsViewHolder) holder).mDesc.setText("");
    }
}