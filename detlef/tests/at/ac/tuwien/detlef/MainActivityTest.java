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

    /**
     * Makes sure that a menu item called "All Podcasts" appears on the first
     * page.
     * 
     * @throws Exception
     */
    public void testAllPodcatsOnFirstPage() throws Exception {
        assertTrue(solo.searchText("All Podcasts"));
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

}
