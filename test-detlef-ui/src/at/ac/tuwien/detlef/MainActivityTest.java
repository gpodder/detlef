package at.ac.tuwien.detlef;

import android.test.ActivityInstrumentationTestCase2;
import at.ac.tuwien.detlef.activities.MainActivity;

import com.jayway.android.robotium.solo.Solo;

public class MainActivityTest extends
        ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
    
    public void testBackButtonPress() {
        solo.clickOnText("PLAYER");
        solo.goBack();
        assertTrue("Episode tab should be displayed now!",solo.searchText("EPISODES"));
        solo.goBack();
        assertTrue("Podcasts tab should be displayed now!", solo.searchText("PODCASTS"));
    }

}
