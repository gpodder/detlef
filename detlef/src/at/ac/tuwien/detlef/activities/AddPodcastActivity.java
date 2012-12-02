
package at.ac.tuwien.detlef.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.gpodder.PodcastListResultHandler;
import at.ac.tuwien.detlef.gpodder.responders.SynchronousSyncResponder;

import com.commonsware.cwac.merge.MergeAdapter;

public class AddPodcastActivity extends Activity {

    private static final String TAG = AddPodcastActivity.class.getName();

    private final MergeAdapter mergeAdapter = new MergeAdapter();
    private PodcastListAdapter resultAdapter;
    private PodcastListAdapter toplistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_podcast_activity);
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        /* Set up our merged list. */

        TextView tv = new TextView(this);
        tv.setText(R.string.search_results);
        mergeAdapter.addView(tv);

        resultAdapter = new PodcastListAdapter(this, android.R.layout.simple_list_item_1,
                new ArrayList<Podcast>());
        mergeAdapter.addAdapter(resultAdapter);

        tv = new TextView(this);
        tv.setText(R.string.popular_podcasts);
        mergeAdapter.addView(tv);

        toplistAdapter = new PodcastListAdapter(this, android.R.layout.simple_list_item_1,
                new ArrayList<Podcast>());
        fillToplistWithDummies();
        mergeAdapter.addAdapter(toplistAdapter);

        ListView lv = (ListView) findViewById(R.id.result_list);
        lv.setAdapter(mergeAdapter);
    }

    private void fillToplistWithDummies() {
        Podcast p = new Podcast();
        p.setTitle("Bestest podcast evar");
        p.setDescription("This is the bestest bestest bestest bestest bestest bestest bestest bestest podcast evar");
        toplistAdapter.add(p);

        p = new Podcast();
        p.setTitle("Somebody set me up the bomb");
        p.setDescription("This is the bestest bestest bestest bestest bestest bestest bestest bestest podcast evar");
        toplistAdapter.add(p);

        p = new Podcast();
        p.setTitle("Somebody set me up the bomb: Somebody set me up the bomb: Somebody set me up the bomb");
        p.setDescription("This is the bestest bestest bestest bestest bestest bestest bestest bestest podcast evar");
        toplistAdapter.add(p);

        p = new Podcast();
        p.setTitle("Dancing babies");
        p.setDescription("This is the bestest bestest bestest bestest bestest bestest bestest bestest podcast evar "
                + "This is the bestest bestest bestest bestest bestest bestest bestest bestest podcast evar "
                + "This is the bestest bestest bestest bestest bestest bestest bestest bestest podcast evar.");
        toplistAdapter.add(p);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSearchClick(View view) {
        Log.v(TAG, "onSearchClick()");

        /* Hide the soft keyboard when starting a search. */

        final TextView tv = (TextView) findViewById(R.id.search_textbox);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);

        /*
         * TODO: If this is not run in a new thread, it blocks. As the service
         * is supposed to be async, I'm wondering whether this is intentional.
         * There is no progress (or 'I'm busy') indicator. The results are not
         * restored after screen rotations. This code won't work if the screen
         * is rotated (and the activity destroyed) while the service is busy.
         */

        final SearchResultHandler srh = new SearchResultHandler(this);
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                GPodderSync gps = new GPodderSync(new SynchronousSyncResponder(
                        AddPodcastActivity.this));
                gps.addSearchPodcastsJob(srh, tv.getText().toString());
            }
        });
        t.start();
    }

    public void onSubscribeClick(View view) {
        Log.v(TAG, "onSubscribeClick()");

        /* TODO */

        Toast.makeText(this, "When implemented, the podcast will be subscribed to here",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles search results. On failure, notifies the user; on success,
     * displays the results. TODO: Note that this does not safely handle cases
     * in which the activity has been exchanged during an ongoing search.
     */
    private static class SearchResultHandler implements PodcastListResultHandler {

        private final AddPodcastActivity activity;

        public SearchResultHandler(AddPodcastActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleFailure(int errCode, String errStr) {
            Toast.makeText(activity, "Podcast search failed", Toast.LENGTH_SHORT);
        }

        @Override
        public void handleSuccess(final List<Podcast> result) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    activity.resultAdapter.clear();
                    activity.resultAdapter.addAll(result);

                    Toast.makeText(activity,
                            String.format("%d results found", result.size()),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * This adapter displays a list of podcasts which the user can subscribe to.
     */
    private static class PodcastListAdapter extends ArrayAdapter<Podcast> {

        private final List<Podcast> podcasts;

        public PodcastListAdapter(Context context, int textViewResourceId, List<Podcast> objects) {
            super(context, textViewResourceId, objects);
            podcasts = objects;
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
