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


package at.ac.tuwien.detlef.gpodder;

import java.util.LinkedList;

/**
 * A byte-based rope data structure.
 *
 * A rope is an array/linked-list hybrid variant of the array-based string.
 *
 * @author ondra
 */
public class ByteRope {
    /** The linked list backing this rope. */
    private LinkedList<byte[]> ll;

    /** The current length, in bytes, of the rope. */
    private int len;

    /** Constructs a byte rope. */
    public ByteRope() {
        ll = new LinkedList<byte[]>();
        len = 0;
    }

    /**
     * Constructs a copy of the given byte rope.
     *
     * @param br
     *            Byte rope whose copy to construct.
     */
    public ByteRope(ByteRope br) {
        this();
        append(br.toByteArray());
    }

    /**
     * Returns The current length, in bytes, of the rope.
     *
     * @return The current length of the rope.
     */
    public int length() {
        return len;
    }

    /**
     * Returns a cohesive byte array representation of the rope.
     *
     * @return A byte array representation of the rope.
     */
    public byte[] toByteArray() {
        // it just assembles the array, really
        byte[] ret = new byte[len];
        int i = 0;

        for (byte[] barr : ll) {
            System.arraycopy(barr, 0, ret, i, barr.length);
            i += barr.length;
        }

        return ret;
    }

    /**
     * Append a byte to the rope.
     *
     * @param b
     *            The byte to append to the rope.
     */
    public void append(byte b) {
        // this is rather wasteful
        byte[] barr = { b };
        append(barr);
    }

    /**
     * Append (a copy of) an existing byte array to the rope.
     *
     * @param barr
     *            The existing byte array to append to the rope.
     */
    public void append(byte[] barr) {
        append(barr, 0, barr.length);
    }

    /**
     * Append (a copy of) a part of an existing byte array to the rope.
     *
     * @param barr
     *            The existing byte array whose part to append to the rope.
     * @param offset
     *            The offset from which to begin copying.
     * @param length
     *            The length of the slice to copy.
     */
    public void append(byte[] barr, int offset, int length) {
        byte[] myBytes = new byte[length];
        System.arraycopy(barr, offset, myBytes, 0, length);

        ll.add(myBytes);
        len += length;
    }
}
