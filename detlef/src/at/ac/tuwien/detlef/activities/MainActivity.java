package at.ac.tuwien.detlef.activities;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
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
import android.widget.Toast;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.db.PodcastDBAssistant;
import at.ac.tuwien.detlef.db.PodcastDBAssistantImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.fragments.EpisodeListFragment;
import at.ac.tuwien.detlef.fragments.PlayerFragment;
import at.ac.tuwien.detlef.fragments.PodListFragment;
import at.ac.tuwien.detlef.gpodder.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.gpodder.FeedSyncResultHandler;
import at.ac.tuwien.detlef.gpodder.GPodderException;
import at.ac.tuwien.detlef.gpodder.PodcastSyncResultHandler;
import at.ac.tuwien.detlef.gpodder.PullFeedAsyncTask;
import at.ac.tuwien.detlef.gpodder.PullSubscriptionsAsyncTask;

public class MainActivity extends FragmentActivity
implements ActionBar.TabListener, PodListFragment.OnPodcastSelectedListener,
EpisodeListFragment.OnEpisodeSelectedListener {

    private static String TAG = MainActivity.class.getName();

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

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        if (savedInstanceState != null) {
            curPodSync = new AtomicInteger(savedInstanceState.getInt(KEY_CUR_POD_SYNC, 0));
            numPodSync = new AtomicInteger(savedInstanceState.getInt(KEY_NUM_POD_SYNC, -1));
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
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

        /* Register the PodcastSyncResultHandler. */
        podcastHandler.register(this);
        /* Register the FeedSyncResultHandler. */
        feedHandler.register(this);
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

        /* Unregister the PodcastSyncResultHandler. */
        podcastHandler.unregister(this);
        /* Unregister the FeedSyncResultHandler. */
        feedHandler.unregister(this);

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
     * The Toast with the Output of the refresh operation is shown this long.
     */
    private static final int REFRESH_MSG_DURATION_MS = 5000;

    /**
     * The progress dialog displayed during a refresh.
     */
    private ProgressDialog progressDialog;

    private static final String KEY_NUM_POD_SYNC = "KEY_NUM_POD_SYNC";

    private static final String KEY_CUR_POD_SYNC = "KEY_CUR_POD_SYNC";

    /** Number of feeds to sync, -1 if no refresh is in progress. */
    private AtomicInteger numPodSync = new AtomicInteger(-1);

    /** Number of feeds already synchronized. */
    private AtomicInteger curPodSync = new AtomicInteger(0);

    /**
     * The Handler for receiving PullSubscriptionsAsyncTask's results.
     */
    private final PodcastSyncResultHandler podcastHandler =
            new PodcastSyncResultHandler() {

        @Override
        public void handle(EnhancedSubscriptionChanges changes) {
            PodcastDBAssistant pda = new PodcastDBAssistantImpl();

            pda.applySubscriptionChanges(MainActivity.this, changes);

            synchronized (numPodSync) {
                for (Podcast p : pda.getAllPodcasts(MainActivity.this)) {
                    Intent i =
                            new Intent().setClass(MainActivity.this,
                                    PullFeedAsyncTask.class);
                    i.putExtra(PullFeedAsyncTask.EXTRA_PODCAST, p);
                    startService(i);
                    numPodSync.incrementAndGet();
                }

                if (numPodSync.get() == 0) {
                    onRefreshDone(getString(R.string.refresh_successful));
                }

                prepareProgressDialog();
            }
        }

        @Override
        public void handleFailure(GPodderException e) {
            onRefreshDone(getString(R.string.operation_failed) + ": "
                    + e.getMessage());
        }

    };

    /**
     * The Handler for receiving PullFeedAsyncTask's results.
     */
    private final FeedSyncResultHandler feedHandler =
            new FeedSyncResultHandler() {

        @Override
        public void handle() {
            synchronized (numPodSync) {
                checkDone();
            }
        }

        @Override
        public void handleFailure(GPodderException e) {
            Toast.makeText(MainActivity.this, e.getMessage(),
                    REFRESH_MSG_DURATION_MS).show();

            checkDone();
        }

        private void checkDone() {
            synchronized (numPodSync) {
                curPodSync.incrementAndGet();

                if (curPodSync.get() == numPodSync.get()) {
                    onRefreshDone(getString(R.string.refresh_successful));
                }

                prepareProgressDialog();
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

        // TODO: Disable refresh button
        startService(new Intent().setClass(this,
                PullSubscriptionsAsyncTask.class));
        progressDialog.show();
    }

    /**
     * Called when refresh is done, dismisses the progress dialog and displays
     * msg in a Toast.
     * 
     * @param msg
     *            The message displayed in a Toast.
     */
    private void onRefreshDone(String msg) {
        numPodSync.set(-1);

        // TODO: Disable reenable button
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
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int POSITION_PODCASTS = 0;
        public static final int POSITION_EPISODES = 1;
        public static final int POSITION_PLAYER = 2;
        public static final int TABCOUNT = POSITION_PLAYER + 1;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            /* References to Fragments used within this adapter may not be kept anywhere,
             * since they are not guaranteed to stay valid. Therefore, we need to create fragment
             * instances within this method, and rely on other means (for example the fragment manager)
             * to retrieve references.
             */

            switch (i) {
            case POSITION_PODCASTS:
                return new PodListFragment();
            case POSITION_EPISODES:
                return new EpisodeListFragment();
            case POSITION_PLAYER:
                return new PlayerFragment();
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
        case R.id.search:
            intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            break;
        case R.id.refresh:
            onRefreshPressed();
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
        return (EpisodeListFragment)manager.findFragmentByTag(tag);
    }

    @Override
    public void onEpisodeSelected(Episode episode) {
        // TODO
        // mSectionsPagerAdapter.getPlayer().setEpisode(episode);
        getActionBar().setSelectedNavigationItem(SectionsPagerAdapter.POSITION_PLAYER);
        // mSectionsPagerAdapter.getPlayer().startPlaying();
    }
}
