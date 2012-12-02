package at.ac.tuwien.detlef.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import at.ac.tuwien.detlef.R;


/**
 * This activity is started when the application runs for the first time. It guides
 * the user through the set up process, which looks like this:
 *
 *  <ul>
 *      <li>User enters username, password, device name</li>
 *      <li>Check if credentials correct</li>
 *      <li>Generate device ID</li>
 *      <li>Create device with given device name and created ID on GPodder.net</li>
 *      <li>Ask user if he wants to add the podcasts from all his devices up to now</li>
 *      <li>If yes, add podcasts from all other GPodder.net devices</li>
 *      <li>Device Name may be configurable afterwards</li>
 *  </ul>
 *
 * @author moe
 *
 */
public class SetupActivity extends FragmentActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setup_activity_layout);

    }

}
