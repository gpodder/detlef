
package at.ac.tuwien.detlef.gpodder;

import android.app.Fragment;
import android.util.Log;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.settings.DeviceRegistratorException;

/**
 * Background task that registers a {@link DeviceId} at gpodder.net.
 */
public class RegisterDeviceIdAsyncTask implements Runnable {
    /** Logging tag. */
    private static final String TAG = RegisterDeviceIdAsyncTask.class.getCanonicalName();

    private final RegisterDeviceIdResultHandler<? extends Fragment> callback;

    private DeviceId deviceId;

    public RegisterDeviceIdAsyncTask(RegisterDeviceIdResultHandler<? extends Fragment> callback,
        DeviceId pDeviceId) {
        deviceId = pDeviceId;
        callback.setDeviceId(pDeviceId);
        this.callback = callback;
    }

    @Override
    public void run() {

        Log.d(TAG, "RegisterDeviceIdAsyncTask.run(): Start");

        try {
            Log.d(TAG, "Registering new Device with id " + deviceId);
            DependencyAssistant.getDependencyAssistant()
                .getDeviceRegistrator()
                .registerNewDeviceId(deviceId);
            Log.d(TAG, "Registering new Device: Done");
        } catch (DeviceRegistratorException e) {
            Log.e(TAG, "DeviceRegistratorException", e);
        }

        Log.d(TAG, "RegisterDeviceIdAsyncTask.run(): Done");

        /* Send the result. */
        callback.handle();
    }

}
