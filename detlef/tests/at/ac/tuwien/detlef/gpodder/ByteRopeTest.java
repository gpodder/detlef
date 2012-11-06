package at.ac.tuwien.detlef.gpodder;

import junit.framework.TestCase;

/**
 * Tests the {@link ByteRope}.
 *
 * @author ondra
 */
public class ByteRopeTest extends TestCase {
    public void testEmptyRope() {
        ByteRope br = new ByteRope();
        byte[] bs = br.toByteArray();

        assertEquals(0, br.length());
        assertEquals(0, bs.length);
    }

    public void testSingleElementRope() {
        ByteRope br = new ByteRope();
        br.append((byte) 42);

        byte[] bs = br.toByteArray();

        assertEquals(1, br.length());
        assertEquals(1, bs.length);

        assertEquals((byte) 42, bs[0]);
    }

    public void testSingleBlockRope() {
        ByteRope br = new ByteRope();
        byte[] src = { 1, 2, 3, 4, 5, 6 };

        br.append(src);
        byte[] bs = br.toByteArray();

        assertEquals(src.length, br.length());
        assertEquals(src.length, bs.length);

        for (int i = 0; i < src.length; ++i) {
            assertEquals(src[i], bs[i]);
        }

        src[0] = 12;
        bs = br.toByteArray();

        assertFalse(src[0] == bs[0]);
    }

    public void testTwoBlockRope() {
        ByteRope br = new ByteRope();
        byte[] srcA = { 1, 2, 3 };
        byte[] srcB = { 4, 5, 6 };

        br.append(srcA);
        br.append(srcB);

        byte[] bs = br.toByteArray();

        assertEquals(srcA.length + srcB.length, br.length());
        assertEquals(srcA.length + srcB.length, bs.length);

        int i;
        for (i = 0; i < srcA.length; ++i) {
            assertEquals(srcA[i], bs[i]);
        }
        for (i = 0; i < srcB.length; ++i) {
            assertEquals(srcB[i], bs[srcA.length + i]);
        }
    }
}
