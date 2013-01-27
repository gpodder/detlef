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

package at.ac.tuwien.detlef.settings;

import at.ac.tuwien.detlef.domain.DeviceId;

/**
 * Generates a new {@link DeviceId}.
 * @author moe
 *
 */
public interface DeviceIdGenerator {

    /**
     * Generates a new {@link DeviceId} by some algorithm intrinsic to
     * the concrete implementation.
     * @return The newly generated DeviceId which must be a) unique and b) do not expose
     *     any private data (e.g. it must not be possible to track back sensitive user data like
     *     ANDROID_ID).
     */
    DeviceId generate();

}
