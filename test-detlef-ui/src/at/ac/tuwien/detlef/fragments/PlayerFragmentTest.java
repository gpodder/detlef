package at.ac.tuwien.detlef.fragments;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.activities.MainActivity;

import com.jayway.android.robotium.solo.Solo;

public class PlayerFragmentTest extends
        ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private MainActivity activity;

    public PlayerFragmentTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() {
        activity = getActivity();
        solo = new Solo(getInstrumentation(), activity);
    }

    private void delay() {
        solo.sleep(2000);
    }

    /**
     * Upon adding a new episode to the DAO, it should be displayed in the
     * episode list.
     */
    public void testPlayPause() {
        Display d =
                ((WindowManager) activity.getApplication().getSystemService(
                        Context.WINDOW_SERVICE)).getDefaultDisplay();
        int screenWidth = d.getWidth();
        int screenHeight = d.getHeight();
        int fromX = (screenWidth / 2) + (screenWidth / 3);
        int toX = (screenWidth / 2) - (screenWidth / 3);
        int fromY = screenHeight / 2;
        int toY = screenHeight / 2;
        solo.drag(fromX, toX, fromY, toY, 1);
        solo.sleep(500);
        solo.drag(fromX, toX, fromY, toY, 1);
        solo.sleep(500);

        ImageButton imageButton =
                (ImageButton) solo.getView(R.id.ButtonPlayStop);
        solo.clickOnView(imageButton);
        SeekBar slider = (SeekBar) solo.getView(R.id.SeekBar01);

        delay();

        assertTrue("SeekBar should be at a position > 0",
                slider.getProgress() > 0);
        solo.clickOnView(imageButton);
    }

    /**
     * When switching fragments, the slider should not get confused and update
     * itself when the player comes to the foreground again.
     */
    public void testSwitchViews() {
        solo.clickOnText("PLAYER");
        ImageButton imageButton =
                (ImageButton) solo.getView(R.id.ButtonPlayStop);
        solo.clickOnView(imageButton);
        SeekBar slider = (SeekBar) solo.getView(R.id.SeekBar01);

        delay();

        assertTrue("SeekBar should have correct max", slider.getMax() != -1);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
