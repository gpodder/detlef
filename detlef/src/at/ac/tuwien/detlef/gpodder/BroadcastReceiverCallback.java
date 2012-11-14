package at.ac.tuwien.detlef.gpodder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.callbacks.ReliableCallback;

public abstract class BroadcastReceiverCallback<Receiver> extends ReliableCallback<Receiver,
BroadcastReceiverCallback.BroadcastReceiverEvent> {

    private final String action;
    private final BroadcastReceiver bcastRcv = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            BroadcastReceiverEvent e = new BroadcastReceiverEvent(context, intent);
            if (!isReady()) {
                queueEvent(e);
                return;
            }

            deliverEvent(e);
        }

    };

    private Receiver rcv = null;

    protected BroadcastReceiverCallback(String action) {
        this.action = action;
    }

    protected Receiver getRcv() {
        return rcv;
    }

    @Override
    public final void registerReceiver(Receiver rcv) {
        if (isReady()) {
            unregisterReceiver();
        }

        this.rcv = rcv;

        super.registerReceiver(rcv);
    }

    @Override
    public final void unregisterReceiver() {
        if (isReady()) {
            rcv = null;
        }
    }

    @Override
    public final void init() {
        IntentFilter fil = new IntentFilter(action);
        LocalBroadcastManager.getInstance(Detlef.getAppContext()).registerReceiver(bcastRcv, fil);
    }

    @Override
    public final void destroy() {
        LocalBroadcastManager.getInstance(Detlef.getAppContext()).unregisterReceiver(bcastRcv);
    }


    @Override
    protected final boolean isReady() {
        return rcv != null;
    }

    protected static final class BroadcastReceiverEvent {
        private final Context context;
        private final Intent intent;

        public BroadcastReceiverEvent(Context context, Intent intent) {
            this.context = context;
            this.intent = intent;
        }

        public Context getContext() {
            return context;
        }

        public Intent getIntent() {
            return intent;
        }
    }
}
