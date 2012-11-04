package at.ac.tuwien.detlef.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.Podcast;

public class PodListAdapter extends ArrayAdapter<Podcast> {

    private final List<Podcast> podcasts;

    public PodListAdapter(Context context, int textViewResourceId,
            List<Podcast> podcasts) {
        super(context, textViewResourceId, podcasts);
        this.podcasts = podcasts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.pod_list_layout, null);
        }

        Podcast podcast = podcasts.get(position);
        if (podcast != null) {
            TextView lastUpdate = (TextView) v
                    .findViewById(R.id.podListLastUpdate);
            TextView podcastName = (TextView) v
                    .findViewById(R.id.podListPodcastName);
            if (lastUpdate != null) {
                lastUpdate.setText("22.06.2012");
            }

            if (podcast != null) {
                podcastName.setText(podcast.getTitle());
            }
        }

        return v;

    }
}
