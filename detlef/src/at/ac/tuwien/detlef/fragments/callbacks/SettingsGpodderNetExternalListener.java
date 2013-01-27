package at.ac.tuwien.detlef.fragments.callbacks;

import android.preference.Preference.OnPreferenceClickListener;
import at.ac.tuwien.detlef.fragments.SettingsGpodderNet;

/**
 * An external listener (in the sense of a listener that is not contained
 * in an enclosing type) needs a reference to the {@link SettingsGpodderNet}.
 *
 * <p>As this functionality is needed in all external listeners, this has been
 * factored into an abstract class. This common functionality can be used
 * both for {@link OnPreferenceClickListener} and
 * {@link OnPreferenceClickListener}, therefore the Interface has to be added
 * in the final class.</p>
 *
 * @author moe
 *
 */
public abstract class SettingsGpodderNetExternalListener {

    private final SettingsGpodderNet sender;

    public SettingsGpodderNetExternalListener(SettingsGpodderNet pSender) {
        sender = pSender;
    }

    public SettingsGpodderNet getSender() {
        return sender;
    }

}
