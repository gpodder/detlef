
package at.ac.tuwien.detlef.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.Episode;

public class EpisodeListAdapter extends ArrayAdapter<Episode> {

    private final List<Episode> episodes;

    public EpisodeListAdapter(Context context, int textViewResourceId,
            List<Episode> episodes) {
        super(context, textViewResourceId, episodes);
        this.episodes = episodes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Episode episode = episodes.get(position);

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.episode_list_layout, null);
        }

        v.setTag(episode);

        TextView title = (TextView) v.findViewById(R.id.episodeListEpisode);
        title.setText(formatTitle(episode));

        TextView description = (TextView) v.findViewById(R.id.episodeListDescription);
        description.setText(episode.getDescription());

        TextView size = (TextView) v.findViewById(R.id.episodeListDlSize);
        size.setText(byteToHumanSize(episode.getFileSize()));

        ImageButton episodeListDownload = (ImageButton) v.findViewById(R.id.episodeListDownload);
        episodeListDownload.setTag(episode);

        ImageButton episodeListDelete = (ImageButton) v.findViewById(R.id.episodeListDelete);
        episodeListDelete.setTag(episode);

        ImageButton episodeListAddToPlaylist = (ImageButton) v
                .findViewById(R.id.episodeListAddToPlaylist);
        episodeListAddToPlaylist.setTag(episode);

        CheckBox episodeListMarkRead = (CheckBox) v
                .findViewById(R.id.episodeListMarkRead);
        episodeListMarkRead.setTag(episode);

        return v;

    }

    private static final int MAX_TITLE_LENGTH = 16;
    private static final String ELLIPSIS = "...";

    private String formatTitle(Episode episode) {
        String podcastTitle = episode.getPodcast().getTitle();
        if (podcastTitle.length() > MAX_TITLE_LENGTH) {
            podcastTitle = String.format("%s%s",
                    podcastTitle.substring(0, MAX_TITLE_LENGTH - ELLIPSIS.length()),
                    ELLIPSIS);
        }
        return String.format("%s: %s", podcastTitle, episode.getTitle());
    }

    private static final int MAX_VALUE = 1024;
    private static final int SUBUNITS_PER_UNIT = 1024;

    private CharSequence byteToHumanSize(long fileSize) {
        final String[] units = {
                "B", "KB", "MB", "GB", "TB"
        };

        double value = fileSize;
        int unitIndex;

        for (unitIndex = 0; unitIndex < units.length; unitIndex++) {
            if (value < MAX_VALUE) {
                break;
            }
            value /= SUBUNITS_PER_UNIT;
        }

        return String.format("%.2f %s", value, units[unitIndex]);
    }
}
