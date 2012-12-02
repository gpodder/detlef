
package at.ac.tuwien.detlef.activities;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.callbacks.CallbackContainer;
import at.ac.tuwien.detlef.db.PodcastDBAssistant;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.fragments.EpisodeListFragment;
import at.ac.tuwien.detlef.fragments.PlayerFragment;
import at.ac.tuwien.detlef.fragments.PodListFragment;
import at.ac.tuwien.detlef.fragments.SearchFragment;
import at.ac.tuwien.detlef.gpodder.FeedSyncResultHandler;
import at.ac.tuwien.detlef.gpodder.GPodderException;
import at.ac.tuwien.detlef.gpodder.PodcastSyncResultHandler;
import at.ac.tuwien.detlef.gpodder.PullFeedAsyncTask;
import at.ac.tuwien.detlef.gpodder.PullSubscriptionsAsyncTask;

public class MainActivity extends FragmentActivity
        implements ActionBar.TabListener, PodListFragment.OnPodcastSelectedListener,
        EpisodeListFragment.OnEpisodeSelectedListener {

    private Menu menu;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ActionBar actionBar;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        /* old Activity is recreated */
        if (savedInstanceState != null) {
            curPodSync = new AtomicInteger(savedInstanceState.getInt(KEY_CUR_POD_SYNC, 0));
            numPodSync = new AtomicInteger(savedInstanceState.getInt(KEY_NUM_POD_SYNC, -1));
        } else {
            cbCont.put(KEY_PODCAST_HANDLER, new PodcastHandler());
            cbCont.put(KEY_FEED_HANDLER, new FeedHandler());
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab.
        // We can also use ActionBar.Tab#select() to do this if we have a
        // reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter.
            // Also specify this Activity object, which implements the
            // TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

        /* Ready the progress dialog */
        progressDialog = new ProgressDialog(this);
        prepareProgressDialog();
        if (numPodSync.get() != -1) {
            progressDialog.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        /* Register the Podcast- & FeedHandler. */
        cbCont.registerReceiver(this);
    }

    @Override
    public void onBackPressed() {
        switch (mViewPager.getCurrentItem()) {
            case SectionsPagerAdapter.POSITION_PODCASTS:
                super.onBackPressed();
                break;
            case SectionsPagerAdapter.POSITION_EPISODES:
                actionBar.selectTab(actionBar.getTabAt(SectionsPagerAdapter.POSITION_PODCASTS));
                break;
            case SectionsPagerAdapter.POSITION_PLAYER:
                actionBar.selectTab(actionBar.getTabAt(SectionsPagerAdapter.POSITION_EPISODES));
                break;
            case SectionsPagerAdapter.POSITION_SEARCH:
                actionBar.selectTab(actionBar.getTabAt(SectionsPagerAdapter.POSITION_PLAYER));
                break;
            default:
                super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        /* Unregister the Podcast- & FeedHandler. */
        cbCont.unregisterReceiver();

        if (isFinishing()) {
            cbCont.clear();
        }

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(KEY_NUM_POD_SYNC, numPodSync.get());
        savedInstanceState.putInt(KEY_CUR_POD_SYNC, curPodSync.get());
    }

    @Override
    public void onDestroy() {
        progressDialog.dismiss();

        super.onDestroy();
    }

    private void prepareProgressDialog() {
        progressDialog.setTitle(R.string.refreshing);
        progressDialog.setCancelable(false);
        if (numPodSync.get() > 0) {
            progressDialog.setMessage(String.format(
                    getString(R.string.refreshing_feed_x_of_y),
                    curPodSync.get() + 1, numPodSync.get()));
        } else {
            progressDialog.setMessage(getString(R.string.refreshing_feed_list));
        }
    }

    /**
     * All callbacks this Activity receives are stored here. This allows us to
     * manage the Activity Lifecycle more easily.
     */
    private static final CallbackContainer<MainActivity> cbCont =
            new CallbackContainer<MainActivity>();

    /**
     * The Tasks for the refresh are run on a single thread.
     */
    private static final ExecutorService refreshBg = Executors.newSingleThreadExecutor();

    /**
     * The Toast with the Output of the refresh operation is shown this long.
     */
    private static final int REFRESH_MSG_DURATION_MS = 5000;

    /**
     * The progress dialog displayed during a refresh.
     */
    private ProgressDialog progressDialog;

    private static final String KEY_PODCAST_HANDLER = "KEY_PODCAST_HANDLER";
    private static final String KEY_FEED_HANDLER = "KEY_FEED_HANDLER";
    private static final String KEY_NUM_POD_SYNC = "KEY_NUM_POD_SYNC";

    private static final String KEY_CUR_POD_SYNC = "KEY_CUR_POD_SYNC";

    /** Number of feeds to sync, -1 if no refresh is in progress. */
    private AtomicInteger numPodSync = new AtomicInteger(-1);

    /** Number of feeds already synchronized. */
    private AtomicInteger curPodSync = new AtomicInteger(0);

    /**
     * The Handler for receiving PullSubscriptionsAsyncTask's results.
     */
    private static final class PodcastHandler extends PodcastSyncResultHandler<MainActivity> {

        /**
         * Once the Podcast list is synchronized, update all feeds.
         */
        @Override
        public void handle() {
            PodcastDBAssistant pda = DependencyAssistant.getDependencyAssistant()
                    .getPodcastDBAssistant();

            synchronized (getRcv().numPodSync) {
                for (Podcast p : pda.getAllPodcasts(getRcv())) {
                    refreshBg.execute(new PullFeedAsyncTask((
                            FeedSyncResultHandler<? extends Activity>)
                            cbCont.get(KEY_FEED_HANDLER), p));
                    getRcv().numPodSync.incrementAndGet();
                }

                if (getRcv().numPodSync.get() == 0) {
                    getRcv().onRefreshDone(getRcv().getString(R.string.refresh_successful));
                }

                getRcv().prepareProgressDialog();
            }
        }

        @Override
        public void handleFailure(GPodderException e) {
            getRcv().onRefreshDone(getRcv().getString(R.string.operation_failed) + ": "
                    + e.getMessage());
        }
    };

    /**
     * The Handler for receiving PullFeedAsyncTask's results.
     */
    private static final class FeedHandler extends FeedSyncResultHandler<MainActivity> {

        @Override
        public void handle() {
            synchronized (getRcv().numPodSync) {
                checkDone();
            }
        }

        @Override
        public void handleFailure(GPodderException e) {
            Toast.makeText(getRcv(), e.getMessage(), REFRESH_MSG_DURATION_MS).show();

            checkDone();
        }

        /**
         * Check whether we have refreshed all feeds and if yes call
         * onRefreshDone.
         */
        private void checkDone() {
            synchronized (getRcv().numPodSync) {
                getRcv().curPodSync.incrementAndGet();

                if (getRcv().curPodSync.get() == getRcv().numPodSync.get()) {
                    getRcv().onRefreshDone(getRcv().getString(R.string.refresh_successful));
                }

                getRcv().prepareProgressDialog();
            }
        }

    };

    /**
     * Called when the refresh button is pressed. Displays a progress dialog and
     * starts the PullSubscriptionsAsyncTask.
     */
    private void onRefreshPressed() {
        synchronized (numPodSync) {
            if (numPodSync.get() != -1) {
                return;
            }

            numPodSync.incrementAndGet();
            curPodSync.set(0);
        }

        refreshBg.execute(new PullSubscriptionsAsyncTask((
                PodcastSyncResultHandler<? extends Activity>)
                cbCont.get(KEY_PODCAST_HANDLER)));
        startService(new Intent().setClass(this,
                PullSubscriptionsAsyncTask.class));
        progressDialog.show();
    }

    /**
     * Called when refresh is done, dismisses the progress dialog and displays
     * msg in a Toast.
     * 
     * @param msg The message displayed in a Toast.
     */
    private void onRefreshDone(String msg) {
        numPodSync.set(-1);

        progressDialog.dismiss();
        Toast.makeText(this, msg, REFRESH_MSG_DURATION_MS).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        switch (mViewPager.getCurrentItem()) {
            case SectionsPagerAdapter.POSITION_PODCASTS:
                getMenuInflater().inflate(R.menu.podcast_menu, menu);
                break;
            case SectionsPagerAdapter.POSITION_EPISODES:
                getMenuInflater().inflate(R.menu.episode_menu, menu);
                break;
            case SectionsPagerAdapter.POSITION_PLAYER:
                // getMenuInflater().inflate(R.menu.player_menu, menu);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        /* Intentionally left blank. */
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        if (menu != null) {
            menu.clear();
            switch (tab.getPosition()) {
                case 0:
                    getMenuInflater().inflate(R.menu.podcast_menu, menu);
                    break;
                case 1:
                    getMenuInflater().inflate(R.menu.episode_menu, menu);
                    break;
                case 2:
                    getMenuInflater().inflate(R.menu.player_menu, menu);
                    break;
                default:
                    System.out.println("Non-existent tab selected! Please fix");
            }
        }
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        /* Intentionally left blank. */
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the primary sections of the app.
     */
    protected class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int POSITION_PODCASTS = 0;
        public static final int POSITION_EPISODES = 1;
        public static final int POSITION_PLAYER = 2;
        public static final int POSITION_SEARCH = 3;
        public static final int TABCOUNT = POSITION_SEARCH + 1;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            /*
             * References to Fragments used within this adapter may not be kept
             * anywhere, since they are not guaranteed to stay valid. Therefore,
             * we need to create fragment instances within this method, and rely
             * on other means (for example the fragment manager) to retrieve
             * references.
             */

            switch (i) {
                case POSITION_PODCASTS:
                    return new PodListFragment();
                case POSITION_EPISODES:
                    return new EpisodeListFragment();
                case POSITION_PLAYER:
                    return new PlayerFragment();
                case POSITION_SEARCH:
                    return new SearchFragment();
                default:
                    throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public int getCount() {
            return TABCOUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case POSITION_PODCASTS:
                    return getString(R.string.podcasts).toUpperCase(Locale.getDefault());
                case POSITION_EPISODES:
                    return getString(R.string.episodes).toUpperCase(Locale.getDefault());
                case POSITION_PLAYER:
                    return getString(R.string.player).toUpperCase(Locale.getDefault());
                case POSITION_SEARCH:
                    return getString(R.string.search).toUpperCase(Locale.getDefault());
                default:
                    return null;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.playlist:
                intent = new Intent(this, PlaylistActivity.class);
                startActivity(intent);
                break;
            case R.id.refresh:
                onRefreshPressed();
                break;
            case R.id.add_new_podcast:
                intent = new Intent(this, AddPodcastActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * The user has selected a podcast in the podcasts list. Filters the episode
     * view and then switches to it. If podcast is null, all episodes are shown.
     */
    @Override
    public void onPodcastSelected(Podcast podcast) {
        getEpisodeListFragment().setPodcast(podcast);
        getActionBar().setSelectedNavigationItem(SectionsPagerAdapter.POSITION_EPISODES);
    }

    /**
     * Retrieves the currently active episode list fragment.
     */
    private EpisodeListFragment getEpisodeListFragment() {
        FragmentManager manager = getSupportFragmentManager();
        String tag = String.format("android:switcher:%d:%d", R.id.pager,
                SectionsPagerAdapter.POSITION_EPISODES);
        return (EpisodeListFragment) manager.findFragmentByTag(tag);
    }

    private PlayerFragment getPlayerFragment() {
        FragmentManager manager = getSupportFragmentManager();
        String tag = String.format("android:switcher:%d:%d", R.id.pager,
                SectionsPagerAdapter.POSITION_PLAYER);
        return (PlayerFragment) manager.findFragmentByTag(tag);
    }

    @Override
    public void onEpisodeSelected(Episode episode) {
        // TODO @Joshi start playing only if episode is downloaded or
        // downloading
        getPlayerFragment().setActiveEpisode(episode);
        getActionBar().setSelectedNavigationItem(SectionsPagerAdapter.POSITION_PLAYER);
        getPlayerFragment().startPlaying();
    }

    public void onDownloadTrashClick(View v) {
        getEpisodeListFragment().onDownloadTrashClick(v);
    }
}
