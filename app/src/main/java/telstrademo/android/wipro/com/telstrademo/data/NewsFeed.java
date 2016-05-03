package telstrademo.android.wipro.com.telstrademo.data;

/**
 * Created by Nilanjan Biswas on 05/02/2016.
 */
public class NewsFeed {
    public String title;
    public String description;
    public String imageHref;

    public NewsFeed(String newsTitle,String newsDesc,String newsImageUrl){
        title = newsTitle;
        description = newsDesc;
        imageHref = newsImageUrl;
    }


}
