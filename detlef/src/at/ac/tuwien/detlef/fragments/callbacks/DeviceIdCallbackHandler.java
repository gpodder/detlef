/* *************************************************************************
 *  Copyright 2012 The detlef developers                                   *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 2 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 ************************************************************************* */

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
import at.ac.tuwien.detlef.gpodder.DeviceIdResultHandler;
import at.ac.tuwien.detlef.gpodder.ReliableResultHandler;
import at.ac.tuwien.detlef.settings.GpodderSettings;

/**
 * The Handler that is called after a {@link DeviceId} has successfully been
 *  registered at gpodder.net.
 *
 * @author moe
 */
public class DeviceIdCallbackHandler
extends ReliableResultHandler<SettingsGpodderNet>
implements DeviceIdResultHandler<SettingsGpodderNet> {

    /** Logging tag. */
    private static final String TAG = DeviceIdCallbackHandler.class.getCanonicalName();

    @Override
    public void handleSuccess(DeviceId deviceId) {


        GpodderSettings settings = DependencyAssistant
                .getDependencyAssistant()
                .getGpodderSettings(getRcv().getActivity());
        settings.setDeviceId(deviceId);
        DependencyAssistant.getDependencyAssistant()
        .getGpodderSettingsDAO(getRcv().getActivity())
        .writeSettings(settings);

        getRcv().getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        getRcv().dismissRegisterDeviceDialog();
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
    public void handleFailure(int errCode, String errString) {
        getRcv().getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        getRcv().dismissRegisterDeviceDialog();
                    }
                }
                );

        DependencyAssistant.getDependencyAssistant().getGuiUtils().showToast(
                getRcv().getText(R.string.operation_failed),
                getRcv().getActivity(),
                TAG
                );
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