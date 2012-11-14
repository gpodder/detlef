package at.ac.tuwien.detlef.gpodder;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

/**
 * A class to handle replies from the PullSubscriptionsAsyncTask.
 *
 * The Receiver has to be registered with registerReceiver() before use and unregistered with
 * unregisterReceiver() once the Receiver is destroyed.
 *
 * In order to implement handling of the Task's results the user has to subclass
 * PodcastSyncResultHandler and implement handle() and handleFailure().
 */
public abstract class PodcastSyncResultHandler<Receiver extends Activity> extends
BroadcastReceiverCallback<Receiver> {
    /** Logging tag. */
    private static final String TAG = "PodcastSyncResultHandler";

    protected PodcastSyncResultHandler() {
        super(PullSubscriptionsAsyncTask.ACTION);
    }

    @Override
    protected void deliverEvent(BroadcastReceiverCallback.BroadcastReceiverEvent e) {
        Log.d(TAG, "deliverEvent");

        Intent intent = e.getIntent();

        /* See whether the Service succeeded. */
        int status = intent.getIntExtra(PullSubscriptionsAsyncTask.EXTRA_STATE,
                PullSubscriptionsAsyncTask.TASK_SUCC);

        switch (status) {
            case PullSubscriptionsAsyncTask.TASK_FAIL:
                this.handleFailure((GPodderException) intent.getSerializableExtra(
                        PullSubscriptionsAsyncTask.EXTRA_EXCEPTION));
                break;
            case PullSubscriptionsAsyncTask.TASK_SUCC:
                this.handle((EnhancedSubscriptionChanges) intent.getSerializableExtra(
                        PullSubscriptionsAsyncTask.EXTRA_CHANGES));
                break;
            default:
                /* dafuq? */
                Log.w("PodcastSyncResultHandler", "Invalid Intent received.");
        }
    }

    /**
     * This has to be implemented by the user and is called when the Task got a subscription update.
     * @param changes The subscription changes.
     */
    public abstract void handle(EnhancedSubscriptionChanges changes);

    /**
     * This has to be implemented by the user and is called when the Task encountered an error.
     * @param e An Exception describing what went wrong.
     */
    public abstract void handleFailure(GPodderException e);
}
