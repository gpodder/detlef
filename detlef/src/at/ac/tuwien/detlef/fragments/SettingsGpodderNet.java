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



package at.ac.tuwien.detlef.fragments;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;
import at.ac.tuwien.detlef.Detlef;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.Singletons;
import at.ac.tuwien.detlef.activities.MainActivity;
import at.ac.tuwien.detlef.activities.SettingsActivity;
import at.ac.tuwien.detlef.domain.DeviceId;
import at.ac.tuwien.detlef.fragments.callbacks.SettingsRegisterOnPreferenceClickListener;
import at.ac.tuwien.detlef.fragments.callbacks.SettingsUsernameOnPreferenceChangeListener;
import at.ac.tuwien.detlef.fragments.callbacks.SettingsUsernameOnPreferenceClickListener;
import at.ac.tuwien.detlef.gpodder.ErrorCode;
import at.ac.tuwien.detlef.gpodder.GPodderSync;
import at.ac.tuwien.detlef.gpodder.PodderIntentService;
import at.ac.tuwien.detlef.gpodder.events.AuthCheckResultEvent;
import at.ac.tuwien.detlef.gpodder.events.ConnectionErrorEvent;
import at.ac.tuwien.detlef.gpodder.events.RegisterDeviceResultEvent;
import at.ac.tuwien.detlef.gpodder.plumbing.GpoNetClientInfo;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.util.GUIUtils;
import de.greenrobot.event.EventBus;

/**
 * This fragment contains the UI logic for the gpodder.net account preferences
 * screen.
 *
 * <p>There are two different states in which this fragment can run:</p>
 *
 * <ul>
 *     <li>"set up mode": This will return to the {@link MainActivity} after the
 *     "register device id" button has been pressed where then the podcast and episode
 *     list is refreshed. This mode is started by passing an {@link Intent} with a boolean
 *     extra that has the key {@value #EXTRA_SETUPMODE} and is set to <code>true</code>.
 *     </li>
 *     <li>"default mode": If no intent is passed or if the boolean extra with the key
 *         {@value #EXTRA_SETUPMODE} is <code>false</code>.
 *     </li>
 * </ul>
 *
 * @author moe
 */
public class SettingsGpodderNet extends PreferenceFragment {

    /**
     * The key for the status variable that holds the property of the
     * ProgressDialog's state.
     */
    private static final String STATEVAR_PROGRESSDIALOG = "ProgressDialogShowing";

    private static final String STATEVAR_REGISTER_PROGRESSDIALOG = "RegisterProgressDialogShowing";

    /**
     * Holds a static reference to all {@link Toast} messages emitted by this
     * fragment so that the can be canceled at any time.
     */
    private static Toast toast;

    /**
     * ExecutorService for asynchronous tasks.
     */
    private static ExecutorService executorService = null;

    private static ProgressDialog checkUserCredentialsProgress;

    private static ProgressDialog registerDeviceProgress;

    /**
     * A tag for the LogCat so everything this class produces can be filtered.
     */
    private static final String TAG = SettingsGpodderNet.class.getCanonicalName();

    /**
     * The name for the extra which controls the behavior of the settings activity.
     * If a {@link Bundle} with this extra exists and it is set to <code>true</code> then
     * the {@link SettingsActivity} will run in a "set up" mode. This mode guides the user
     * through the initial steps that are needed in order to run {@link Detlef}.
     */
    public static final String EXTRA_SETUPMODE = "setupmode";

    private boolean setupMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }


        if (getActivity().getIntent().getBooleanExtra(EXTRA_SETUPMODE, false)) {
            setupMode = true;
        }

        restoreState(savedInstanceState);

        toast = Toast.makeText(getActivity(), "", 0);

        addPreferencesFromResource(R.xml.preferences_gpoddernet);


        setUpUsernameField();
        setUpPasswordField();
        setUpDeviceNameButton();
        setUpTestConnectionButton();
        setUpRegisterDeviceButton();
        setUpRegisterOnGpodderNetButton();
        loadSummaries();
    }

    private void setUpRegisterOnGpodderNetButton() {
        findPreference("button_register_account").setOnPreferenceClickListener(
            new SettingsRegisterOnPreferenceClickListener(this)
        );
    }

    @Override
    public void onDestroy() {
        dismissDialog();
        dismissRegisterDeviceDialog();

        super.onDestroy();
    }

    private void restoreState(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            return;
        }

        /* Don't restore the dialog if we lost the callback for some reason; */
        if (savedInstanceState.getBoolean(STATEVAR_PROGRESSDIALOG)) {
            showProgressDialog();
        }

        if (savedInstanceState.getBoolean(STATEVAR_REGISTER_PROGRESSDIALOG)) {
            showRegisterDeviceProgressDialog();
        }
    }

    private void setUpDeviceNameButton() {
        findPreference("devicename").setOnPreferenceChangeListener(
        new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        }
        );
    }

    private void setUpPasswordField() {
        findPreference("password").setOnPreferenceChangeListener(
        new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(maskPassword((String) newValue));
                Singletons.i().getGpodderSettingsDAO() .writeSettings(
                    getSettings().setPassword((String) newValue));
                setUpTestConnectionButton();
                return true;
            }
        }
        );
    }

    /**
     * Sets the behavior for the user name field. The user name field should
     * show the current user name as summary and should update the device name
     * input field and the state of the "Test Connection" button.
     */
    private void setUpUsernameField() {

        findPreference("username").setOnPreferenceClickListener(
            new SettingsUsernameOnPreferenceClickListener(this)
        );

        findPreference("username").setOnPreferenceChangeListener(
            new SettingsUsernameOnPreferenceChangeListener(this)
        );
    }

    /**
     * Loads the summary texts. This will be the texts entered at the
     * corresponding field.
     */
    private void loadSummaries() {
        Preference username = findPreference("username");
        username.setSummary(getSettings().getUsername());
        Preference password = findPreference("password");
        password.setSummary(maskPassword(getSettings().getPassword()));
        Preference devicename = findPreference("devicename");
        devicename.setSummary(getSettings().getDevicename());
    }

    private GpodderSettings getSettings() {
        return Singletons.i().getGpodderSettings();
    }

    public void setUpTestConnectionButton() {
        Preference button = findPreference("button_test_connect");

        if (getSettings().isAccountVerified()) {
            button.setEnabled(false);
            button.setSummary(R.string.account_is_verified);
            return;
        }

        if (getSettings().getUsername().isEmpty() || getSettings().getPassword().isEmpty()) {
            button.setEnabled(false);
            button.setSummary(R.string.enter_username_password_first);
            return;
        }

        button.setEnabled(true);
        button.setSummary(getText(R.string.tap_here_to_test_connection));

        button.setOnPreferenceClickListener(new TestConnectionButtonPreferenceListener());
    }

    public void setUpRegisterDeviceButton() {

        Preference button = findPreference("button_register_device");

        if (getSettings().getDeviceId() != null) {
            button.setEnabled(false);
            button.setSummary(
                String.format(
                    getText(R.string.device_registered_with_id).toString(),
                    getSettings().getDeviceId()
                )
            );
            return;
        }

        if (!getSettings().isAccountVerified()) {
            button.setEnabled(false);
            button.setSummary(R.string.verify_settings_first);
            return;
        }

        button.setEnabled(true);
        button.setSummary(getText(R.string.register_device_summary));

        button.setOnPreferenceClickListener(new RegisterDeviceIdPreferenceListener());
    }

    /**
     * Called if the provided settings are valid, i.e. the username/password
     * combination is recognized as valid account.
     * @param settings
     */
    private void connectionIsValid() {
        final GpodderSettings settings = Singletons.i().getGpodderSettings();

        Singletons.i().getGpodderSettingsDAO().writeSettings(settings.setAccountVerified(true));

        enableNextStepButton();

        GUIUtils.showToast(
                String.format(getText(R.string.connectiontest_successful).toString(),
                        settings.getUsername()), getActivity(), TAG);

        dismissDialog();
        setUpTestConnectionButton();
        setUpRegisterDeviceButton();

    }

    /**
     * Called if the provided settings are not valid.
     */
    private void connectionIsNotValid() {
        GUIUtils.showToast(getText(R.string.connectiontest_unsuccessful), getActivity(), TAG);
        dismissDialog();
    }

    /**
     * Called if an error occurs while connecting.
     */
    private void connectionFailed() {
        GUIUtils.showToast(getText(R.string.connectiontest_error), getActivity(), TAG);
        dismissDialog();
    }

    public void onEventMainThread(AuthCheckResultEvent event) {
        connectionIsValid();
    }

    public void onEventMainThread(RegisterDeviceResultEvent event) {
        if (ErrorCode.failed(event.code)) {
            dismissRegisterDeviceDialog();
            GUIUtils.showToast(getText(R.string.operation_failed), getActivity(), TAG);
            return;
        }

        GpodderSettings settings = getSettings();
        settings.setDeviceId(new DeviceId(event.id));
        Singletons.i().getGpodderSettingsDAO().writeSettings(settings);

        dismissRegisterDeviceDialog();
        setUpTestConnectionButton();
        setUpRegisterDeviceButton();

        if (isSetupMode()) {
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
        GUIUtils.showToast(
            getText(R.string.device_id_registration_success),
            getActivity(),
            TAG
        );
    }

    /**
     * The actions that should be executed if {@link SettingsGpodderNet} is in
     * {@link SettingsGpodderNet#isSetupMode() setup mode}.
     */
    private void setupModeAction() {
        GUIUtils.showSimpleOkDialog(
            R.string.almost_done,
            R.string.detlef_will_now_synchronize,
            new SetupModeNextStepClickListener(),
            getActivity()
        );
    }

    private class SetupModeNextStepClickListener implements OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {

            Intent data = new Intent().putExtra(
                MainActivity.EXTRA_REFRESH_FEED_LIST,
                true
            );
            if (getActivity().getParent() == null) {
                getActivity().setResult(Activity.RESULT_OK, data);
            } else {
                getActivity().getParent().setResult(Activity.RESULT_OK, data);
            }

            getActivity().finish();

        }
    }

    public void onEventMainThread(ConnectionErrorEvent event) {
        switch (event.code) {
            case ErrorCode.AUTHENTICATION_FAILED:
                connectionFailed();
                break;
            default:
                connectionIsNotValid();
                break;
        }
    }

    /**
     * Defines behavior of the "Test Connection" button. Basically it opens a
     * {@link ProgressDialog}, calls a connection test method in a separate
     * thread and prints a {@link Toast} message with the result of the
     * operation.
     *
     * @author moe
     */
    public class TestConnectionButtonPreferenceListener implements OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference arg0) {

            showProgressDialog();
            toast.cancel();

            GPodderSync gps = Singletons.i().getGPodderSync();
            GpodderSettings settings = Singletons.i().getGpodderSettings();

            gps.setHostname(settings.getApiHostname());

            final GpoNetClientInfo clientInfo = gps.getClientInfo();
            final GpoNetClientInfo tempClientInfo = new GpoNetClientInfo();

            tempClientInfo.setHostname(clientInfo.getHostname());
            tempClientInfo.setUsername(settings.getUsername());
            tempClientInfo.setPassword(settings.getPassword());

            getActivity().startService(new Intent(getActivity(), PodderIntentService.class).putExtra(
                             PodderIntentService.EXTRA_REQUEST,
                             PodderIntentService.REQUEST_AUTH_CHECK).putExtra(
                                     PodderIntentService.EXTRA_CLIENT_INFO,
                                     tempClientInfo));

            return true;
        }
    }

    public void dismissDialog() {
        if ((checkUserCredentialsProgress != null)
                && (checkUserCredentialsProgress.isShowing())
           ) {
            checkUserCredentialsProgress.dismiss();
        }
    }

    /**
     * A {@link OnPreferenceClickListener} that registers a new {@link DeviceId} at gpodder.net.
     * @author moe
     */
    public class RegisterDeviceIdPreferenceListener implements OnPreferenceClickListener {

        @Override
        public boolean onPreferenceClick(Preference arg0) {

            showRegisterDeviceProgressDialog();

            GpodderSettings settings = Singletons.i().getGpodderSettings();

            GpoNetClientInfo clientInfo = new GpoNetClientInfo();
            clientInfo.setHostname(settings.getApiHostname());
            clientInfo.setUsername(settings.getUsername());
            clientInfo.setPassword(settings.getPassword());

            getActivity().startService(new Intent(getActivity(), PodderIntentService.class)
                    .putExtra(
                            PodderIntentService.EXTRA_REQUEST,
                            PodderIntentService.REQUEST_REGISTER)
                    .putExtra(
                            PodderIntentService.EXTRA_CLIENT_INFO,
                            clientInfo)
                    .putExtra(
                            PodderIntentService.EXTRA_DEVICE_ID,
                            Singletons.i().getDeviceIdGenerator().generate().toString())
                    .putExtra(
                            PodderIntentService.EXTRA_DEVICE_NAME,
                            getSettings().getDevicename()));

            return true;
        }

    }

    /**
     * Masks a password with a masking character defined in the strings
     * resource.
     *
     * @param password The password. Does not necessary be the real password,
     *            but can be any String as only the length is of relevance.
     * @return The masked String, e.g. "12345" will return "*****" if the mask
     *         character is "*".
     */
    private String maskPassword(String password) {
        return new String(new char[password.length()]).replace(
                   "\0",
                   getText(R.string.password_mask_char)
               );
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        Log.d(TAG, "onSaveInstanceState()");
        savedInstanceState.putBoolean(
            STATEVAR_PROGRESSDIALOG,
            (checkUserCredentialsProgress != null)
            && (checkUserCredentialsProgress.isShowing())
        );
        savedInstanceState.putBoolean(
            STATEVAR_REGISTER_PROGRESSDIALOG,
            (registerDeviceProgress != null)
            && (registerDeviceProgress.isShowing())
        );
    }

    /**
     * Opens up the {@link ProgressDialog} that is shown while the user name and
     * password is checked against gpodder.net.
     */
    private void showProgressDialog() {
        checkUserCredentialsProgress = ProgressDialog.show(
                                           getActivity(),
                                           getString(R.string.checking_your_account_settings_title),
                                           getString(R.string.checking_your_account_settings_summary),
                                           true,
                                           true,
        new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, String.format("%s.onCancel(%s)", checkUserCredentialsProgress, dialog));

                /* TODO: Handle disconnection from backend service. */
            }
        }
                                       );

        Log.d(TAG, "Open Progressbar: " + checkUserCredentialsProgress);
    }

    /**
     * Opens up the {@link ProgressDialog} that is shown while the user name and
     * password is checked against gpodder.net.
     */
    private void showRegisterDeviceProgressDialog() {
        registerDeviceProgress = ProgressDialog.show(
                                     getActivity(),
                                     getString(R.string.register_device_title),
                                     getString(R.string.register_device_summary),
                                     true,
                                     true,
                                     null
                                 );
    }

    public void dismissRegisterDeviceDialog() {
        if ((registerDeviceProgress != null)
                && (registerDeviceProgress.isShowing())
           ) {
            registerDeviceProgress.dismiss();
        }
    }

    /**
     * Enables the "next step" button which is used during the set up mode.
     */
    public void enableNextStepButton() {
        getActivity().runOnUiThread(
        new Runnable() {
            @Override
            public void run() {
                if (findPreference("button_next_step") != null) {
                    findPreference("button_next_step").setEnabled(true);
                }
            }
        }
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this,
                AuthCheckResultEvent.class,
                ConnectionErrorEvent.class,
                RegisterDeviceResultEvent.class);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);

        super.onPause();
    }

    public boolean isSetupMode() {
        return setupMode;
    }


}
