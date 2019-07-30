package com.gahee.rss_v2.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gahee.rss_v2.R;
import com.gahee.rss_v2.data.reuters.model.ChannelObj;
import com.gahee.rss_v2.data.reuters.tags.Item;
import com.gahee.rss_v2.data.wwf.model.WWFArticle;
import com.gahee.rss_v2.data.wwf.model.WWFChannel;
import com.gahee.rss_v2.remoteSource.RemoteViewModel;
import com.gahee.rss_v2.ui.pagerAdapters.ReutersPagerAdapter;
import com.gahee.rss_v2.ui.pagerAdapters.WwfPagerAdapter;
import com.gahee.rss_v2.utils.MyTimers;
import com.gahee.rss_v2.utils.ProgressBarUtil;
import com.gahee.rss_v2.utils.SliderIndexViewModel;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import static com.gahee.rss_v2.utils.Constants.CURRENT_WINDOW;
import static com.gahee.rss_v2.utils.Constants.MEDIA_URL;
import static com.gahee.rss_v2.utils.Constants.REUTERS_SLIDER_INDEX;
import static com.gahee.rss_v2.utils.Constants.PLAYBACK_POSITION;
import static com.gahee.rss_v2.utils.Constants.PLAY_WHEN_READY;
import static com.gahee.rss_v2.utils.Constants.REUTERS_SLIDER_TIME_INTERVAL;
import static com.gahee.rss_v2.utils.Constants.TAG_REUTERS_FRAME;
import static com.gahee.rss_v2.utils.Constants.TAG_WWF_FRAME;
import static com.gahee.rss_v2.utils.Constants.USER_AGENT;
import static com.gahee.rss_v2.utils.Constants.WWF_SLIDER_TIME_INTERVAL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ViewPager viewPagerReuters;
    private ViewPager viewPagerWWF;

    private List<Item> reutersItemList;
//    private ArrayList<WWFArticle> wwfItemList;
    private ReutersPagerAdapter pagerAdapter;
    private ProgressBar [] reutersProgressBars;
    private ProgressBar[] wwfProgressBars;

    private int reutersSliderIndex = 0;

    //simpleExoPlayer
    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView playerView;
    private String mediaURL = "";
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private FrameLayout frameLayout;

    private SliderIndexViewModel sliderIndexViewModel;

    private ProgressBarUtil reutersProgress;
    private ProgressBarUtil wwfProgress;
    private RemoteViewModel remoteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.d(TAG, "onCreate: ");

        findProgressBarsById();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        remoteViewModel = ViewModelProviders.of(this).get(RemoteViewModel.class);

        if(savedInstanceState != null){
            reutersSliderIndex = savedInstanceState.getInt(REUTERS_SLIDER_INDEX);
            mediaURL = savedInstanceState.getString(MEDIA_URL);
            playWhenReady = savedInstanceState.getBoolean(PLAY_WHEN_READY, false);
            playbackPosition = savedInstanceState.getLong(PLAYBACK_POSITION, 0);
            currentWindow = savedInstanceState.getInt(CURRENT_WINDOW, 0);
        }

        remoteViewModel.fetchReutersDataFromRepo();
        remoteViewModel.fetchTimeDataFromRepo();
        remoteViewModel.fetchWWFDataFromRepo();

        Log.d(TAG, "onCreate: fetching data");


        remoteViewModel.getChannelMutableLiveData().observe(this, reutersChannel -> {
            Log.d(TAG, "onChanged: " + "reuters");
            reutersItemList = reutersChannel.get(0).getmItemList();
            viewPagerReuters = findViewById(R.id.view_pager_reuters_outer);

            pagerAdapter = new ReutersPagerAdapter(MainActivity.this,  reutersChannel.get(0));
            viewPagerReuters.setAdapter(pagerAdapter);
            viewPagerReuters.addOnPageChangeListener(reutersViewPagerListener);

            frameLayout = viewPagerReuters.findViewWithTag(TAG_REUTERS_FRAME + 0);
            playerView = frameLayout.findViewById(R.id.reuters_outer_video_player);

            reutersProgress = new ProgressBarUtil();
            reutersProgress.setProgressBars(reutersProgressBars);
            reutersProgress.resetProgressBarToUserSelection( 0);

            TextView textView = findViewById(R.id.reuters_channel_title);
            textView.setText(reutersChannel.get(0).getmChannelTitle());

            setMediaURL(reutersChannel.get(0).getmItemList().get(0).getGroup().getContent().getUrlVideo());

            initializePlayer();
            hideSystemUi();
            setUpReutersSliderTimer(reutersChannel);
        });

        remoteViewModel.getWwfArticleLiveData().observe(this, wwfArticles -> {
//            this.wwfItemList = wwfArticles;
            viewPagerWWF = findViewById(R.id.view_pager_wwf_outer);
            WwfPagerAdapter pagerAdapter = new WwfPagerAdapter(MainActivity.this, wwfArticles);
            viewPagerWWF.setAdapter(pagerAdapter);
            Log.d(TAG, "setting wwf pager adapter " );
            viewPagerWWF.addOnPageChangeListener(wwfViewPagerListener);

            wwfProgress = new ProgressBarUtil();
            wwfProgress.setProgressBars(wwfProgressBars);
            wwfProgress.resetProgressBarToUserSelection(0);

            setUpWWFSliderTimer(wwfArticles);

        });

        remoteViewModel.getWwfChannelLiveData().observe(this, wwfChannels -> {
            TextView textView = findViewById(R.id.wwf_channel_title);
            textView.setText(wwfChannels.get(0).getTitle());
        });

        sliderIndexViewModel = ViewModelProviders.of(this).get(SliderIndexViewModel.class);
    }

    //list to store search results
    private final ArrayList searchResultList = new ArrayList();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.news_search_icon).getActionView();

        searchView.setFocusable(false);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //make an array list to store search results
                if(searchResultList != null){
                    Log.d(TAG, "search list cleared ");
                    searchResultList.clear();
                }
                if(searchResultList == null){

                }else{

                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });


        return true;
    }

    /*
    *  ViewPager Listeners
    */
    private final ViewPager.OnPageChangeListener reutersViewPagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            releasePlayer();
            setMediaURL(reutersItemList.get(position).getGroup().getContent().getUrlVideo());
            playbackPosition = 0;

            FrameLayout frameLayout = viewPagerReuters.findViewWithTag(TAG_REUTERS_FRAME + position);
            PlayerView playerView = frameLayout.findViewById(R.id.reuters_outer_video_player);
            //play videos
            MainActivity.this.playerView = playerView;
            initializePlayer();

            reutersProgress.resetProgressBarToUserSelection(position);

            if(simpleExoPlayer != null){
                Animation fadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.description_fade_out);
                TextView description = frameLayout.findViewById(R.id.tv_reuters_outer_description);

                TextView title = frameLayout.findViewById(R.id.tv_reuters_outer_title);
                Animation slideToRight = AnimationUtils.loadAnimation(MainActivity.this, R.anim.title_slide_to_left);

                if(description.getVisibility() == View.GONE || title.getVisibility() == View.GONE){
                    description.setVisibility(View.VISIBLE);
                    title.setVisibility(View.VISIBLE);
                }

                description.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                            description.setVisibility(View.GONE);
                            title.startAnimation(slideToRight);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                slideToRight.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        title.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            Log.d(TAG, "player view init ... " + MainActivity.this.playerView);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private final ViewPager.OnPageChangeListener wwfViewPagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.description_fade_in);
            FrameLayout frameLayout = viewPagerWWF.findViewWithTag(TAG_WWF_FRAME + position);
            TextView articleDescription = frameLayout.findViewById(R.id.tv_wwf_outer_description);
            articleDescription.startAnimation(fadeIn);

            sliderIndexViewModel.setWwfSliderIndex(position);
            sliderIndexViewModel.getWwfSliderIndex().observe(MainActivity.this, integer -> wwfProgress.resetProgressBarToUserSelection(integer));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };




    /*
     *  ProgressBars reference
     */
    private void findProgressBarsById(){
        reutersProgressBars = new ProgressBar[]{
                findViewById(R.id.progress_bar_1),
                findViewById(R.id.progress_bar_2),
                findViewById(R.id.progress_bar_3),
                findViewById(R.id.progress_bar_4),
                findViewById(R.id.progress_bar_5),
                findViewById(R.id.progress_bar_6),
        };

        wwfProgressBars = new ProgressBar [] {
                findViewById(R.id.progress_bar_100),
                findViewById(R.id.progress_bar_200),
                findViewById(R.id.progress_bar_300),
                findViewById(R.id.progress_bar_400),
                findViewById(R.id.progress_bar_500),
                findViewById(R.id.progress_bar_600)

        };

    }


    /*
     * Timers for each ViewPager
     */
    private void setUpReutersSliderTimer(ArrayList<ChannelObj> channelObjs){
        MyTimers myTimersReuters = new MyTimers(channelObjs.get(0).getmItemList().size());
        Timer timerReuters = new Timer();

        MyTimers.SliderTimer reutersSliderTimer
                = myTimersReuters.getSliderTimer(MainActivity.this, viewPagerReuters, reutersProgress);
        timerReuters.scheduleAtFixedRate(reutersSliderTimer, REUTERS_SLIDER_TIME_INTERVAL, REUTERS_SLIDER_TIME_INTERVAL);

    }


    private void setUpWWFSliderTimer(ArrayList<WWFArticle> wwfArticles){
        MyTimers myTimers = new MyTimers(wwfArticles.size());
        Timer wwfTimer = new Timer();
        MyTimers.SliderTimer sliderTimer = myTimers.getSliderTimer(MainActivity.this, viewPagerWWF, wwfProgress);
        wwfTimer.scheduleAtFixedRate(sliderTimer, WWF_SLIDER_TIME_INTERVAL, WWF_SLIDER_TIME_INTERVAL);
    }



    /*
     *  SimpleExoPlayer set up
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        if(Util.SDK_INT > 23){
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if(Util.SDK_INT <= 23 || simpleExoPlayer == null) {
            initializePlayer();
        }
    }

    private void setMediaURL(String mediaURL){

        this.mediaURL = mediaURL;

        Log.d(TAG, "media URL : " + this.mediaURL);
    }

    private boolean initializePlayer(){
        if(simpleExoPlayer != null){
            releasePlayer();
        }
        if(mediaURL.equals("")){
            Log.d(TAG, "no media url");
            Toast.makeText(this, getString(R.string.no_video_warning), Toast.LENGTH_SHORT).show();
            return false;
        }else {
            Log.d(TAG, "initializing exo player");
            playerView.setVisibility(View.VISIBLE);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
                    this,
                    new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl()
            );
            playerView.setPlayer(simpleExoPlayer);
            simpleExoPlayer.setPlayWhenReady(playWhenReady);
            simpleExoPlayer.seekTo(currentWindow, playbackPosition);

            Uri uri = Uri.parse(mediaURL);
            MediaSource mediaSource = buildMediaSource(uri);
            simpleExoPlayer.prepare(mediaSource, false, false);
            return true;
        }
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(REUTERS_SLIDER_INDEX, reutersSliderIndex);
        outState.putString(MEDIA_URL, mediaURL);
        outState.putBoolean(PLAY_WHEN_READY, playWhenReady);
        outState.putLong(PLAYBACK_POSITION, playbackPosition);
        outState.putInt(CURRENT_WINDOW, currentWindow);

    }



    private MediaSource buildMediaSource(Uri uri){
        DefaultDataSourceFactory defaultDataSourceFactory = createDataSourceFactory(this, USER_AGENT, null);
        return new ProgressiveMediaSource.Factory(
                defaultDataSourceFactory)
                .createMediaSource(uri);
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        retrieveCurrentPlayerState(true);
        if(Util.SDK_INT <= 23){
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        if(Util.SDK_INT > 23 || simpleExoPlayer != null){
            releasePlayer();
        }
    }

    private void releasePlayer(){
        Log.d(TAG, "releasePlayer()");
        if(simpleExoPlayer != null){
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
    }

    private void retrieveCurrentPlayerState(boolean savePlaybackPosition){
        if(simpleExoPlayer != null && savePlaybackPosition){
            playbackPosition = simpleExoPlayer.getCurrentPosition();
        }else{
            playbackPosition = 0;
        }
        currentWindow = simpleExoPlayer != null ? simpleExoPlayer.getCurrentWindowIndex() : 0;
        playWhenReady = simpleExoPlayer != null ? simpleExoPlayer.getPlayWhenReady() : false;
    }

    private static DefaultDataSourceFactory createDataSourceFactory(Context context, String userAgent,
                                                                    TransferListener listener) {
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(
                USER_AGENT,
                null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true
        );

        return new DefaultDataSourceFactory(
                context,
                listener,
                httpDataSourceFactory
        );
    }





}
