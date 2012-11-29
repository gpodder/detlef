package at.ac.tuwien.detlef.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * Class representing changes in podcast subscriptions.
 */
public class EnhancedSubscriptionChanges implements Serializable, Parcelable {

    private static final long serialVersionUID = 1L;

    /** Added podcasts. */
    private final List<Podcast> add;

    /** Removed podcasts. */
    private final List<Podcast> remove;

    /** Timestamp of the changes. */
    private final long timestamp;

    /**
     * Create new EnhancedSubscriptionChanges.
     * 
     * The Lists passed to the constuctor are copied into Lists of Podcast. The timestamp an all
     * Podcasts is set to the given timestamp.
     * 
     * @param add List of IPodcasts to add.
     * @param remove List of IPodcasts to remove.
     * @param timestamp The timestamp of the changes.
     */
    public EnhancedSubscriptionChanges(List<IPodcast> add, List<IPodcast> remove, long timestamp) {
        this.add = new ArrayList<Podcast>(add.size());
        for (IPodcast ip : add) {
            Podcast p = new Podcast(ip);
            p.setLastUpdate(0);
            this.add.add(p);
        }

        this.remove = new ArrayList<Podcast>(remove.size());
        for (IPodcast ip : remove) {
            Podcast p = new Podcast(ip);
            p.setLastUpdate(timestamp);
            this.remove.add(p);
        }

        this.timestamp = timestamp;
    }

    public List<Podcast> getAdd() {
        return add;
    }

    public List<Podcast> getRemove() {
        return remove;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(add);
        dest.writeList(remove);
        dest.writeLong(timestamp);
    }

    public static final Parcelable.Creator<EnhancedSubscriptionChanges> CREATOR = new
            Creator<EnhancedSubscriptionChanges>() {
                @Override
                public EnhancedSubscriptionChanges[] newArray(int size) {
                    return new EnhancedSubscriptionChanges[size];
                }

                @Override
                @SuppressWarnings("unchecked")
                public EnhancedSubscriptionChanges createFromParcel(Parcel source) {
                    List<IPodcast> a = (List<IPodcast>) source.readArrayList(null);
                    List<IPodcast> r = (List<IPodcast>) source.readArrayList(null);
                    long t = source.readLong();
                    return new EnhancedSubscriptionChanges(a, r, t);
                }
            };
}
