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

import android.test.ActivityInstrumentationTestCase2;
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
