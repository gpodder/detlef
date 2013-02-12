package at.ac.tuwien.detlef.fragments;

import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.settings.GpodderSettings;

public class MockDependencyAssistant extends Singletons {
    @Override
    public GpodderSettings getGpodderSettings() {
        return new MockGpodderSettings();
    }
}
