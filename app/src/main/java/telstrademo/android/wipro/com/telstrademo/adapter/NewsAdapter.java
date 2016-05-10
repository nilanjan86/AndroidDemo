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

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import telstrademo.android.wipro.com.telstrademo.R;
import telstrademo.android.wipro.com.telstrademo.controller.TaskMediator;
import telstrademo.android.wipro.com.telstrademo.data.NewsFeed;
import telstrademo.android.wipro.com.telstrademo.interfaces.INetworkFeedCallback;
import telstrademo.android.wipro.com.telstrademo.util.ApplicationCache;
import telstrademo.android.wipro.com.telstrademo.util.LogManager;

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
        protected String mImageHref;

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
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        clearHolder(holder);
        if(((NewsViewHolder)holder).mImageHref != null){
            TaskMediator.getInstance(mContext).unregisterImageListener(((NewsViewHolder)holder).mImageHref);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final NewsFeed nf = (NewsFeed)mDataSet.get(position);
        if(nf == null){
            return;
        }
        ((NewsViewHolder)holder).mTitle.setText(nf.title);
        ((NewsViewHolder) holder).mDesc.setText(nf.description);
        if(null != nf.imageHref && nf.imageHref.length()>0){
            ((NewsViewHolder)holder).mImageHref = nf.imageHref;
            Bitmap bitmap = ApplicationCache.getInstance(mContext).getBitmap(nf.imageHref);
            if(bitmap != null){
                WeakReference<Bitmap> wrb = new WeakReference<Bitmap>(bitmap);
                ((NewsViewHolder)holder).mImage.setImageBitmap(wrb.get());
            }else {
                // Piasso takes too much time,especially for redirect urls
                    /* Picasso.with(mContext)
                        .load(nf.imageHref)
                        .placeholder(R.drawable.def_thumnail)
                        .into(((NewsViewHolder)holder).mImage);*/

                TaskMediator.getInstance(mContext).registerImageListener(nf.imageHref, new INetworkFeedCallback() {
                    @Override
                    public void onDataReceived(String jsonData) { /* no action*/ }

                    @Override
                    public void onImageDownloaded(String url, Bitmap image) {
                        LogManager.d(TAG, "onImageDownloaded::url = " + url + " image = " + image + " nf = " + nf.title + " holder = " + ((NewsViewHolder) holder).mTitle.getText());
                        if (nf.imageHref != null && nf.imageHref.length() > 0 && nf.imageHref.equals(url)) {
                            ((NewsViewHolder) holder).mImage.setImageBitmap(image);
                        } else {
                            LogManager.d(TAG, "Ooops..view not visible.Ignore downloaded image");
                        }
                    }

                    @Override
                    public void onDataDownloadError(int errorCode) { /* no action*/ }

                    @Override
                    public void onImageDownloadError(String url, int errorCode) { /* no action*/ }
                });
            }
        }else{
            ((NewsViewHolder)holder).mImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.def_thumnail));
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        if(mDataSet != null)
            return mDataSet.size();
        else
            return 0;
    }

    public void setDataSet(ArrayList<NewsFeed> ds){
        mDataSet = ds;
    }

    private void clearHolder(RecyclerView.ViewHolder holder){
        ((NewsViewHolder)holder).mImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.def_thumnail));
    }
}
