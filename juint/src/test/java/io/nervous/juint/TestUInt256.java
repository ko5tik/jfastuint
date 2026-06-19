package io.nervous.juint;

import java.math.BigInteger;

import org.junit.BeforeClass;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class TestUInt256 extends Properties<UInt256> {
  UInt256 construct(int[] ints)   { return new UInt256(ints);  }
  UInt256 constructMutable(int[] ints) { return UInt256.mutable(ints); }
  UInt256 constructMutable(long  v)    { return UInt256.mutable(v); }
  UInt256 construct(byte[] bytes) { return new UInt256(bytes); }
  UInt256 construct(BigInteger b) { return new UInt256(b);     }
  UInt256 construct(long   v)     { return new UInt256(v);     }

  UInt256 construct(String s, int radix) {
    return new UInt256(s, radix);
  }

  int maxWidth() {
    return UInt256.MAX_WIDTH;
  }

  @Test
  public void copyCtor() {
    for(int i = 0; i < SAMPLE_SMALL; i++) {
      int[] ints    = randomints(UInt128.MAX_WIDTH);
      UInt128 small = new UInt128(ints);
      assertEquals(new UInt256(small.toBigInteger()), new UInt256(small));
      assertEquals(small, new UInt128(new UInt256(small)));
      int[] expected = new int[UInt256.MAX_WIDTH];
      for (int j = 0; j < ints.length; j++) {
        expected[j] = ints[ints.length - 1 - j];
      }
      assertArrayEquals(expected, new UInt256(small).ints);
    }
  }

  @Test
  public void multiplyOverflow() {
    // 2^255 * 2 = 2^256 (overflows)
    UInt256 a = one.shiftLeft(255);
    UInt256 res1 = a.multiply(two);
    assertTrue(res1.overflow());
    assertTrue(res1.isZero());

    // MAX_VALUE * 2 overflows
    UInt256 res2 = max.multiply(two);
    assertTrue(res2.overflow());
    
    // 2^128 * 2^127 = 2^255 (fits)
    UInt256 b = one.shiftLeft(128);
    UInt256 c = one.shiftLeft(127);
    UInt256 res3 = b.multiply(c);
    assertFalse(res3.overflow());
    assertEquals(one.shiftLeft(255), res3);
  }

  @Test
  public void powOverflow() {
    // 2^128 ^ 2 = 2^256 (overflows)
    UInt256 a = one.shiftLeft(128);
    UInt256 res1 = a.pow(2);
    assertTrue(res1.overflow());
    assertTrue(res1.isZero());

    // 3^162 overflows (since 3^162 > 2^256)
    UInt256 b = construct(3);
    UInt256 res2 = b.pow(162);
    assertTrue(res2.overflow());

    // 2^20 ^ 12 = 2^240 (fits)
    UInt256 c = one.shiftLeft(20);
    UInt256 res3 = c.pow(12);
    assertFalse(res3.overflow());
    assertEquals(one.shiftLeft(240), res3);
  }
}
