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
