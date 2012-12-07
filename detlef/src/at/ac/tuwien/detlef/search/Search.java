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

/**
 * Provides a very abstract interface for search functionality which takes some
 * {@link SearchCriteria search criteria} and invokes a callback function after the search has
 * finished and results are available.
 * 
 * <p>The intention is to make this interface as flexible as possible so that also more advanced
 * searches (using different filters, multiple keyword combinations, data sources) can all be
 * implementations of this interface.</p>
 * 
 * @param <S> The type of {@link SearchCriteria} that the implementation accepts.
 * @param <T> The type of object that shall be searched for, typically this will be something
 *     like {@link Episode}, {@link Podcast} or the like. 
 * 
 * @author moe
 */
public interface Search<S extends SearchCriteria, T> {
    
    /**
     * Performs an asynchronous search for a given criteria.
     * @param criteria The criteria that is passed to the implementation. A criteria can be a
     *    very simple thing like a keyword, but also a complex set of arbitrary boolean options,
     *    keyword combinations and the like.
     * @param callback The callback function that is invoked after the search has finished.
     */
    void search(S criteria, SearchCallback<T> callback);
    
}
