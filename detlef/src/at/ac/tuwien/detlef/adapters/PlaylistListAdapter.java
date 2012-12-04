
package at.ac.tuwien.detlef.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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

        Button removeButton = (Button) v.findViewById(R.id.playListRemoveFromPlaylist);
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
