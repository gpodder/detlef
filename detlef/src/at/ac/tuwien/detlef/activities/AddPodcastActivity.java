/* *************************************************************************
 *  Copyright 2012 The detlef developers                                   *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 2 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 ************************************************************************* */



package at.ac.tuwien.detlef.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.db.PodcastDAOImpl;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.gpodder.NoDataResultHandler;
import at.ac.tuwien.detlef.gpodder.PodcastListResultHandler;
import at.ac.tuwien.detlef.gpodder.ReliableResultHandler;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.commonsware.cwac.merge.MergeAdapter;
import com.dragontek.mygpoclient.simple.IPodcast;

public class AddPodcastActivity extends Activity {

    private static final String TAG = AddPodcastActivity.class.getName();

    private static final String BUNDLE_SEARCH_RESULTS = "BUNDLE_SEARCH_RESULTS";
    private static final String BUNDLE_SUGGESTIONS = "BUNDLE_SUGGESTIONS";
    private static final String BUNDLE_TOPLIST = "BUNDLE_TOPLIST";
    
    private static int podcastsAdded = 0;

    private final MergeAdapter mergeAdapter = new MergeAdapter();
    private PodcastListAdapter resultAdapter;
    private PodcastListAdapter suggestionsAdapter;
    private PodcastListAdapter toplistAdapter;

    private static final SearchResultHandler srh = new SearchResultHandler();
    private static final ToplistResultHandler trh = new ToplistResultHandler();
    private static final SuggestionResultHandler urh = new SuggestionResultHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_podcast_activity);
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        /* Set up our merged list. */

        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TextView tv = (TextView) vi.inflate(R.layout.add_podcast_list_header, null);
        tv.setText(R.string.search_results);
        mergeAdapter.addView(tv);

        resultAdapter = new PodcastListAdapter(this, android.R.layout.simple_list_item_1,
                new ArrayList<Podcast>());
        mergeAdapter.addAdapter(resultAdapter);

        tv = (TextView) vi.inflate(R.layout.add_podcast_list_header, null);
        tv.setText(R.string.suggestions);
        mergeAdapter.addView(tv);

        suggestionsAdapter = new PodcastListAdapter(this, android.R.layout.simple_list_item_1,
                new ArrayList<Podcast>());
        mergeAdapter.addAdapter(suggestionsAdapter);

        tv = (TextView) vi.inflate(R.layout.add_podcast_list_header, null);
        tv.setText(R.string.popular_podcasts);
        mergeAdapter.addView(tv);

        toplistAdapter = new PodcastListAdapter(this, android.R.layout.simple_list_item_1,
                new ArrayList<Podcast>());
        mergeAdapter.addAdapter(toplistAdapter);

        ListView lv = (ListView) findViewById(R.id.result_list);
        lv.setAdapter(mergeAdapter);

        /*
         * Iff we are starting for the first time, create dummy content for
         * suggestions and the toplist. Otherwise, saved content will be
         * restored later on.
         */

        if (savedInstanceState == null) {
            GPodderSync gps = DependencyAssistant.getDependencyAssistant().getGPodderSync();
            gps.addGetToplistJob(trh);
            gps.addGetSuggestionsJob(urh);
        }
        podcastsAdded = 0;
    }

    @Override
    protected void onPause() {
        srh.unregisterReceiver();
        trh.unregisterReceiver();
        urh.unregisterReceiver();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        srh.registerReceiver(this);
        trh.registerReceiver(this);
        urh.registerReceiver(this);
    }

    @Override
    public void onBackPressed() {
        if (podcastsAdded > 0) {
            Intent data = new Intent().putExtra(MainActivity.PODCAST_ADD_REFRESH_FEED_LIST, true);
            if (getParent() == null) {
                setResult(Activity.RESULT_OK, data);
            } else {
                getParent().setResult(Activity.RESULT_OK, data);
            }
            podcastsAdded = 0;
        } else {
            Intent data = new Intent().putExtra(MainActivity.PODCAST_ADD_REFRESH_FEED_LIST, true);
            if (getParent() == null) {
                setResult(Activity.RESULT_CANCELED, data);
            } else {
                getParent().setResult(Activity.RESULT_CANCELED, data);
            }
        }
        super.onBackPressed();
    };

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState == null) {
            return;
        }

        resultAdapter.addAll(restorePodcastsFromBundle(savedInstanceState, BUNDLE_SEARCH_RESULTS));
        suggestionsAdapter.addAll(restorePodcastsFromBundle(savedInstanceState, BUNDLE_SUGGESTIONS));
        toplistAdapter.addAll(restorePodcastsFromBundle(savedInstanceState, BUNDLE_TOPLIST));
    }

    /**
     * Retrieves the the list of parcelables with the given key from the bundle,
     * and returns it with each element cast to Podcast.
     */
    private List<Podcast> restorePodcastsFromBundle(Bundle savedInstanceState, String key) {
        List<Parcelable> parcels = savedInstanceState.getParcelableArrayList(key);
        List<Podcast> podcasts = new ArrayList<Podcast>(parcels.size());

        for (Parcelable p : parcels) {
            podcasts.add((Podcast) p);
        }

        return podcasts;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUNDLE_SEARCH_RESULTS, resultAdapter.getList());
        outState.putParcelableArrayList(BUNDLE_SUGGESTIONS, suggestionsAdapter.getList());
        outState.putParcelableArrayList(BUNDLE_TOPLIST, toplistAdapter.getList());

        super.onSaveInstanceState(outState);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // call the onbackpressed functionality for 
                // refresh feed handling
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSearchClick(View view) {
        Log.v(TAG, "onSearchClick()");

        /* Hide the soft keyboard when starting a search. */

        final TextView tv = (TextView) findViewById(R.id.search_textbox);
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);

        /* TODO: There is no progress (or 'I'm busy') indicator.*/
        
        GPodderSync gps = DependencyAssistant.getDependencyAssistant().getGPodderSync();
        gps.addSearchPodcastsJob(srh, tv.getText().toString());
    }

    public void onSubscribeClick(View view) {
        Log.v(TAG, "onSubscribeClick()");

        /*
         * Note that view is the actual button in this case. We need to retrieve
         * the tag from its parent.
         */
        View parent = (View) view.getParent();
        Podcast p = (Podcast) parent.getTag();

        PodcastDAO dao = PodcastDAOImpl.i();
        p.setLocalAdd(true);
        if (dao.insertPodcast(p) == null) {
            Toast.makeText(this, "Subscription update failed", Toast.LENGTH_SHORT);
            return;
        }

        Toast.makeText(this, "Subscription update succeeded", Toast.LENGTH_SHORT).show();

    }

    /**
     * Handles search results. On failure, notifies the user; on success,
     * displays the results.
     */
    private static class SearchResultHandler extends ReliableResultHandler<AddPodcastActivity>
    implements PodcastListResultHandler<AddPodcastActivity> {

        @Override
        public void handleFailure(int errCode, String errStr) {
            getRcv().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getRcv(), "Podcast search failed", Toast.LENGTH_SHORT);
                }
            });
        }

        @Override
        public void handleSuccess(final List<Podcast> result) {
            getRcv().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    getRcv().resultAdapter.clear();
                    getRcv().resultAdapter.addAll(result);

                    Toast.makeText(getRcv(),
                            String.format("%d results found", result.size()),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private static class ToplistResultHandler extends ReliableResultHandler<AddPodcastActivity>
    implements PodcastListResultHandler<AddPodcastActivity> {

        @Override
        public void handleFailure(int errCode, String errStr) {
            getRcv().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getRcv(), "Toplist retrieval failed", Toast.LENGTH_SHORT);
                }
            });
        }

        @Override
        public void handleSuccess(final List<Podcast> result) {
            getRcv().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getRcv().toplistAdapter.clear();
                    getRcv().toplistAdapter.addAll(result);
                }
            });
        }
        
    }
    
    private static class SuggestionResultHandler extends ReliableResultHandler<AddPodcastActivity>
    implements PodcastListResultHandler<AddPodcastActivity> {

        @Override
        public void handleFailure(int errCode, String errStr) {
            getRcv().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getRcv(), "Suggestion retrieval failed", Toast.LENGTH_SHORT);
                }
            });
        }

        @Override
        public void handleSuccess(final List<Podcast> result) {
            getRcv().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getRcv().suggestionsAdapter.clear();
                    getRcv().suggestionsAdapter.addAll(result);
                }
            });
        }
        
    }

    /**
     * This adapter displays a list of podcasts which the user can subscribe to.
     */
    private static class PodcastListAdapter extends ArrayAdapter<Podcast> {

        private final ArrayList<Podcast> podcasts;

        public PodcastListAdapter(Context context, int textViewResourceId,
                ArrayList<Podcast> objects) {
            super(context, textViewResourceId, objects);
            podcasts = objects;
        }

        public ArrayList<Podcast> getList() {
            return podcasts;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            Podcast podcast = podcasts.get(position);

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) this.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.add_podcast_list_layout, null);
            }

            v.setTag(podcast);

            TextView podcastName = (TextView) v.findViewById(R.id.podcast_name);
            podcastName.setText(podcast.getTitle());

            TextView podcastDesc = (TextView) v.findViewById(R.id.podcast_description);
            podcastDesc.setText(Html.fromHtml(podcast.getDescription()));

            return v;

        }
    }
}
