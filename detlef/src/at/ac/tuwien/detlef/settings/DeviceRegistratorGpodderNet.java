package at.ac.tuwien.detlef.settings;

import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.domain.DeviceId;

import com.dragontek.mygpoclient.api.MygPodderClient;

/**
 * A device registrator that facilitates gpodder.net to register the new device.
 *
 * @author moe
 *
 */
public class DeviceRegistratorGpodderNet
    implements DeviceRegistrator {

    @Override
    public DeviceRegistratorGpodderNet registerNewDeviceId(DeviceId deviceId)
    throws DeviceRegistratorException {

        try {
            GpodderSettings gpodderSettings = DependencyAssistant
                                              .getDependencyAssistant()
                                              .getGpodderSettings();

            MygPodderClient gpc = new MygPodderClient(
                gpodderSettings.getUsername(),
                gpodderSettings.getPassword(),
                gpodderSettings.getApiHostname()
            );

            gpc.updateDeviceSettings(
                deviceId.toString(),
                gpodderSettings.getDevicename(),
                "mobile"
            );

        } catch (Exception e) {
            throw new DeviceRegistratorException();
        }

        return this;
    }

}
