
package at.ac.tuwien.detlef.AllTests;

import junit.framework.Test;
import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

/**
 * Test class which includes the tests under at.ac.tuwien.detlef.db
 * @author Lacky
 *
 */
public class AllTests extends TestSuite {

    public static Test suite() {
        return new TestSuiteBuilder(AllTests.class).includePackages("at.ac.tuwien.detlef.db")
                .includePackages("at.ac.tuwien.detlef.domain")
                .build();
    }
}
