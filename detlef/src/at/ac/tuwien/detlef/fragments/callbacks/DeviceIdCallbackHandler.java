package at.ac.tuwien.detlef.fragments.callbacks;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.activities.MainActivity;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.fragments.SettingsGpodderNet;
import at.ac.tuwien.detlef.gpodder.GPodderException;
import at.ac.tuwien.detlef.gpodder.RegisterDeviceIdResultHandler;
import at.ac.tuwien.detlef.settings.GpodderSettings;

/**
 * The Handler that is called after a {@link DeviceId} has successfully been registered at
 * gpodder.net.
 * 
 * <p></p>
 * 
 * @author moe
 */
public class DeviceIdCallbackHandler
    extends RegisterDeviceIdResultHandler<SettingsGpodderNet> {

    /** Logging tag. */
    private static final String TAG = DeviceIdCallbackHandler.class.getCanonicalName();

    @Override
    public void handle() {
    
    
        GpodderSettings settings = DependencyAssistant
            .getDependencyAssistant()
            .getGpodderSettings(getRcv().getActivity());
        settings.setDeviceId(getDeviceId());
        DependencyAssistant.getDependencyAssistant()
            .getGpodderSettingsDAO(getRcv().getActivity())
            .writeSettings(settings);
    
        getRcv().dismissRegisterDeviceDialog();
        
        getRcv().getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        getRcv().setUpTestConnectionButton();
                        getRcv().setUpRegisterDeviceButton();
                    }
                }
            );
        
        if (getRcv().isSetupMode()) {
            setupModeAction();
        } else {
            normalModeAction();
        }
    }

    /**
     * The actions that should be executed if {@link SettingsGpodderNet} is in "normal" mode,
     * i.e. not in {@link SettingsGpodderNet#isSetupMode() setup mode}.
     */
    private void normalModeAction() {
        DependencyAssistant.getDependencyAssistant().getGuiUtils().showToast(
                getRcv().getText(R.string.device_id_registration_success),
                getRcv().getActivity(),
                TAG
                );

    }

    /**
     * The actions that should be executed if {@link SettingsGpodderNet} is in
     * {@link SettingsGpodderNet#isSetupMode() setup mode}.
     */
    private void setupModeAction() {
        DependencyAssistant.getDependencyAssistant().getGuiUtils().showSimpleOkDialog(
                R.string.almost_done,
                R.string.detlef_will_now_synchronize,
                new SetupModeNextStepClickListener(),
                getRcv().getActivity()
                );
    }

    @Override
    public void handleFailure(GPodderException e) {
        // TODO Auto-generated method stub

    }

    class SetupModeNextStepClickListener implements OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {

            Intent data = new Intent().putExtra(
                    MainActivity.EXTRA_REFRESH_FEED_LIST,
                    true
                    );
            if (getRcv().getActivity().getParent() == null) {
                getRcv().getActivity().setResult(Activity.RESULT_OK, data);
            } else {
                getRcv().getActivity().getParent().setResult(Activity.RESULT_OK, data);
            }

            getRcv().getActivity().finish();

        }
    }

}