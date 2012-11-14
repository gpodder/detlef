package at.ac.tuwien.detlef.gpodder;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

/**
 * A class to handle replies from the PullFeedAsyncTask.
 *
 * The Receiver has to be registered with registerReceiver() before use and unregistered with
 * unregisterReceiver() once the Receiver is destroyed.
 *
 * In order to implement handling of the Task's results the user has to subclass
 * FeedSyncResultHandler and implement handle() and handleFailure().
 */
public abstract class FeedSyncResultHandler<Receiver extends Activity> extends
BroadcastReceiverCallback<Receiver> {
    /** Logging tag. */
    private static final String TAG = "FeedSyncResultHandler";

    protected FeedSyncResultHandler() {
        super(PullFeedAsyncTask.ACTION);
    }

    @Override
    protected void deliverEvent(BroadcastReceiverCallback.BroadcastReceiverEvent e) {
        Log.d(TAG, "deliverEvent");

        Intent intent = e.getIntent();

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
