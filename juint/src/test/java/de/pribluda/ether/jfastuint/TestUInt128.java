package de.pribluda.ether.jfastuint;

import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class TestUInt128 extends Properties<UInt128> {
  UInt128 construct(int[] ints)   { return new UInt128(ints);  }
  UInt128 constructMutable(int[] ints) { return UInt128.mutable(ints); }
  UInt128 constructMutable(long  v)    { return UInt128.mutable(v); }
  UInt128 construct(byte[] bytes) { return new UInt128(bytes); }
  UInt128 construct(BigInteger b) { return new UInt128(b);     }
  UInt128 construct(long   v)     { return new UInt128(v);     }

  UInt128 construct(String s, int radix) {
    return new UInt128(s, radix);
  }

  int maxWidth() {
    return UInt128.MAX_WIDTH;
  }

  @Test
  public void copyCtor() {
    for(int i = 0; i < SAMPLE_SMALL; i++) {
      int[] ints    = randomints(UInt256.MAX_WIDTH);
      UInt256 large = new UInt256(ints);
      assertEquals(new UInt128(large.toBigInteger()), new UInt128(large));
      int[] expected = new int[UInt128.MAX_WIDTH];
      for (int j = 0; j < UInt128.MAX_WIDTH; j++) {
        expected[j] = ints[UInt256.MAX_WIDTH - 1 - j];
      }
      assertArrayEquals(expected, new UInt128(large).ints);
    }
  }

  @Test
  public void multiplyOverflow() {
    // 2^127 * 2 = 2^128 (overflows)
    UInt128 a = one.shiftLeft(127);
    UInt128 res1 = a.multiply(two);
    assertTrue(res1.overflow());
    assertTrue(res1.isZero());

    // MAX_VALUE * 2 overflows
    UInt128 res2 = max.multiply(two);
    assertTrue(res2.overflow());
    
    // 2^64 * 2^63 = 2^127 (fits)
    UInt128 b = one.shiftLeft(64);
    UInt128 c = one.shiftLeft(63);
    UInt128 res3 = b.multiply(c);
    assertFalse(res3.overflow());
    assertEquals(one.shiftLeft(127), res3);
  }

  @Test
  public void powOverflow() {
    // 2^64 ^ 2 = 2^128 (overflows)
    UInt128 a = one.shiftLeft(64);
    UInt128 res1 = a.pow(2);
    assertTrue(res1.overflow());
    assertTrue(res1.isZero());

    // 3^81 overflows
    UInt128 b = construct(3);
    UInt128 res2 = b.pow(81);
    assertTrue(res2.overflow());

    // 2^10 ^ 12 = 2^120 (fits)
    UInt128 c = one.shiftLeft(10);
    UInt128 res3 = c.pow(12);
    assertFalse(res3.overflow());
    assertEquals(one.shiftLeft(120), res3);
  }
}
