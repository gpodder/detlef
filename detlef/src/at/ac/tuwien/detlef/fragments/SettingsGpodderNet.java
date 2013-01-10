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
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.callbacks.CallbackContainer;
import at.ac.tuwien.detlef.fragments.callbacks.DeviceIdCallbackHandler;
import at.ac.tuwien.detlef.fragments.callbacks.SettingsUsernameOnPreferenceChangeListener;
import at.ac.tuwien.detlef.fragments.callbacks.SettingsUsernameOnPreferenceClickListener;
import at.ac.tuwien.detlef.gpodder.RegisterDeviceIdAsyncTask;
import at.ac.tuwien.detlef.gpodder.RegisterDeviceIdResultHandler;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.GpodderConnectionException;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.util.GUIUtils;

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

    /**
     * Holds a static reference to all {@link Toast} messages emitted by this
     * fragment so that the can be canceled at any time.
     */
    private static Toast toast;

    /**
     * The Thread that executes the user credential check. It is static so it
     * can be accessed even if the Fragment gets recreated in the mean time.
     */
    private static Thread connectionTestThread;

    /**
     *
     */
    private static final ExecutorService REGISTER_DEVICE = Executors.newSingleThreadExecutor();

    private static ProgressDialog checkUserCredentialsProgress;

    private static ProgressDialog registerDeviceProgress;

    /**
     * This holds a static reference to the activity that currently is
     * associated with this fragment. The reason for this is that if a
     * {@link Thread} is started and the screen is rotated while the thread is
     * running, the method {@link #getActivity()} will return null. But as the
     * {@link Toast} needs to know the current {@link Context}, the activity
     * needs to be stored somewhere.
     */
    private static Activity activity;

    /**
     * A tag for the LogCat so everything this class produces can be filtered.
     */
    private static final String TAG = SettingsGpodderNet.class.getCanonicalName();

    private GUIUtils guiUtils;

    /**
     * The name for the extra which controls the behavior of the settings activity.
     * If a {@link Bundle} with this extra exists and it is set to <code>true</code> then
     * the {@link SettingsActivity} will run in a "set up" mode. This mode guides the user
     * through the initial steps that are needed in order to run {@link Detlef}.
     */
    public static final String EXTRA_SETUPMODE = "setupmode";



    private boolean setupMode = false;

    /**
     * All callbacks this Activity receives are stored here.
     *
     * This allows us to manage the Activity Lifecycle more easily.
     */
    private static final CallbackContainer<SettingsGpodderNet> CALLBACK_CONTAINER =
            new CallbackContainer<SettingsGpodderNet>();

    /**
     *
     */
    private static final String KEY_DEVICE_ID_HANDLER = "device_id_handler";

    /**
     * @return The {@link ConnectionTester} that can be used to determine if the
     *         user name and password are correct.
     */
    public ConnectionTester getConnectionTester() {
        return DependencyAssistant.getDependencyAssistant().getConnectionTester();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getActivity().getIntent().getBooleanExtra(EXTRA_SETUPMODE, false)) {
            setupMode = true;
        }

        guiUtils = DependencyAssistant.getDependencyAssistant().getGuiUtils();

        activity = getActivity();

        restoreState(savedInstanceState);

        toast = Toast.makeText(getActivity(), "", 0);

        Log.d(TAG, "Toast: " + toast);

        addPreferencesFromResource(R.xml.preferences_gpoddernet);


        setUpUsernameField();
        setUpPasswordField();
        setUpDeviceNameButton();
        setUpTestConnectionButton();
        setUpRegisterDeviceButton();
        
        loadSummaries();
        setUpRegisterDeviceIdCallback();
        

    }





    private void setUpRegisterDeviceIdCallback() {
        CALLBACK_CONTAINER.put(KEY_DEVICE_ID_HANDLER, new DeviceIdCallbackHandler());
    }

    private void restoreState(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            return;
        }

        if (savedInstanceState.getBoolean(STATEVAR_PROGRESSDIALOG)) {
            showProgressDialog();
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
                        DependencyAssistant.getDependencyAssistant()
                            .getGpodderSettingsDAO(getActivity())
                            .writeSettings(
                                    getSettings().setPassword((String) newValue)
                            );
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
            new SettingsUsernameOnPreferenceClickListener()
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
        
        Log.d(TAG, "getDevicename(): >" + getSettings().getDevicename() + "<");
        
        devicename.setSummary(getSettings().getDevicename());
    }

    private GpodderSettings getSettings() {
        return DependencyAssistant.getDependencyAssistant().getGpodderSettings(getActivity());
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

            connectionTestThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        if (getConnectionTester().testConnection(getSettings())) {
                            
                            guiUtils.showToast(
                                    String.format(
                                        activity.getText(R.string.connectiontest_successful)
                                            .toString(),
                                        getSettings().getUsername()
                                    ), activity, TAG);
                            
                            DependencyAssistant.getDependencyAssistant()
                                .getGpodderSettingsDAO(activity)
                                .writeSettings(getSettings().setAccountVerified(true));
                            
                            enableNextStepButton();
                            
                            getActivity().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        setUpTestConnectionButton();
                                        setUpRegisterDeviceButton();
                                    }
                                }
                            );

                        } else {
                            guiUtils.showToast(activity
                                    .getText(R.string.connectiontest_unsuccessful),
                                    activity, TAG);
                        }
                    } catch (GpodderConnectionException e) {
                        guiUtils.showToast(activity.getText(R.string.connectiontest_error),
                                activity, TAG);
                    } catch (InterruptedException e) {
                        return;
                    }

                    if ((checkUserCredentialsProgress != null)
                            && (checkUserCredentialsProgress.isShowing())
                    ) {
                        checkUserCredentialsProgress.dismiss();
                    }
                }


            });

            connectionTestThread.start();

            return true;
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

            REGISTER_DEVICE.execute(
                new RegisterDeviceIdAsyncTask(
                    (RegisterDeviceIdResultHandler<SettingsGpodderNet>) CALLBACK_CONTAINER.get(
                        KEY_DEVICE_ID_HANDLER
                    ),
                    DependencyAssistant.getDependencyAssistant().getDeviceIdGenerator().generate()
                )
            );

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
                        Log.d(
                                TAG,
                                String.format("%s.onCancel(%s)", checkUserCredentialsProgress,
                                        dialog)
                                );
                        if ((connectionTestThread != null) && connectionTestThread.isAlive()) {
                            Log.d(TAG, "interrupting thread.");
                            connectionTestThread.interrupt();
                        }
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
        registerDeviceProgress.dismiss();
    }

    /**
     * Enables the "next step" button which is used during the set up mode.
     */
    private void enableNextStepButton() {
        getActivity().runOnUiThread(
            new Runnable() {
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

        /* Register the Podcast- & FeedHandler. */
        CALLBACK_CONTAINER.registerReceiver(this);
    }

    @Override
    public void onPause() {
        /* Unregister the Podcast- & FeedHandler. */
        Log.d(TAG, "onPause()");
        CALLBACK_CONTAINER.unregisterReceiver();

        super.onPause();
    }

    public void onStop() {

        Log.d(TAG, "onStop()");
        CALLBACK_CONTAINER.clear();
        super.onStop();
    }

    public boolean isSetupMode() {
        return setupMode;
    }


}
