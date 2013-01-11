package at.ac.tuwien.detlef.fragments;

import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.settings.GpodderSettings;

class MockGpodderSettings extends GpodderSettings {
    @Override
    public String getUsername() {
        return "username";
    }

    @Override
    public String getPassword() {
        return "password";
    }

    @Override
    public String getDevicename() {
        return "username-android";
    }
    
    public DeviceId getDeviceId() {
        return new DeviceId("abcde");
    }

    @Override
    public long getLastUpdate() {
        return 0;
    }

    @Override
    public GpodderSettings setLastUpdate(long timestamp) {
        return this;
    }
}
