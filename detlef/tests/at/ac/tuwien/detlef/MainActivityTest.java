package at.ac.tuwien.detlef;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

public class MainActivityTest extends
	ActivityInstrumentationTestCase2<MainActivity> {

	private Solo solo;
	
	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public MainActivityTest() {
		super(MainActivity.class);
	}

	/**
	 * Makes sure that a menu item called "All Podcasts" appears
	 * on the first page.
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
