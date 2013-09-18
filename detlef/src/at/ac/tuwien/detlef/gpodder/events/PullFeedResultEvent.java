package at.ac.tuwien.detlef.gpodder.events;

import android.os.Bundle;

public class PullFeedResultEvent {
    public final int code;
    public final Bundle bundle;
    public PullFeedResultEvent(int code, Bundle bundle) {
        this.code = code;
        this.bundle = bundle;
    }
}
