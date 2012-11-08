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
        public ParcelableByteArray createFromParcel(Parcel source) {
            byte[] moop = new byte[source.readInt()];
            source.readByteArray(moop);
            return new ParcelableByteArray(moop);
        }

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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(arr.length);
        dest.writeByteArray(arr);
    }
}
