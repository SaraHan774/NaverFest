package com.gahee.rss_v2;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.gahee.rss_v2.data.nasa.model.ChannelObj;
import com.gahee.rss_v2.data.nasa.tags.Item;
import com.gahee.rss_v2.data.wwf.model.WWFArticle;
import com.gahee.rss_v2.data.wwf.model.WWFChannel;
import com.gahee.rss_v2.remoteSource.RemoteViewModel;
import com.gahee.rss_v2.ui.pagerAdapters.outer.NasaPagerAdapter;
import com.gahee.rss_v2.ui.pagerAdapters.outer.WwfPagerAdapter;
import com.gahee.rss_v2.utils.ProgressBarUtil;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.gahee.rss_v2.utils.Constants.NASA_SLIDER_INDEX;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ViewPager viewPagerNasa;
    private ViewPager viewPagerWWF;
    private List<Item> nasaItemList;
    private ArrayList<WWFArticle> wwfItemList;
    private ProgressBarUtil progressBarUtil = new ProgressBarUtil();
    private ProgressBar [] progressBars;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findProgressBarsById();

        RemoteViewModel remoteViewModel = ViewModelProviders.of(this).get(RemoteViewModel.class);
        if(savedInstanceState == null){
            remoteViewModel.fetchNasaDataFromRepo();
//            fragmentTransactionHelper(NasaFragment.newInstance());
        }else{
            index = savedInstanceState.getInt(NASA_SLIDER_INDEX);
        }
//        remoteViewModel.fetchTimeDataFromRepo();
        remoteViewModel.fetchWWFDataFromRepo();



        remoteViewModel.getChannelMutableLiveData().observe(this, new Observer<ArrayList<ChannelObj>>() {
            @Override
            public void onChanged(ArrayList<ChannelObj> channelObjs) {
                nasaItemList = channelObjs.get(0).getmItemList();
                viewPagerNasa = findViewById(R.id.view_pager_nasa_outer);
                NasaPagerAdapter pagerAdapter = new NasaPagerAdapter(MainActivity.this,  channelObjs.get(0));
                viewPagerNasa.setAdapter(pagerAdapter);

            }
        });

        remoteViewModel.getWwfArticleLiveData().observe(this, new Observer<ArrayList<WWFArticle>>() {
            @Override
            public void onChanged(ArrayList<WWFArticle> wwfArticles) {
                wwfItemList = wwfArticles;
                viewPagerWWF = findViewById(R.id.view_pager_wwf_outer);
                WwfPagerAdapter pagerAdapter = new WwfPagerAdapter(MainActivity.this, wwfArticles);
                viewPagerWWF.setAdapter(pagerAdapter);
            }
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 4000, 6000);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NASA_SLIDER_INDEX, index);
    }

    //    public void transformPage(View view, float position) {
//        if(position <= -1.0F || position >= 1.0F) {
//            view.setTranslationX(view.getWidth() * position);
//            view.setAlpha(0.0F);
//        } else if( position == 0.0F ) {
//            view.setTranslationX(view.getWidth() * position);
//            view.setAlpha(1.0F);
//        } else {
//            // position is between -1.0F & 0.0F OR 0.0F & 1.0F
//            view.setTranslationX(view.getWidth() * -position);
//            view.setAlpha(1.0F - Math.abs(position));
//        }
//    }



    private void fragmentTransactionHelper(final Fragment fragment)   {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_placeholder_nasa, fragment);
        transaction.commit();
    }

    private class SliderTimer extends TimerTask{

        @Override
        public void run() {
            MainActivity.this.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            if(viewPagerNasa.getCurrentItem() < nasaItemList.size() - 1){
                                if (index < 6) {
                                    progressBarUtil.setSliderProgress(progressBars[index++]);
                                } else {
                                    index = 0;
                                    progressBarUtil.refreshAllProgressBars(progressBars);
                                }
                                Log.d(TAG, "progress bar index : " + index);
                                viewPagerNasa.setCurrentItem(viewPagerNasa.getCurrentItem() + 1);
                            }else{
                                viewPagerNasa.setCurrentItem(0);
                            }
                        }
                    }
            );
        }
    }

    private void findProgressBarsById(){
        progressBars = new ProgressBar[]{
            findViewById(R.id.progress_bar_1),
            findViewById(R.id.progress_bar_2),
            findViewById(R.id.progress_bar_3),
            findViewById(R.id.progress_bar_4),
            findViewById(R.id.progress_bar_5),
            findViewById(R.id.progress_bar_6),
        };
    }


}
