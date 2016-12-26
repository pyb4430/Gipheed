package com.example.taylor.gipheed.Activities;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.taylor.gipheed.Fragments.GifPlayFragment;
import com.example.taylor.gipheed.Fragments.SearchFragment;
import com.example.taylor.gipheed.Fragments.TrendingFragment;
import com.example.taylor.gipheed.Giphy.GiphyController;
import com.example.taylor.gipheed.Giphy.GiphyTrendRespModel;
import com.example.taylor.gipheed.ThreadManager;
import com.example.taylor.gipheed.GifFeedRecyclerAdapter;
import com.example.taylor.gipheed.Utils;

public class MainActivity extends AbstractActivity {

    private static final String TAG = "MainActivity";

    private Utils.Sizer sizer;

    private FragmentManager fragmentManager;

    private LinearLayout llTabs;
    private TextView trendingTab;
    private TextView searchTab;
    private TextView gifPlayTab;

    private FrameLayout flMain;
    private TrendingFragment trendingFragment;
    private SearchFragment searchFragment;
    private GifPlayFragment gifPlayFragment;

    private RecyclerView recyclerView;
    private GifFeedRecyclerAdapter recyclerAdapter;

    private GiphyTrendRespModel giphyTrendRespModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sizer = Utils.getSizer(this);

        LinearLayout llMain = new LinearLayout(this);
        llMain.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        llMain.setOrientation(LinearLayout.VERTICAL);
        llMain.setGravity(Gravity.CENTER);
        setContentView(llMain);

        llTabs = new LinearLayout(this);
        llTabs.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, sizer.viewSize(15)));
        llTabs.setOrientation(LinearLayout.HORIZONTAL);
        llMain.addView(llTabs);

        trendingTab = new TextView(this);
        trendingTab.setLayoutParams(new LinearLayout.LayoutParams(sizer.viewSize(33.3f), sizer.viewSize(15)));
        trendingTab.setText("Trending");
        trendingTab.setTextColor(Color.WHITE);
        trendingTab.setGravity(Gravity.CENTER);
        trendingTab.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizer.viewSize(5));
        llTabs.addView(trendingTab);

        searchTab = new TextView(this);
        searchTab.setLayoutParams(new LinearLayout.LayoutParams(sizer.viewSize(33.3f), sizer.viewSize(15)));
        searchTab.setText("Search");
        searchTab.setTextColor(Color.WHITE);
        searchTab.setGravity(Gravity.CENTER);
        searchTab.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizer.viewSize(5));
        llTabs.addView(searchTab);

        gifPlayTab = new TextView(this);
        gifPlayTab.setLayoutParams(new LinearLayout.LayoutParams(sizer.viewSize(33.3f), sizer.viewSize(15)));
        gifPlayTab.setText("Play Gif");
        gifPlayTab.setTextColor(Color.WHITE);
        gifPlayTab.setGravity(Gravity.CENTER);
        gifPlayTab.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizer.viewSize(5));
        llTabs.addView(gifPlayTab);

        flMain = new FrameLayout(this);
        flMain.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        flMain.setId(10001);
        llMain.addView(flMain);

        trendingFragment = new TrendingFragment();
        searchFragment = new SearchFragment();
        gifPlayFragment = new GifPlayFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(flMain.getId(), trendingFragment);
        fragmentTransaction.commit();



//        recyclerView = new RecyclerView(this);
//        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        llMain.addView(recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerAdapter = new TrendingRecyclerAdapter(this);
//        recyclerView.setAdapter(recyclerAdapter);

//        LinearLayout llLoadGif = new LinearLayout(this);
//        llLoadGif.setLayoutParams(new LinearLayout.LayoutParams(300, 100, Gravity.BOTTOM|Gravity.RIGHT));
//        llLoadGif.setBackgroundColor(Color.GREEN);
//        llLoadGif.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, LoadGifActivity.class);
//                startActivity(intent);
//            }
//        });
//        llMain.addView(llLoadGif);

        searchTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(flMain.getId(), searchFragment);
                fragmentTransaction.commit();
                trendingTab.setBackgroundColor(Color.LTGRAY);
                gifPlayTab.setBackgroundColor(Color.LTGRAY);
                searchTab.setBackgroundColor(Color.parseColor("#78AB46"));
            }
        });

        trendingTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(flMain.getId(), trendingFragment);
                fragmentTransaction.commit();
                trendingTab.setBackgroundColor(Color.parseColor("#78AB46"));
                gifPlayTab.setBackgroundColor(Color.LTGRAY);
                searchTab.setBackgroundColor(Color.LTGRAY);
            }
        });

        gifPlayTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(flMain.getId(), gifPlayFragment);
                fragmentTransaction.commit();
                trendingTab.setBackgroundColor(Color.LTGRAY);
                gifPlayTab.setBackgroundColor(Color.parseColor("#78AB46"));
                searchTab.setBackgroundColor(Color.LTGRAY);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

//        loadData();
    }

    private void loadData() {
        ThreadManager.Run(new Runnable() {
            @Override
            public void run() {
                giphyTrendRespModel = GiphyController.getTrending(25);

                ThreadManager.RunUI(new Runnable() {
                    @Override
                    public void run() {
                        recyclerAdapter.setData(giphyTrendRespModel);
                    }
                });
            }
        });
    }

}
