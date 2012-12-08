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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.activities.MainActivity;
import at.ac.tuwien.detlef.callbacks.CallbackContainer;
import at.ac.tuwien.detlef.gpodder.GPodderException;
import at.ac.tuwien.detlef.gpodder.RegisterDeviceIdAsyncTask;
import at.ac.tuwien.detlef.gpodder.RegisterDeviceIdResultHandler;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.GpodderConnectionException;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.util.GUIUtils;

/**
 * This fragment contains the UI logic for the gpodder.net account settings
 * screen. The user can enter these settings: - User name - Password - Device
 * name Additionally, there exists a button which checks the validity of the
 * entered data.
 * 
 * <p>This fragment may also run in a "set up mode": This will display an additional
 * button that registers the {@link DeviceId} against the gpodder.net API  and
 * refreshes the {@link MainActivity}'s list of Podcasts and Episodes.
 * </p>
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

        setUpTestConnectionButton();
        setUpNextStepButton();
        setUpUsernameField();
        setUpPasswordField();
        setUpDeviceNameButton();

        loadSummaries();
        setUpRegisterDeviceIdCallback();

    }



    private void setUpRegisterDeviceIdCallback() {
        
        final Activity act = getActivity();
        
        CALLBACK_CONTAINER.put(
                KEY_DEVICE_ID_HANDLER,
                new RegisterDeviceIdResultHandler<SettingsGpodderNet>() {

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

                    act.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            final AlertDialog.Builder b = new AlertDialog.Builder(act);
                            b.setTitle(R.string.almost_done);
                            b.setMessage(R.string.should_detlef_download_podcast_list);

                            b.setPositiveButton(
                                    android.R.string.ok, new OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            Intent data = new Intent().putExtra(
                                                MainActivity.EXTRA_REFRESH_FEED_LIST,
                                                true
                                            );
                                            if (act.getParent() == null) {
                                                act.setResult(Activity.RESULT_OK, data);
                                            } else {
                                                act.getParent().setResult(Activity.RESULT_OK, data);
                                           }

                                            act.finish();

                                        }
                                    });

                            b.setNegativeButton(
                                android.R.string.cancel,
                                new OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent data = new Intent().putExtra(
                                            MainActivity.EXTRA_REFRESH_FEED_LIST,
                                            true
                                        );
                                        if (act.getParent() == null) {
                                            act.setResult(Activity.RESULT_CANCELED, data);
                                        } else {
                                            act.getParent().setResult(
                                                Activity.RESULT_CANCELED,
                                                data
                                            );
                                       }

                                        act.finish();

                                    }
                                }
                            );

                            b.show();
                        }
                    });
                }

                @Override
                public void handleFailure(GPodderException e) {
                    // TODO Auto-generated method stub

                }
            });
        
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
                        updateTestConnectionButtonEnabledState(
                                getSettings().getUsername(),
                                (String) newValue
                        );
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
        findPreference("username").setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        preference.setSummary((String) newValue);

                        if (getSettings().isDefaultDevicename()) {
                            updateDeviceName((String) newValue);
                        }

                        updateTestConnectionButtonEnabledState(
                                (String) newValue,
                                getSettings().getPassword()
                        );

                        return true;
                    }

                    private void updateDeviceName(String username) {
                        Editor edit = PreferenceManager
                                .getDefaultSharedPreferences(getActivity())
                                .edit();

                        boolean writeDevicenameStatus = edit.putString(
                                "devicename",
                                String.format("%s-android", username)
                                ).commit();

                        if (writeDevicenameStatus) {
                            findPreference("devicename").setSummary(getSettings().getDevicename());
                        }
                    }
                }
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
        return DependencyAssistant.getDependencyAssistant().getGpodderSettings(getActivity());
    }

    private void setUpTestConnectionButton() {
        Preference button = findPreference("button_test_connect");
        button.setOnPreferenceClickListener(new TestConnectionButtonPreferenceListener());
        updateTestConnectionButtonEnabledState(getSettings());
    }

    private void setUpNextStepButton() {
        Preference button = findPreference("button_next_step");


        if (!setupMode) {
            getPreferenceScreen().removePreference(button);
            return;
        }

        button.setEnabled(false);
        button.setOnPreferenceClickListener(new NextStepButtonPreferenceListener());

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
                            enableButton();
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

    public class NextStepButtonPreferenceListener implements OnPreferenceClickListener {

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

    private void updateTestConnectionButtonEnabledState(GpodderSettings settings) {
        updateTestConnectionButtonEnabledState(settings.getUsername(), settings.getPassword());
    }

    /**
     * This updates the enabled state of the "Test Connection" button depending
     * on if the user name and password are both non-empty.
     *
     * @param username
     * @param password
     */
    private void updateTestConnectionButtonEnabledState(String username, String password) {
        Preference button = findPreference("button_test_connect");
        button.setEnabled((!username.isEmpty()) && (!password.isEmpty()));
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
                new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                       //TODO
                    }
                }
                );
    }

    public void dismissRegisterDeviceDialog() {
        registerDeviceProgress.dismiss();
    }

    private void enableButton() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (findPreference("button_next_step") != null) {
                    findPreference("button_next_step").setEnabled(true);
                }
           }
       });

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


}
