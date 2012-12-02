package at.ac.tuwien.detlef.domain;

import junit.framework.TestCase;
/**
 * Test methods for {@link DeviceId}.
 * @author moe
 *
 */
public class DeviceIdTest extends TestCase {

    public void testInvalidDeviceIdThrowsException() {
        try {
            new DeviceId('\13' + "string");
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testDeviceIdToString() {
        final String deviceId = "mydeviceid";
        assertEquals(deviceId, new DeviceId(deviceId).toString());
    }
}
