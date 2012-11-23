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

public class SearchListAdapter extends ArrayAdapter<Episode> {

    private ArrayList<Episode> episodes;

    public SearchListAdapter(Context context, int textViewResourceId,
            ArrayList<Episode> pEpisodes) {
        super(context, textViewResourceId, pEpisodes);
        episodes = pEpisodes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.episode_list_layout, null);
        }

        Episode episode = episodes.get(position);
        if (episodes != null) {

            TextView epsiodeName = (TextView) v
                    .findViewById(R.id.episodeListDescription );


            if (episode != null) {
                epsiodeName.setText(episode.getTitle());
            }
        }

        return v;

    }
}
