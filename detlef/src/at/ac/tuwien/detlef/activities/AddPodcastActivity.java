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
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.db.PodcastDAO;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.gpodder.PodcastListResultHandler;
import at.ac.tuwien.detlef.gpodder.PodcastResultHandler;
import at.ac.tuwien.detlef.gpodder.PodderIntentService;
import at.ac.tuwien.detlef.gpodder.ReliableResultHandler;

import com.commonsware.cwac.merge.MergeAdapter;

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
    private static final SuggestionResultHandler urh = new SuggestionResultHandler();
    private static final AddPodcastResultHandler prh = new AddPodcastResultHandler();

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

        final TextView searchBox = (TextView) findViewById(R.id.search_textbox);
        final ImageButton searchButton = (ImageButton) findViewById(R.id.search_button);
        String text = searchBox.getText().toString();
        if (text.startsWith("http://") || text.startsWith("https://")) {
            searchButton.setImageDrawable(getResources().getDrawable(
                                              android.R.drawable.ic_menu_add));
        } else {
            searchButton.setImageDrawable(getResources().getDrawable(
                                              R.drawable.ic_action_search));
        }

        searchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = searchBox.getText().toString();
                if (text.startsWith("http://") || text.startsWith("https://")) {
                    searchButton.setImageDrawable(getResources().getDrawable(
                                                      android.R.drawable.ic_menu_add));
                } else {
                    searchButton.setImageDrawable(getResources().getDrawable(
                                                      R.drawable.ic_action_search));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        /*
         * Iff we are starting for the first time, load toplist and suggestions
         * contents from gpodder. Otherwise, saved content will be restored
         * later on.
         */

        if (savedInstanceState == null) {
            GPodderSync gps = Singletons.i().getGPodderSync();
            gps.addGetSuggestionsJob(urh);

            startService(new Intent(this, PodderIntentService.class).putExtra(
                             PodderIntentService.EXTRA_REQUEST,
                             PodderIntentService.REQUEST_TOPLIST).putExtra(
                             PodderIntentService.EXTRA_RESULT_RECEIVER,
                             new ToplistResultReceiver(new Handler(), this)));
        }
        podcastsAdded = 0;
    }

    private static class ToplistResultReceiver extends ResultReceiver {

        final AddPodcastActivity activity;

        public ToplistResultReceiver(Handler handler, AddPodcastActivity activity) {
            super(handler);
            this.activity = activity;
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == PodderIntentService.RESULT_FAILURE) {
                Toast.makeText(activity, "Toplist retrieval failed", Toast.LENGTH_SHORT);
                return;
            }

            List<Podcast> podcasts = resultData.getParcelableArrayList(PodderIntentService.EXTRA_RESULT_PODCAST_LIST);
            Log.d(TAG, String.format("Got results back: %s", podcasts));

            activity.toplistAdapter.clear();
            activity.toplistAdapter.addAll(filterSubscribedPodcasts(podcasts));
        }

    }

    @Override
    protected void onPause() {
        srh.unregisterReceiver();
        urh.unregisterReceiver();
        prh.unregisterReceiver();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        srh.registerReceiver(this);
        urh.registerReceiver(this);
        prh.registerReceiver(this);
    }

    @Override
    public void onBackPressed() {
        final Intent data = new Intent().putExtra(MainActivity.PODCAST_ADD_REFRESH_FEED_LIST, true);
        final int result = (podcastsAdded > 0) ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
        final Activity target = (getParent() == null) ? this : getParent();

        target.setResult(result, data);
        podcastsAdded = 0;

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

        String text = tv.getText().toString();
        setBusy(true);
        if (text.startsWith("http://") || text.startsWith("https://")) {
            PodcastDAO dao = Singletons.i().getPodcastDAO();
            for (Podcast p : dao.getAllPodcasts()) {
                if (p.getUrl().equals(text)) {
                    Toast.makeText(this,
                                   "This podcast is already in your podcast list!",
                                   Toast.LENGTH_LONG).show();
                    setBusy(false);
                    return;
                }
            }

            GPodderSync gps = Singletons.i().getGPodderSync();
            ArrayList<String> urls = new ArrayList<String>();
            urls.add(tv.getText().toString());
            gps.addGetPodcastInfoJob(prh, urls);
        } else {
            GPodderSync gps = Singletons.i().getGPodderSync();
            gps.addSearchPodcastsJob(srh, tv.getText().toString());
        }
    }

    private void setBusy(boolean busy) {
        View v = findViewById(R.id.search_button);
        v.setVisibility(busy ? View.GONE : View.VISIBLE);

        v = findViewById(R.id.search_progress);
        v.setVisibility(busy ? View.VISIBLE : View.GONE);
    }

    public void onSubscribeClick(View view) {
        Log.v(TAG, "onSubscribeClick()");

        /*
         * Note that view is the actual button in this case. We need to retrieve
         * the tag from its parent.
         */
        View parent = (View) view.getParent();
        Podcast p = (Podcast) parent.getTag();

        resultAdapter.removePodcast(p);
        suggestionsAdapter.removePodcast(p);
        toplistAdapter.removePodcast(p);

        PodcastDAO dao = Singletons.i().getPodcastDAO();
        p.setLocalAdd(true);
        if (dao.insertPodcast(p) == null) {
            Toast.makeText(this, "Subscription update failed", Toast.LENGTH_SHORT);
            return;
        }

        Toast.makeText(this, "Subscription update succeeded", Toast.LENGTH_SHORT).show();
        podcastsAdded++;
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
                    getRcv().setBusy(false);

                    Toast.makeText(getRcv(), "Podcast search failed", Toast.LENGTH_SHORT);
                }
            });
        }

        @Override
        public void handleSuccess(final List<Podcast> result) {
            final List<Podcast> filteredResult = filterSubscribedPodcasts(result);
            getRcv().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    getRcv().setBusy(false);

                    getRcv().resultAdapter.clear();
                    getRcv().resultAdapter.addAll(filteredResult);

                    Toast.makeText(getRcv(),
                                   String.format("%d results found", filteredResult.size()),
                                   Toast.LENGTH_SHORT).show();
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
                    getRcv().suggestionsAdapter.addAll(filterSubscribedPodcasts(result));
                }
            });
        }

    }

    private static class AddPodcastResultHandler extends ReliableResultHandler<AddPodcastActivity>
        implements PodcastResultHandler<AddPodcastActivity> {

        @Override
        public void handleFailure(int errCode, final String url) {
            getRcv().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                        getRcv(),
                        "This podcast is not known to GPodder! "
                        + "Please add it via web gpodder.net. It will be available on your device in a few days.",
                        Toast.LENGTH_LONG).show();
                    getRcv().setBusy(false);
                }
            });
        }

        @Override
        public void handleSuccess(final Podcast result) {
            getRcv().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PodcastDAO dao = Singletons.i().getPodcastDAO();
                    for (Podcast p : dao.getAllPodcasts()) {
                        if (p.getUrl().equals(result.getUrl())) {
                            Toast.makeText(getRcv(),
                                           "This podcast is already in your podcast list!",
                                           Toast.LENGTH_LONG).show();
                            getRcv().setBusy(false);
                            return;
                        }
                    }
                    result.setLocalAdd(true);
                    if (dao.insertPodcast(result) == null) {
                        Toast.makeText(getRcv(), "Add podcast from URL failed", Toast.LENGTH_SHORT);
                        getRcv().setBusy(false);
                        return;
                    }

                    getRcv().resultAdapter.removePodcast(result);
                    getRcv().suggestionsAdapter.removePodcast(result);
                    getRcv().toplistAdapter.removePodcast(result);
                    Toast.makeText(getRcv(), "Add podcast from URL succeeded", Toast.LENGTH_SHORT).show();
                    podcastsAdded++;
                    getRcv().setBusy(false);
                }
            });
        }
    }

    /**
     * Removes podcasts we are already subscribed to from in and returns the
     * resulting list.
     */
    private static List<Podcast> filterSubscribedPodcasts(List<Podcast> in) {
        List<Podcast> out = new ArrayList<Podcast>(in);

        PodcastDAO dao = Singletons.i().getPodcastDAO();
        for (int i = out.size() - 1; i >= 0; i--) {
            if (dao.getPodcastByUrl(out.get(i).getUrl()) != null) {
                out.remove(i);
            }
        }

        return out;
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

        public void removePodcast(Podcast p) {
            Iterator<Podcast> it = podcasts.iterator();
            while (it.hasNext()) {
                Podcast q = it.next();
                if (q.getUrl().equals(p.getUrl())) {
                    it.remove();
                }
            }
            notifyDataSetChanged();
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

            if ((podcastDesc != null) && ((podcast != null) & (podcast.getDescription() != null))) {
                podcastDesc.setText(Html.fromHtml(podcast.getDescription()));
            }

            return v;
        }
    }
}
