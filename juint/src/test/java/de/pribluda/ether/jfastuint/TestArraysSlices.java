package de.pribluda.ether.jfastuint;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestArraysSlices {

    @Test
    public void testIsZero() {
        int[] arr = {0, 0, 5, 0, 0};
        assertTrue(Arrays.isZero(arr, 0, 2));
        assertFalse(Arrays.isZero(arr, 1, 3));
        assertTrue(Arrays.isZero(arr, 3, 2));
    }

    @Test
    public void testBitLength() {
        // [0, 0, 0x10, 0, 0] -> slice starting at index 1, length 3 is [0, 0x10, 0]
        // 0x10 has bit length 5. In the slice, it's at index 1 of the slice (which is index 2 of the big array).
        // 1 * 32 + 5 = 37.
        int[] arr = {0, 0, 0x10, 0, 0};
        assertEquals(37, Arrays.bitLength(arr, 1, 3));
        assertEquals(0, Arrays.bitLength(arr, 0, 2));
    }

    @Test
    public void testCompareAndCompareActive() {
        int[] a = {0, 10, 20, 0};
        int[] b = {0, 0, 10, 20};

        // Compare a[1..3] which is [10, 20] with b[2..4] which is [10, 20]
        assertEquals(0, Arrays.compare(a, 1, 2, b, 2, 2));
        
        // Compare active values
        assertEquals(0, Arrays.compareActive(a, 1, 3, b, 2, 2)); // [10, 20, 0] vs [10, 20]
    }

    @Test
    public void testLogicalOperations() {
        int[] a = {0, 0x0f0f0f0f, 0, 0};
        int[] b = {0, 0, 0xf0f0f0f0, 0};

        // mAnd a[1..2] with b[2..3]
        // a[1..2] is [0x0f0f0f0f, 0]
        // b[2..3] is [0xf0f0f0f0, 0]
        int[] dest = a.clone();
        Arrays.mAnd(dest, 1, 2, b, 2, 2);
        assertEquals(0, dest[1]);
        assertEquals(0, dest[2]);

        // mOr
        dest = a.clone();
        Arrays.mOr(dest, 1, 2, b, 2, 2);
        assertEquals(0xffffffff, dest[1]);
        assertEquals(0, dest[2]);
    }

    @Test
    public void testBitShifts() {
        int[] arr = {0, 1, 2, 0};
        // shift left the slice starting at 1, length 2: [1, 2]
        // shift by 32 bits should result in [0, 1], with overflow if 2 was non-zero
        int[] dest = arr.clone();
        boolean overflow = Arrays.mShiftLeft(dest, 1, 2, 32);
        assertTrue(overflow);
        assertEquals(0, dest[1]);
        assertEquals(1, dest[2]);
        assertEquals(0, dest[0]); // outer untouched
        assertEquals(0, dest[3]); // outer untouched

        // shift right the slice starting at 1, length 2: [1, 2]
        // shift by 32 bits should result in [2, 0]
        dest = arr.clone();
        overflow = Arrays.mShiftRight(dest, 1, 2, 32);
        assertTrue(overflow); // 1 is lost
        assertEquals(2, dest[1]);
        assertEquals(0, dest[2]);
    }

    @Test
    public void testBasicArithmetic() {
        int[] a = {0, 10, 20, 0};
        int[] b = {0, 0, 5, 0};

        // mAdd a[1..2] (which is [10, 20]) and b[2..3] (which is [5, 0])
        int[] dest = a.clone();
        Arrays.mAdd(dest, 1, 2, b, 2, 2);
        assertEquals(15, dest[1]);
        assertEquals(20, dest[2]);

        // mSubtract
        dest = a.clone();
        Arrays.mSubtract(dest, 1, 2, b, 2, 2);
        assertEquals(5, dest[1]);
        assertEquals(20, dest[2]);
    }

    @Test
    public void testMultiplyAndDivide() {
        int[] a = {0, 10, 0, 0};
        int[] b = {0, 0, 5, 0};

        // Multiply a[1..2] ([10, 0]) by b[2..3] ([5, 0])
        int[] dest = a.clone();
        Arrays.mMultiply(dest, 1, 2, b, 2, 2);
        assertEquals(50, dest[1]);
        assertEquals(0, dest[2]);

        // Divide dest[1..2] ([50, 0]) by b[2..3] ([5, 0])
        Arrays.mDivide(dest, 1, 2, b, 2, 2);
        assertEquals(10, dest[1]);
        assertEquals(0, dest[2]);
    }
}
