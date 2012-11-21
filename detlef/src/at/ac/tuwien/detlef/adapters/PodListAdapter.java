package at.ac.tuwien.detlef.adapters;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    private static final long MS_PER_SEC = 1000;

    private final List<Podcast> podcasts;

    public PodListAdapter(Context context, int textViewResourceId,
            List<Podcast> podcasts) {
        super(context, textViewResourceId, podcasts);
        this.podcasts = podcasts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Podcast podcast = podcasts.get(position);

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.pod_list_layout, null);
        }

        v.setTag(podcast);

        TextView lastUpdate = (TextView) v.findViewById(R.id.podListLastUpdate);
        if (lastUpdate != null) {
            Date date = new Date(podcast.getLastUpdate() * MS_PER_SEC);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            lastUpdate.setText(sdf.format(date));
        }

        TextView podcastName = (TextView) v
                .findViewById(R.id.podListPodcastName);
        if (podcast != null) {
            podcastName.setText(podcast.getTitle());
        }

        return v;

    }
}
