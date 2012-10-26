package at.ac.tuwien.detlef;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestRunner;

public class MainActivityTest extends
	ActivityInstrumentationTestCase2<MainActivity> {

	private Solo solo;
	
	private MainActivity ma = new MainActivity();
	
	private static Class<MainActivity> cma = MainActivity.class;

	public void setUp() throws Exception {
		InstrumentationTestRunner ru = new android.test.InstrumentationTestRunner();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public MainActivityTest() {
		super(cma);
	}

	public void test() {
		assertTrue(true);
	}
	
   @Override
   public void tearDown() throws Exception {
	   solo.finishOpenedActivities();
   }


}
