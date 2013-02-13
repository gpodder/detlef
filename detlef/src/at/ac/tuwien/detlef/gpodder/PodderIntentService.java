package at.ac.tuwien.detlef.gpodder;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class PodderIntentService extends IntentService {

    private static final String TAG = PodderIntentService.class.getName();

    public static final String EXTRA_REQUEST = "EXTRA_REQUEST";
    public static final String EXTRA_RESULT_RECEIVER = "EXTRA_RESULT_RECEIVER";

    public static final int REQUEST_TOPLIST = 0;

    public PodderIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        int request = extras.getInt(EXTRA_REQUEST);
        ResultReceiver resultReceiver = extras.getParcelable(EXTRA_RESULT_RECEIVER);

        Log.d(TAG, String.format("Received request %d", request));

        resultReceiver.send(0, null);
    }


}
