package at.ac.tuwien.detlef.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
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
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.fragments.EpisodeListFragment;
import at.ac.tuwien.detlef.fragments.PlayerFragment;
import at.ac.tuwien.detlef.fragments.PodListFragment;
import at.ac.tuwien.detlef.fragments.ProgressDialogFragment;
import at.ac.tuwien.detlef.gpodder.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.gpodder.GPodderException;
import at.ac.tuwien.detlef.gpodder.PodcastSyncResultHandler;
import at.ac.tuwien.detlef.gpodder.PullSubscriptionsAsyncTask;

public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener, PodListFragment.OnPodcastSelectedListener {

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
        // Create the adapter that will return a fragment for each of the three
        // primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

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
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
        progressDialog = ProgressDialogFragment.newInstance(getString(R.string.refreshing),
                getString(R.string.refreshing_feed_list));
        progressDialog.setCancelable(false);

        /* Register the PodcastSyncResultHandler. */
        psrh.register(this);
    }

    @Override
    public void onDestroy() {
        /* Unregister the PodcastSyncResultHandler. */
        psrh.unregister(this);

        super.onDestroy();
    }

    /**
     * The Toast with the Output of the refresh operation is shown this long.
     */
    private static final int REFRESH_MSG_DURATION_MS = 5000;

    /**
     * The progress dialog is identified by this tag. This is needed to remove it even if the
     * actual object has been invalidated (by a screen rotation for example).
     */
    private static final String PROGRESS_DIALOG_TAG = "PROGRESS_DIALOG_TAG";

    /**
     * The progress dialog displayed during a refresh.
     */
    private ProgressDialogFragment progressDialog;

    /**
     * The Handler for receiving PullSubscriptionsAsyncTask's results.
     */
    private final PodcastSyncResultHandler psrh = new PodcastSyncResultHandler() {

        @Override
        public void handle(EnhancedSubscriptionChanges changes) {
            /* Let's not do this, as getPodcastDBAssistant() currently returns null. */
            //DependencyAssistant.DEPENDENCY_ASSISTANT.getPodcastDBAssistant()
            //        .applySubscriptionChanges(changes);

            onRefreshDone(getString(R.string.refresh_successful));
        }

        @Override
        public void handleFailure(GPodderException e) {
            onRefreshDone(getString(R.string.operation_failed) + ": " + e.getMessage());
        }

    };

    /**
     * Called when the refresh button is pressed. Displays a progress dialog and starts the
     * PullSubscriptionsAsyncTask.
     */
    private void onRefreshPressed() {
        startService(new Intent().setClass(this, PullSubscriptionsAsyncTask.class));
        progressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
    }

    /**
     * Called when refresh is done, dismisses the progress dialog and displays msg in a Toast.
     * @param msg The message displayed in a Toast.
     */
    private void onRefreshDone(String msg) {
        ProgressDialogFragment.dismiss(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
        Toast.makeText(this, msg, REFRESH_MSG_DURATION_MS).show();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        // TODO Auto-generated method stub
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        switch (mViewPager.getCurrentItem()) {
        case 0:
            getMenuInflater().inflate(R.menu.podcast_menu, menu);
            break;
        case 1:
            getMenuInflater().inflate(R.menu.episode_menu, menu);
            break;
        case 2:
            // getMenuInflater().inflate(R.menu.player_menu, menu);
            break;
        default:
            return false;
        }
        return true;
    }

    public void onTabUnselected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
    }

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

    public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the primary sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int POSITION_PODCASTS = 0;
        public static final int POSITION_EPISODES = 1;
        public static final int POSITION_PLAYER = 2;
        public static final int TABCOUNT = POSITION_PLAYER + 1;

        private final PodListFragment podList = new PodListFragment();
        private final EpisodeListFragment episodeList = new EpisodeListFragment();
        private final PlayerFragment player = new PlayerFragment();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == POSITION_PODCASTS) {
                return podList;
            } else if (i == POSITION_EPISODES) {
                return episodeList;
            } else {
                return player;
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
                return getString(R.string.podcasts).toUpperCase();
            case POSITION_EPISODES:
                return getString(R.string.episodes).toUpperCase();
            case POSITION_PLAYER:
                return getString(R.string.player).toUpperCase();
            default:
                return null;
            }
        }

        public PodListFragment getPodList() {
            return podList;
        }

        public EpisodeListFragment getEpisodeList() {
            return episodeList;
        }

        public PlayerFragment getPlayer() {
            return player;
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
    public void onPodcastSelected(Podcast podcast) {
        mSectionsPagerAdapter.getEpisodeList().setPodcast(podcast);
        getActionBar().setSelectedNavigationItem(
                SectionsPagerAdapter.POSITION_EPISODES);
    }
}
