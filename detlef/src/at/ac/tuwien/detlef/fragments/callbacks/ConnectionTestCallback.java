/* *************************************************************************
 *  Copyright 2012-2013 The detlef developers                              *
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

import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.fragments.SettingsGpodderNet;
import at.ac.tuwien.detlef.gpodder.NoDataResultHandler;
import at.ac.tuwien.detlef.gpodder.PodderService;
import at.ac.tuwien.detlef.gpodder.ReliableResultHandler;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.util.GUIUtils;

/**
 *
 */
public class ConnectionTestCallback
    extends ReliableResultHandler<SettingsGpodderNet>
    implements NoDataResultHandler<SettingsGpodderNet> {

    /**
     * Called if the provided settings are valid, i.e. the username/password
     * combination is recognized as valid account.
     * @param settings
     */
    public void connectionIsValid() {

        final GpodderSettings settings = Singletons.i()
                                         .getGpodderSettings();

        Singletons.i()
        .getGpodderSettingsDAO(getRcv().getActivity())
        .writeSettings(settings.setAccountVerified(true));

        getRcv().enableNextStepButton();

        getRcv().getActivity().runOnUiThread(
        new Runnable() {
            @Override
            public void run() {
                showToast(
                    String.format(
                        getRcv().getText(R.string.connectiontest_successful)
                        .toString(),
                        settings.getUsername()
                    )
                );

                getRcv().dismissDialog();
                getRcv().setUpTestConnectionButton();
                getRcv().setUpRegisterDeviceButton();
            }
        }
        );

    }

    /**
     * Called if the provided settings are not valid.
     */
    public void connectionIsNotValid() {
        getRcv().getActivity().runOnUiThread(
        new Runnable() {
            @Override
            public void run() {
                showToast(R.string.connectiontest_unsuccessful);
                getRcv().dismissDialog();
            }
        });
    }

    /**
     * Called if an error occurs while connecting.
     */
    public void connectionFailed() {
        getRcv().getActivity().runOnUiThread(
        new Runnable() {
            @Override
            public void run() {
                showToast(R.string.connectiontest_error);
                getRcv().dismissDialog();
            }
        });
    }

    private void showToast(int message) {
        showToast(getRcv().getText(message));
    }

    private void showToast(CharSequence message) {
        GUIUtils guiUtils = Singletons.i().getGuiUtils();

        guiUtils.showToast(
            message,
            getRcv().getActivity(),
            ""
        );

    }

    @Override
    public void handleFailure(int errCode, String errStr) {
        switch (errCode) {
        case PodderService.ErrorCode.AUTHENTICATION_FAILED:
            connectionIsNotValid();
            break;
        default:
            connectionFailed();
        }
    }

    @Override
    public void handleSuccess() {
        connectionIsValid();
    }
}
