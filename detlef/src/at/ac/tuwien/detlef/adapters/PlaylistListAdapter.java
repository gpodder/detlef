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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.domain.Episode;

public class PlaylistListAdapter extends ArrayAdapter<Episode> {

    private final ArrayList<Episode> episodes;

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
            TextView podcastName = (TextView) v
                    .findViewById(R.id.playListPodcast);
            TextView episodeName = (TextView) v
                    .findViewById(R.id.playListEpisode);
            if (podcastName != null) {
                podcastName.setText(episode.getPodcast().getTitle());
            }
            if (episode != null) {
                episodeName.setText(episode.getTitle());
            }
        }

        ImageButton removeButton = (ImageButton) v.findViewById(R.id.playListRemoveFromPlaylist);
        removeButton.setTag(position);

        ImageButton downloadButton = (ImageButton) v.findViewById(R.id.playListDownload);
        downloadButton.setTag(episode);
        setDownloadButtonStatus(downloadButton, episode);

        v.setTag(episode);
        return v;
    }

    private void setDownloadButtonStatus(ImageButton downloadButton, Episode episode) {
        switch (episode.getStorageState()) {
            case NOT_ON_DEVICE:
                downloadButton.setVisibility(ImageButton.VISIBLE);
                downloadButton.setImageResource(android.R.drawable.ic_menu_upload);
                break;
            case DOWNLOADING:
                downloadButton.setVisibility(ImageButton.VISIBLE);
                downloadButton.setImageResource(R.drawable.ic_media_stop);
                break;
            default:
                downloadButton.setVisibility(ImageButton.INVISIBLE);
        }
        downloadButton.setTag(episode);
    }
}
