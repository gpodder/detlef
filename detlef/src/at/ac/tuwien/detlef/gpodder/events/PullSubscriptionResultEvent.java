package at.ac.tuwien.detlef.gpodder.events;

import android.os.Bundle;

public class PullSubscriptionResultEvent {
    public final int code;
    public final Bundle bundle;
    public PullSubscriptionResultEvent(int code, Bundle bundle) {
        this.code = code;
        this.bundle = bundle;
    }
}
