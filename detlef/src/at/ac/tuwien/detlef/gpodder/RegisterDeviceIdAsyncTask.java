
package at.ac.tuwien.detlef.gpodder;

import android.util.Log;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.settings.DeviceRegistratorException;

/**
 * Background task that registers a {@link DeviceId} at gpodder.net.
 */
public class RegisterDeviceIdAsyncTask implements Runnable {
    /** Logging tag. */
    private static final String TAG = RegisterDeviceIdAsyncTask.class.getCanonicalName();

    private final DeviceIdResultHandler<?> callback;

    private final DeviceId deviceId;

    public RegisterDeviceIdAsyncTask(DeviceIdResultHandler<?> callback,
                                     DeviceId pDeviceId) {
        deviceId = pDeviceId;
        this.callback = callback;
    }

    @Override
    public void run() {

        Log.d(TAG, "RegisterDeviceIdAsyncTask.run(): Start");

        try {
            Log.d(TAG, "Registering new Device with id " + deviceId);
            Singletons.i()
            .getDeviceRegistrator()
            .registerNewDeviceId(deviceId);
            Log.d(TAG, "Registering new Device: Done");
        } catch (DeviceRegistratorException e) {
            Log.e(TAG, "DeviceRegistratorException", e);
            callback.handleFailure(-1, e.getLocalizedMessage());
        }

        Log.d(TAG, "RegisterDeviceIdAsyncTask.run(): Done");

        callback.handleSuccess(deviceId);
    }

}
