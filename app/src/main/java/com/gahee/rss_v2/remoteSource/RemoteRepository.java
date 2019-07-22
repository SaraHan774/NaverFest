package com.gahee.rss_v2.remoteSource;

import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;

import com.gahee.rss_v2.data.nasa.model.ArticleObj;
import com.gahee.rss_v2.data.nasa.model.ChannelObj;
import com.gahee.rss_v2.data.time.model.TimeArticle;
import com.gahee.rss_v2.data.time.model.TimeChannel;
import com.gahee.rss_v2.data.youtube.model.YoutubeChannel;
import com.gahee.rss_v2.data.youtube.model.YoutubeVideo;

import java.util.ArrayList;

public class RemoteRepository {

    private final RemoteDataUtils mRemoteDataUtils;
    public static RemoteRepository instance;

    public static RemoteRepository getInstance() {
        if(instance == null){
            instance = new RemoteRepository();
        }
        return instance;
    }

    public RemoteRepository(){
        mRemoteDataUtils = RemoteDataUtils.getInstance();
    }

    public void fetchData(){
        new FetchDataAsync(mRemoteDataUtils).execute();
    }

    public MutableLiveData<ArrayList<ChannelObj>> getChannelMutableLiveData(){
        return mRemoteDataUtils.getmChannelMutableLiveData();
    }

    public MutableLiveData<ArrayList<ArticleObj>> getArticleMutableLiveData(){
        return mRemoteDataUtils.getmArticleMutableLiveData();
    }

    //youtube
    public void fetchYtData(){new FetchYtDataAsync(mRemoteDataUtils).execute();}
    public MutableLiveData<ArrayList<YoutubeChannel>> getYoutubeChannelLiveData(){
        return mRemoteDataUtils.getmYoutubeChannelLiveData();
    }
    public MutableLiveData<ArrayList<YoutubeVideo>> getYoutubeVideoLiveData(){
        return mRemoteDataUtils.getmYoutubeVideoLiveData();
    }

    //Time news
    public void fetchTimeData(){new FetchTimeDataAsync(mRemoteDataUtils).execute();}
    public MutableLiveData<ArrayList<TimeChannel>> getTimeChannelLiveData(){
        return mRemoteDataUtils.getmTimeChannelLiveData();
    }

    public MutableLiveData<ArrayList<TimeArticle>> getTimeArticleLiveData(){
        return mRemoteDataUtils.getmTimeArticleLiveData();
    }

    //WWF articles
    public void fetchWWFData(){new FetchWWFDataAsync(mRemoteDataUtils).execute();}




    //fetching data async task
    private static class FetchDataAsync extends AsyncTask<Void, Void, Void>{
        RemoteDataUtils mRemoteDataUtils;

        public FetchDataAsync(RemoteDataUtils remoteDataUtils){
            this.mRemoteDataUtils = remoteDataUtils;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mRemoteDataUtils.fetchRemoteData();
            return null;
        }
    }

    private static class FetchYtDataAsync extends AsyncTask<Void, Void, Void>{
        RemoteDataUtils mRemoteDataUtils;

        public FetchYtDataAsync(RemoteDataUtils remoteDataUtils){
            this.mRemoteDataUtils = remoteDataUtils;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            mRemoteDataUtils.fetchRemoteYoutubeData();
            return null;
        }
    }

    private static class FetchTimeDataAsync extends AsyncTask<Void, Void, Void>{
        RemoteDataUtils mRemoteDataUtils;

        public FetchTimeDataAsync(RemoteDataUtils remoteDataUtils){
            this.mRemoteDataUtils = remoteDataUtils;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mRemoteDataUtils.fetchRemoteTimeData();
            return null;
        }
    }

    private static class FetchWWFDataAsync extends AsyncTask<Void, Void, Void>{
        RemoteDataUtils mRemoteDataUtils;

        public FetchWWFDataAsync(RemoteDataUtils remoteDataUtils){
            this.mRemoteDataUtils = remoteDataUtils;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mRemoteDataUtils.fetchRemoteWWFData();
            return null;
        }
    }

}