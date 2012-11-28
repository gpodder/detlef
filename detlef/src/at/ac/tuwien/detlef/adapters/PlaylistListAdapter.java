
package at.ac.tuwien.detlef.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.Episode;

public class PlaylistListAdapter extends ArrayAdapter<Episode> {

    private ArrayList<Episode> episodes;

    public PlaylistListAdapter(Context context, int textViewResourceId,
            ArrayList<Episode> episodes) {
        super(context, textViewResourceId, episodes);
        this.episodes = episodes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.playlist_list_layout, null);
        }

        Episode episode = episodes.get(position);
        if (episode != null) {
            TextView podcastname = (TextView) v
                    .findViewById(R.id.playListPodcast);
            TextView episodeView = (TextView) v
                    .findViewById(R.id.playListEpisode);
            if (podcastname != null) {
                podcastname.setText("Podcast Name");
            }

            if (episode != null) {
                episodeView.setText(episode.getTitle());
            }
        }

        return v;
    }

}
