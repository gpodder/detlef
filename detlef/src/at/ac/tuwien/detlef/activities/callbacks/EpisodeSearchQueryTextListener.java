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

package at.ac.tuwien.detlef.activities.callbacks;

import android.util.Log;
import android.widget.SearchView.OnQueryTextListener;
import at.ac.tuwien.detlef.filter.KeywordFilter;
import at.ac.tuwien.detlef.fragments.EpisodeListFragment;

/**
 * An {@link OnQueryTextListener}.
 *
 * @author moe
 */
public class EpisodeSearchQueryTextListener
    implements OnQueryTextListener {

    private static final String TAG = EpisodeSearchQueryTextListener.class.getCanonicalName();

    EpisodeListFragment episodeFragment;

    public EpisodeSearchQueryTextListener(EpisodeListFragment pEpisodeFragment) {
        episodeFragment = pEpisodeFragment;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, "onQueryTextSubmit: " + query);
        KeywordFilter titleFilter = new KeywordFilter().setKeyword(query);
        episodeFragment.getFilter().putEpisodeFilter(titleFilter);
        episodeFragment.refresh();
        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        Log.d(TAG, "onQueryTextChange: " + newText);
        episodeFragment.setKeyword(newText);
        return true;
    }
}
