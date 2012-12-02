
package at.ac.tuwien.detlef.gpodder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothClass.Device;
import android.util.Log;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.callbacks.Callback;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.domain.EnhancedSubscriptionChanges;
import at.ac.tuwien.detlef.settings.DeviceRegistratorException;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.dragontek.mygpoclient.api.MygPodderClient;
import com.dragontek.mygpoclient.api.SubscriptionChanges;
import com.dragontek.mygpoclient.pub.PublicClient;
import com.dragontek.mygpoclient.simple.IPodcast;

/**
 * Background task that registers a {@link DeviceId} at gpodder.net.
 */
public class RegisterDeviceIdAsyncTask implements Runnable {
    /** Logging tag. */
    private static final String TAG = RegisterDeviceIdAsyncTask.class.getCanonicalName();

    private final RegisterDeviceIdResultHandler<? extends Fragment> callback;

    private DeviceId deviceId;

    public RegisterDeviceIdAsyncTask(RegisterDeviceIdResultHandler<? extends Fragment> callback, DeviceId pDeviceId) {
        deviceId = pDeviceId;
        this.callback = callback;
    }

    @Override
    public void run() {

        Log.d(TAG, "RegisterDeviceIdAsyncTask.run(): Start");

        try {
            Log.d(TAG, "Registering new Device with id " + deviceId);
            DependencyAssistant.getDependencyAssistant().getDeviceRegistrator().registerNewDeviceId(deviceId);
            Log.d(TAG, "Registering new Device: Done");
        } catch (DeviceRegistratorException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.d(TAG, "RegisterDeviceIdAsyncTask.run(): Done");

        /* Send the result. */
        callback.handle();
    }

}
