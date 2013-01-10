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

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.Toast;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.activities.SettingsActivity;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.GpodderConnectionException;
import at.ac.tuwien.detlef.settings.GpodderSettings;

import com.jayway.android.robotium.solo.Solo;
/**
 * This tests the "Connection Test" button.
 * Because we use a modified {@link DependencyAssistant} this is in a separate file as the
 * {@link DependencyAssistant} must be passed in the {@link #setUp()} method.
 * 
 * @author moe
 */
public class SettingsGpodderNetConnectionTest extends
ActivityInstrumentationTestCase2<SettingsActivity> {

    private DependcyAssistantTestSettings depencyAssistant = new DependcyAssistantTestSettings();

    public enum ConnectionTestBehavior {CORRECT, INCORRECT, CONNECTIONERROR};

    private static final String TAG = "SettingsGpodderNetConnectionTest";

    /** Default sleep time between actions passed to {@link Solo#sleep(int)}. */
    private static final int DEFAULT_SLEEP_TIME = 300;

    /** Default time to wait for {@link Toast} messages */
    private static final int DEFAULT_TOAST_WAIT_TIME = 5000;

    private Solo solo;

    @Override
    public void setUp() throws Exception {


        resetSettings();

        DependencyAssistant.setDependencyAssistant(depencyAssistant);

        solo = new Solo(getInstrumentation(), getActivity());
        solo.setActivityOrientation(Solo.PORTRAIT);
        solo.sleep(DEFAULT_SLEEP_TIME);
        clickOnGpodderNetListEntry();
        solo.sleep(DEFAULT_SLEEP_TIME);

    }


    /**
     * Makes sure that the settings of the AVD are reset
     * to guarantee repeatable results.
     */
    private void resetSettings() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.clear();
        editor.commit();
    }

    private void clickOnGpodderNetListEntry() throws Exception {
        solo.clickInList(1);
        solo.sleep(DEFAULT_SLEEP_TIME);
    }

    public SettingsGpodderNetConnectionTest() {
        super(SettingsActivity.class);
    }

    public void testConnectIncorrectUsernamePassword() throws Exception {

        depencyAssistant.connectionTestBehavior = ConnectionTestBehavior.INCORRECT;

        solo.clickOnText(
                (String) getActivity().getText(R.string.test_connection)
                );

        Log.d(TAG, "waiting for toast...");

        assertTrue(
                String.format(
                        "Text %s should appear",
                        getActivity().getText(R.string.connectiontest_unsuccessful).toString()
                        ),
                        solo.waitForText(
                                getActivity().getText(R.string.connectiontest_unsuccessful).toString(),
                                1,
                                DEFAULT_TOAST_WAIT_TIME
                                )
                );

    }

    /**
     * Test the behavior if a connection could not be established.
     * @throws Exception
     */
    public void testConnectError() throws Exception {

        depencyAssistant.connectionTestBehavior = ConnectionTestBehavior.CONNECTIONERROR;

        solo.clickOnText(
                (String) getActivity().getText(R.string.test_connection)
                );

        Log.d(TAG, "waiting for toast...");

        assertTrue(
                String.format(
                        "Text %s should appear",
                        getActivity().getText(R.string.connectiontest_error).toString()
                        ),
                        solo.waitForText(
                                getActivity().getText(R.string.connectiontest_error).toString(),
                                1,
                                DEFAULT_TOAST_WAIT_TIME
                                )
                );

    }

    /**
     * Test the behavior if a connection could not be established.
     * @throws Exception
     */
    public void testConnectCorrectUsernamePassword() throws Exception {

        depencyAssistant.connectionTestBehavior = ConnectionTestBehavior.CORRECT;

        solo.clickOnText(
                (String) getActivity().getText(R.string.test_connection)
                );

        Log.d(TAG, "waiting for toast...");

        String expected = String.format(
                getActivity().getText(R.string.connectiontest_successful).toString(),
                depencyAssistant.getGpodderSettings(getActivity()).getUsername()
                );

        assertTrue(
                String.format("Text %s should appear", expected),
                solo.waitForText(expected, 1, DEFAULT_TOAST_WAIT_TIME)
                );

    }

    /**
     * This one is quite tricky:
     * If the connection test is still running and the screen is rotated (i.e. the
     * {@link SettingsActivity} is recreated, the background thread should post
     * the message to the new activity.
     */
    public void testRotateScreenWhileProgressBarIsShowing() {
        depencyAssistant.connectionTestSleepTime = 5000;
        depencyAssistant.connectionTestBehavior = ConnectionTestBehavior.INCORRECT;

        solo.clickOnText(
                (String) getActivity().getText(R.string.test_connection)
                );

        solo.sleep(500);

        solo.setActivityOrientation(Solo.LANDSCAPE);

        assertTrue(
                String.format(
                        "Text %s should appear",
                        getActivity().getText(R.string.connectiontest_unsuccessful).toString()
                        ),
                        solo.waitForText(
                                getActivity().getText(R.string.connectiontest_unsuccessful).toString(),
                                1,
                                5000
                                )
                );
    }

    @Override
    public void tearDown() throws Exception {
        // reset dependency assistant so that other tests are not affected
        DependencyAssistant.setDependencyAssistant(new DependencyAssistant());
        solo.finishOpenedActivities();
    }

    /**
     * This is a modified {@link DependencyAssistant} which adjusts these two aspects:
     * <ul>
     *   <li>{@link DependencyAssistant#getConnectionTester()}: Instead of a real connection test
     *   	this is replaced by a mock connection test, whose outcome can be defined via
     *   	{@link DependcyAssistantTestSettings#connectionTestBehavior}. So the UI functionality
     *   	can be tested without having to make a real connection.
     *   </li>
     *   <li>
     *   	{link {@link DependencyAssistant#getGpodderSettings(Context)}: This returns a fixed
     *   	user name and password combination, so the test can run faster because we don't have
     *   	to enter the username and password first in order to activate the connection test
     *   	button.
     *   </li>
     * </ul>
     * 
     * @author moe
     */
    public class DependcyAssistantTestSettings extends DependencyAssistant {

        public ConnectionTestBehavior connectionTestBehavior = ConnectionTestBehavior.CORRECT;

        /** The time in ms the connection tester will wait before the result is returned. */
        public int connectionTestSleepTime = 10;

        @Override
        public ConnectionTester getConnectionTester() {
            return new ConnectionTester() {

                @Override
                public boolean testConnection(GpodderSettings pSettings)
                        throws GpodderConnectionException, InterruptedException {

                    solo.sleep(connectionTestSleepTime);

                    switch (connectionTestBehavior) {
                        case CORRECT:
                            return true;
                        case INCORRECT:
                            return false;
                        case CONNECTIONERROR:
                        default:
                            throw new GpodderConnectionException();
                    }
                }
            };
        }

        @Override
        public GpodderSettings getGpodderSettings(Context context) {

            return new GpodderSettings() {

                @Override
                public String getUsername() {
                    return "username";
                }

                @Override
                public String getPassword() {
                    return "password";
                }

                @Override
                public String getDevicename() {
                    return "username-android";
                }

                @Override
                public long getLastUpdate() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                @Override
                public GpodderSettings setLastUpdate(long timestamp) {
                    // TODO Auto-generated method stub
                    return this;
                }
            };
        }

    }

}
