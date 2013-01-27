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


package at.ac.tuwien.detlef.search;

import at.ac.tuwien.detlef.db.DatabaseHelper;
import at.ac.tuwien.detlef.db.EpisodeDAO;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;

/**
 * Provides an interface for a search functionality which takes a
 * {@link SearchCriteriaKeyword keyword} as {@link SearchCriteria} and searches for
 * it in the database.
 *
 * @author moe
 */
public class SearchKeywordDb
    implements Search<SearchCriteriaKeyword, Episode> {

    private final EpisodeDAO edao;

    public SearchKeywordDb() {
        edao = EpisodeDAOImpl.i();
    }

    @Override
    public void search(final SearchCriteriaKeyword criteria,
                       final SearchCallback<Episode> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String selection = String.format(
                                       "%s LIKE ? OR %s LIKE ?",
                                       DatabaseHelper.COLUMN_EPISODE_TITLE,
                                       DatabaseHelper.COLUMN_EPISODE_DESCRIPTION
                                   );
                String[] selectionArgs = {
                    String.format("%%%s%%", criteria.getKeyword()),
                    String.format("%%%s%%", criteria.getKeyword())
                };
                callback.getResult(edao.getEpisodesWhere(selection, selectionArgs));
            }
        }
                  ).run();
    }





}
