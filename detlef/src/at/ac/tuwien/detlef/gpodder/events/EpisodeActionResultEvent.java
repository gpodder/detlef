package at.ac.tuwien.detlef.gpodder.events;

import android.os.Bundle;

public class EpisodeActionResultEvent {
    public final int code;
    public final Bundle bundle;
    public EpisodeActionResultEvent(int code, Bundle bundle) {
        this.code = code;
        this.bundle = bundle;
    }
}
