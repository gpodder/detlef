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
 * Generates a completely random {@link DeviceId}.
 * @author moe
 *
 */
public class DeviceIdGeneratorRandom 
    implements DeviceIdGenerator {
    
    /**
     * The length of the random ID.
     */
    private static final int ID_LENGTH = 12; 
    
    /** ASCII Offset of the lower chars (a-z). */
    private static final int LOWER_OFFSET = 97;
    
    /** ASCII Offset of the upper chars (A-Z). */
    private static final int UPPER_OFFSET = 65;
    
    /** ASCII Offset of the digits (0-9). */
    private static final int DIGIT_OFFSET = 48;
    
    /** Number of chars in the alphabet. */
    private static final int CHAR_COUNT = 26;

    /** Number of digits in the alphabet. */
    private static final int DIGIT_COUNT = 10;
    
    /** 
     *  The number of possible cases.
     *  In our case we have 3: lower case chars, upper case chars and digits.
     */
    private static final int CASE_COUNT = 3;
    
    public DeviceId generate() {
        
        
        char[] chars = new char[ID_LENGTH];
        
        for (int i = 0; i < ID_LENGTH; i++) {
            
            switch ((int) (Math.random() * (CASE_COUNT + 1))) {
                case 0:
                default:
                    chars[i] = (char) (UPPER_OFFSET + (int) (Math.random() * CHAR_COUNT));
                    break;
                case 1:
                    chars[i] = (char) (LOWER_OFFSET + (int) (Math.random() * CHAR_COUNT));
                    break;
                case 2:
                    chars[i] = (char) (DIGIT_OFFSET + (int) (Math.random() * DIGIT_COUNT));
                    break;
            }
            
             
        }
        
        return new DeviceId(new String(chars));

    }
    
}
