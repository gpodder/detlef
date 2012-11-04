package at.ac.tuwien.detlef.fragments;

import java.util.UUID;

import com.jayway.android.robotium.solo.Solo;

import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.activities.SettingsActivity;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.Gpodder;
import at.ac.tuwien.detlef.settings.GpodderConnectionException;

public class SettingsGpodderNetTest extends
ActivityInstrumentationTestCase2<SettingsActivity> {

	private Solo solo;

    @Override
    public void setUp() throws Exception {
	resetSettings();
        solo = new Solo(getInstrumentation(), getActivity());
        clickOnGpodderNetListEntry();
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

	private void clickOnGpodderNetListEntry() {
		solo.clickInList(1);
	}

	public SettingsGpodderNetTest() {
		super(SettingsActivity.class);
	}

    /**
     * If a new user name is entered, then the user name should appear
     * as summary on the preferences page.
     * @throws Exception
     */
    public void testChangeUsernameUpdatesSummary() throws Exception {

	String newUsername =  UUID.randomUUID().toString();
	enterUsername(newUsername);

	assertTrue(solo.searchText(newUsername));
    }

    /**
     * If a new user name is entered then the device name should be
     * updated accordingly.
     * @throws Exception
     */
    public void testChangeUsernameUpdatesDevicename() throws Exception {

	String newUsername =  UUID.randomUUID().toString();
	enterUsername(newUsername);

	// user name should appear as summary
	assertTrue(solo.searchText(String.format("%s-android", newUsername)));
    }

    /**
     * If a new user name is entered then the device name should NOT be
     * updated if a custom device name has been entered before.
     * @throws Exception
     */
    public void testChangeUsernameDoesNotUpdatesCustomDevicename() throws Exception {

	String newDeviceName =  UUID.randomUUID().toString();
	String newUsername =  UUID.randomUUID().toString();

	enterDevicename(newDeviceName);
	enterUsername(newUsername);

	// previously entered device name should still appear on settings page
	assertTrue(solo.searchText(newDeviceName));
    }

    private void enterUsername(String username) {
	solo.clickInList(1);
	solo.enterText(0, username);
	solo.clickOnButton("OK");
    }

    private void enterDevicename(String devicename) {
	solo.clickInList(3);
	solo.enterText(0, devicename);
	solo.clickOnButton("OK");
    }



    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
