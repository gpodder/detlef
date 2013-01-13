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

package at.ac.tuwien.detlef.gpodder;

import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.fragments.SettingsGpodderNet;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.util.GUIUtils;

public class ConnectionTestActivity extends ConnectionTestCallback<SettingsGpodderNet> {

    @Override
    public void connectionIsValid(GpodderSettings settings) {
        
        showToast(
            String.format(
                getRcv().getText(R.string.connectiontest_successful).toString(),
                settings.getUsername()
            )
        );
        
        getRcv().dismissDialog();
        
        DependencyAssistant.getDependencyAssistant()
            .getGpodderSettingsDAO(getRcv().getActivity())
            .writeSettings(settings.setAccountVerified(true));
    
        getRcv().enableNextStepButton();
        
        getRcv().getActivity().runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    getRcv().setUpTestConnectionButton();
                    getRcv().setUpRegisterDeviceButton();
                }
            }
        );

    }

    @Override
    public void connectionIsNotValid() {
        showToast(R.string.connectiontest_unsuccessful);
        getRcv().dismissDialog();
    }

    @Override
    public void connectionFailed() {
        showToast(R.string.connectiontest_error);
        getRcv().dismissDialog();
    }
    
    private void showToast(int message) {
        showToast(getRcv().getText(message));
    }
    
    private void showToast(CharSequence message) {
        GUIUtils guiUtils = DependencyAssistant.getDependencyAssistant().getGuiUtils();
        
        guiUtils.showToast(
            message,
            getRcv().getActivity(),
            ""
        );
        
    }

}
