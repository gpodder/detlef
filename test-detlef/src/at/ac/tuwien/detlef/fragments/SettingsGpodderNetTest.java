package at.ac.tuwien.detlef.fragments;

import java.util.HashMap;
import java.util.UUID;

import com.jayway.android.robotium.solo.Solo;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import at.ac.tuwien.detlef.DependencyAssistant;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.activities.SettingsActivity;
import at.ac.tuwien.detlef.settings.ConnectionTester;
import at.ac.tuwien.detlef.settings.GpodderConnectionException;
import at.ac.tuwien.detlef.settings.GpodderSettings;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAO;
import at.ac.tuwien.detlef.settings.GpodderSettingsDAOAndroid;

public class SettingsGpodderNetTest extends
ActivityInstrumentationTestCase2<SettingsActivity> {

	private Solo solo;

    @Override
    public void setUp() throws Exception {
    	resetSettings();
        solo = new Solo(getInstrumentation(), getActivity());
        delay();
        clickOnGpodderNetListEntry();
        delay();
    }

    private void delay() {
    	try {
    		Thread.sleep(300);
    	} catch (InterruptedException e) {
    	}
	}
    
	private void delay(int times) {
		for (int i = 0; i < times; i++) {
			delay();
		}
		
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
		delay();
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
    
    public void testCorrectUsernamePassword() throws Exception {
    	
    	DependencyAssistant.DEPENDENCY_ASSISTANT = new DependencyAssistant() {
    		public ConnectionTester getConnectionTester() {
    			return new ConnectionTester() {
    				public boolean testConnection(GpodderSettings pSettings)
    						throws GpodderConnectionException, InterruptedException {
    					delay(4);
    					return true;
    				}
    			};
    		}
    		
    	    public GpodderSettings getGpodderSettings(Context context) {
    	    	
    	    	return new GpodderSettings() {
					
					@Override
					public boolean isDefaultDevicename() {
						return true;
					}
					
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
				};
    	    }
    		
    	};
    	
    	solo.clickInList(4);
   	
    	assertTrue(
    		String.format("Text %s should appear", getActivity().getText(R.string.connectiontest_successful)).toString(),
    		solo.waitForText(getActivity().getText(R.string.connectiontest_successful).toString())
    	);
    	
    	
    }
    
    private void enterPassword(String password) {
		solo.clickInList(2);
		delay();
		solo.enterText(0, password);
		delay();
		solo.clickOnButton("OK");
		delay();
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
		delay();
		enterUsername(newUsername);
		// previously entered device name should still appear on settings page
		delay();
		assertTrue(solo.searchText(newDeviceName));
    }

    private void enterUsername(String username) throws InterruptedException {
		solo.clickInList(1);
		delay();
		solo.enterText(0, username);
		delay();
		solo.clickOnButton("OK");
		delay();
    }

    private void enterDevicename(String devicename) throws InterruptedException {
		solo.clickInList(3);
		delay();
		solo.enterText(0, devicename);
		delay();
		solo.clickOnButton("OK");
		delay();
    }



    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
