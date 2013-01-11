package at.ac.tuwien.detlef.fragments;

import android.content.Context;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.settings.GpodderSettings;

public class MockDependencyAssistant extends DependencyAssistant {
    public GpodderSettings getGpodderSettings(Context context) {
        return new MockGpodderSettings();
    }
}
