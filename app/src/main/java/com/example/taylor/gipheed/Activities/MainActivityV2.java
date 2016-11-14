package com.example.taylor.gipheed.Activities;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.taylor.gipheed.Fragments.FavoritesFragment;
import com.example.taylor.gipheed.Fragments.SearchFragment;
import com.example.taylor.gipheed.Fragments.TrendingFragment;
import com.example.taylor.gipheed.R;
import com.example.taylor.gipheed.ThreadManager;
import com.example.taylor.gipheed.Utils;

public class MainActivityV2 extends AppCompatActivity {

    private static final String TAG = "MainActivityV2";

    private Utils.Sizer sizer;

    private Menu menu;

    private FragmentManager fragmentManager;
    private TrendingFragment trendingFragment;
    private SearchFragment searchFragment;
    private FavoritesFragment favoritesFragment;

    private Toolbar toolbar;

    private ViewPager viewPager;

    private String searchQuery;
    private boolean newSearchRecieved = false;

    private boolean isViewModeStream = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sizer = Utils.getSizer(this);

        isViewModeStream = false;

        setContentView(R.layout.activity_main_v2);

        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        viewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
             @Override
             public Fragment getItem(int position) {
                 switch (position) {
                     case 0:
                         trendingFragment = new TrendingFragment();
                         trendingFragment.setIsViewModeStream(isViewModeStream);
                         trendingFragment.setFeedChangeListener(GIF_FEED_CHANGE_LISTENER);
                         return trendingFragment;
                     case 1:
                         searchFragment = new SearchFragment();
                         searchFragment.setIsViewModeStream(isViewModeStream);
                         searchFragment.setFeedChangeListener(GIF_FEED_CHANGE_LISTENER);
                         return searchFragment;
                     case 2:
                         favoritesFragment = new FavoritesFragment();
                         favoritesFragment.setIsViewModeStream(isViewModeStream);
                         return favoritesFragment;
                     default:
                         trendingFragment = new TrendingFragment();
                         trendingFragment.setIsViewModeStream(isViewModeStream);
                         trendingFragment.setFeedChangeListener(GIF_FEED_CHANGE_LISTENER);
                         return trendingFragment;
                 }
             }

             @Override
             public int getCount() {
                 return 3;
             }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "Trending";
                    case 1:
                        return "Search";
                    case 2:
                        return "Favorites";
                    default:
                        return "Trending";
                }
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                    case 2:
                        if(menu != null) {
                            menu.findItem(R.id.action_search).collapseActionView();
                        }
                        break;
                    case 1:
                        if(menu != null && (searchQuery == null || searchQuery.length() < 1)) {
                            menu.findItem(R.id.action_search).expandActionView();
                        }
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchQuery = intent.getStringExtra(SearchManager.QUERY);
            newSearchRecieved = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = getComponentName();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        //TODO: Set the query of the searchview to the last searched query
//        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                if(searchQuery != null && searchQuery.length() > 0) {
//                    searchView.setQuery(searchQuery, false);
//                }
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                return false;
//            }
//        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MenuItemCompat.collapseActionView(searchItem);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_toggle_view) {
            Intent intent = new Intent(this, ImageMaskActivity.class);
            startActivity(intent);
//            Log.v(TAG, "view mode toggled");
//            if(isViewModeStream) {
//                isViewModeStream = false;
//                item.setIcon(R.drawable.ic_view_stream_black_24dp);
//            } else {
//                isViewModeStream = true;
//                item.setIcon(R.drawable.ic_view_list_black_24dp);
//            }
//            if(trendingFragment != null) {
//                trendingFragment.setIsViewModeStream(isViewModeStream);
//            }
//            if(searchFragment != null) {
//                searchFragment.setIsViewModeStream(isViewModeStream);
//            }
//            if(favoritesFragment != null) {
//                favoritesFragment.setIsViewModeStream(isViewModeStream);
//            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(newSearchRecieved && searchQuery != null && searchQuery.length()>0) {
            newSearchRecieved = false;
            if(searchFragment != null) {
                searchFragment.loadData(searchQuery);
                viewPager.setCurrentItem(1);
            } else {
                viewPager.setCurrentItem(1);
                searchFragment.loadData(searchQuery);
            }
        }
    }

    private final GifFeedChangeListener GIF_FEED_CHANGE_LISTENER = new GifFeedChangeListener() {
        @Override
        public void onFeedClick() {
            if(menu != null) {
                MenuItem searchItem = menu.findItem(R.id.action_search);
                if(searchItem.isActionViewExpanded()) {
                    searchItem.collapseActionView();
                }
            }
        }

        @Override
        public void onFavoritesUpdated() {
            if(favoritesFragment != null) {
                ThreadManager.RunUI(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivityV2.this, "Favorites Updated", Toast.LENGTH_SHORT).show();
                    }
                });
                favoritesFragment.loadData();
            }
        }
    };

    // Used to dismiss search/keyboard, update favorites tab
    public interface GifFeedChangeListener {
        void onFeedClick();
        void onFavoritesUpdated();
    }

}
