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

package at.ac.tuwien.detlef.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.ActionState;
import at.ac.tuwien.detlef.domain.Episode.StorageState;

public class EpisodeListAdapter extends ArrayAdapter<Episode> {

    private static final String TAG = EpisodeListAdapter.class.getName();

    private final List<Episode> episodes;

    private List<Episode> episodesFull;

    private String keyword = null;

    public EpisodeListAdapter(Context context, int textViewResourceId,
            List<Episode> episodes) {
        super(context, textViewResourceId, episodes);
        this.episodes = episodes;
        episodesFull = new ArrayList<Episode>(episodes);
    }

    public EpisodeListAdapter setKeyword(String pKeyword) {
        keyword = pKeyword.toLowerCase();

        if (keyword == null) {
            return this;
        }
       
        episodes.clear();

        for (Episode episode : episodesFull) {
            Log.d(TAG, "keyword filter active");
            if (episode.getTitle().toLowerCase().contains(keyword)) {
                episodes.add(episode);
            }
        }

        return this;

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
        Log.d(TAG, "getView()");

        v.setTag(episode);

        TextView title = (TextView) v.findViewById(R.id.episodeListEpisode);
        title.setText(formatTitle(episode));

        ImageButton episodeListMarkRead = (ImageButton) v.findViewById(R.id.episodeListMarkRead);
        episodeListMarkRead.setTag(episode);

        toggleEpisodeReadAppearance(episode, title, episodeListMarkRead);

        TextView description = (TextView) v.findViewById(R.id.episodeListDescription);
        description.setText(Html.fromHtml(episode.getDescription()));

        TextView size = (TextView) v.findViewById(R.id.episodeListDlSize);
        size.setText(byteToHumanSize(episode.getFileSize()));
        size.setCompoundDrawablesWithIntrinsicBounds(null,
                episode.getPodcast().getLogoIcon(), null, null);

        ImageButton episodeListDownload = (ImageButton) v.findViewById(R.id.episodeListDownload);
        episodeListDownload.setImageResource(stateToImageResource(episode.getStorageState()));
        episodeListDownload.setTag(episode);

        ImageButton episodeListAddToPlaylist =
                (ImageButton) v.findViewById(R.id.episodeListAddToPlaylist);
        episodeListAddToPlaylist.setTag(episode);

        return v;
    }

    private void toggleEpisodeReadAppearance(Episode episode, TextView title,
            ImageButton markRead) {
        if (episode.getActionState() == ActionState.DELETE) {
            title.setTypeface(Typeface.DEFAULT);
            title.setTextColor(Color.parseColor("#11AADD"));
            markRead.setImageResource(android.R.drawable.ic_menu_set_as);
        } else {
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setTextColor(Color.parseColor("#0099CC"));
            markRead.setImageResource(R.drawable.ic_menu_star);
        }
    }

    private int stateToImageResource(StorageState storageState) {
        switch (storageState) {
            case NOT_ON_DEVICE:
                return android.R.drawable.ic_menu_upload;
            case DOWNLOADING:
                return R.drawable.ic_media_stop;
            case DOWNLOADED:
                return android.R.drawable.ic_menu_delete;
            default:
                Log.e(TAG, "Unknown storage state encountered");
                return 0;
        }
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
