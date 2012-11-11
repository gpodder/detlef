package at.ac.tuwien.detlef.gpodder;

import java.io.Serializable;
import java.util.List;

import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * Class representing changes in podcast subscriptions.
 */
public class EnhancedSubscriptionChanges implements Serializable {
    /** Added podcasts. */
    private final List<IPodcast> add;

    /** Removed podcasts. */
    private final List<IPodcast> remove;

    /** Timestamp of the changes. */
    private final long timestamp;

    public EnhancedSubscriptionChanges(List<IPodcast> add, List<IPodcast> remove, long timestamp) {
        this.add = add;
        this.remove = remove;
        this.timestamp = timestamp;
    }

    public List<IPodcast> getAdd() {
        return add;
    }

    public List<IPodcast> getRemove() {
        return remove;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
