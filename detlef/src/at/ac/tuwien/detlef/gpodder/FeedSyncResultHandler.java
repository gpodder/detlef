package at.ac.tuwien.detlef.gpodder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * A class to handle replies from the PullFeedAsyncTask.
 *
 * The Receiver has to be registered with register() before use and unregistered with unregister()
 * once the receiving Activity is destroyed. The Receiver can only receive Intents from within the
 * same Application.
 *
 * In order to implement handling of the Task's results the user has to subclass
 * FeedSyncResultHandler and implement handle() and handleFailure().
 */
public abstract class FeedSyncResultHandler extends BroadcastReceiver {
    /** Logging tag. */
    private static final String TAG = "PodcastSyncResultHandler";

    /**
     * Register the receiver to the given context.
     * @param context The context.
     */
    public final void register(Context context) {
        IntentFilter fil = new IntentFilter(PullFeedAsyncTask.ACTION);
        LocalBroadcastManager.getInstance(context).registerReceiver(this, fil);
    }

    /**
     * Unregister the receiver from the given context.
     * @param context The context.
     */
    public final void unregister(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        /* See whether the Service succeeded. */
        int status = intent.getIntExtra(PullFeedAsyncTask.EXTRA_STATE,
                PullFeedAsyncTask.TASK_SUCC);

        switch (status) {
            case PullFeedAsyncTask.TASK_FAIL:
                this.handleFailure((GPodderException) intent.getSerializableExtra(
                        PullFeedAsyncTask.EXTRA_EXCEPTION));
                break;
            case PullFeedAsyncTask.TASK_SUCC:
                this.handle();
                break;
            default:
                /* dafuq? */
                Log.w("PodcastSyncResultHandler", "Invalid Intent received.");
        }
    }

    /**
     * This has to be implemented by the user and is called when the Task is done.
     */
    public abstract void handle();

    /**
     * This has to be implemented by the user and is called when the Task encountered an error.
     * @param e An Exception describing what went wrong.
     */
    public abstract void handleFailure(GPodderException e);
}
