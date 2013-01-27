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


package at.ac.tuwien.detlef.gpodder.plumbing;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A {@link Parcelable} container for a byte array.
 * @author ondra
 */
public class ParcelableByteArray implements Parcelable {
    private byte[] arr;

    public static final Parcelable.Creator<ParcelableByteArray> CREATOR = new
    Parcelable.Creator<ParcelableByteArray>() {
        @Override
        public ParcelableByteArray createFromParcel(Parcel source) {
            byte[] moop = new byte[source.readInt()];
            source.readByteArray(moop);
            return new ParcelableByteArray(moop);
        }

        @Override
        public ParcelableByteArray[] newArray(int size) {
            return new ParcelableByteArray[size];
        }
    };

    public ParcelableByteArray(byte[] array) {
        setArray(array);
    }

    public ParcelableByteArray() {
        this(null);
    }

    public byte[] getArray() {
        return arr;
    }

    public void setArray(byte[] array) {
        this.arr = array;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(arr.length);
        dest.writeByteArray(arr);
    }
}
