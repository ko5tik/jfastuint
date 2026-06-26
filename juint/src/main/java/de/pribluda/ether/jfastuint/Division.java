package de.pribluda.ether.jfastuint;

/**
 * High-performance division algorithms implemented directly for Little-Endian array representations.
 * Contains single-word, double-word (long), and multi-word array division routines utilizing
 * Knuth's Division Algorithm D (from TAOCP Vol 2) optimized to avoid array reversals.
 */
final class Division {
  private static final long LONG = 0xffffffffL;

  /**
   * Performs in-place division of a multi-word Little-Endian dividend by a single 32-bit unsigned integer divisor.
   * Runs right-to-left (most significant to least significant word) directly on the Little-Endian inputs.
   *
   * @param a      the dividend array in Little-Endian layout
   * @param aLen   active length of the dividend array
   * @param b      the 32-bit divisor (treated as unsigned)
   * @param quo    output array where the quotient will be stored
   * @param remOut output array of size 1 where the remainder will be stored
   */
  static void div(final int[] a, final int aLen, final int b, final int[] quo, final int[] remOut) {
    final long bl = b & LONG;
    long rem = 0;
    // Process from most significant word (at index aLen - 1) down to least significant word (index 0)
    for (int i = aLen - 1; i >= 0; i--) {
      long dividend = (rem << 32) | (a[i] & LONG);
      long q;
      if (dividend >= 0) {
        q = dividend / bl;
        rem = dividend - q * bl;
      } else {
        q = Long.divideUnsigned(dividend, bl);
        rem = dividend - q * bl;
      }
      quo[i] = (int) q;
    }
    remOut[0] = (int) rem;
  }

  /**
   * Performs in-place division of a multi-word Little-Endian dividend by a 64-bit unsigned long divisor.
   * Uses Knuth's Division Algorithm D tailored for a 2-word (64-bit) divisor in Little-Endian layout.
   *
   * @param a      the dividend array in Little-Endian layout
   * @param aLen   active length of the dividend array
   * @param b      the 64-bit divisor (treated as unsigned)
   * @param quo    output array where the quotient will be stored
   * @param remOut output array where the remainder will be stored
   */
  static void div(final int[] a, final int aLen, long b, final int[] quo, final int[] remOut) {
    java.util.Arrays.fill(remOut, 0, aLen + 2, 0);
    java.util.Arrays.fill(quo, 0, aLen, 0);

    // Normalize divisor and dividend by shifting left so MSB of divisor is 1
    final int places = Long.numberOfLeadingZeros(b);
    if (0 < places) {
      copyshiftLE(a, aLen, remOut, places);
      b <<= places;
    } else {
      System.arraycopy(a, 0, remOut, 0, aLen);
    }

    final int dh = (int)(b >>> 32);
    final long dhl = dh & LONG;
    final int dl = (int)(b & LONG);

    // Main Knuth loop running from most significant quotient word down to least significant
    int qhat;
    for (int i = aLen - 2; i >= 0; i--) {
      if ((qhat = D3_long_LE(i, remOut, dh, dhl, dl)) != 0) {
        quo[i] = D4_D5_long_LE(i, remOut, dh, dl, qhat);
      }
    }

    // Denormalize remainder
    if (0 < places) {
      rshiftLE(remOut, aLen + 1, places);
    }
  }

  /**
   * Performs multi-precision division of two unsigned Little-Endian integers in-place.
   * Implements Knuth's Division Algorithm D operating directly on Little-Endian layouts.
   *
   * @param a          the dividend array in Little-Endian layout
   * @param aLen       active length of the dividend array
   * @param b          the divisor array in Little-Endian layout
   * @param bLen       active length of the divisor array
   * @param quo        output array where the quotient will be stored
   * @param remOut     output array where the remainder will be stored
   * @param divScratch scratchpad array used during normalization of the divisor
   */
  static void div(final int[] a, final int aLen, final int[] b, final int bLen, final int[] quo, final int[] remOut, final int[] divScratch) {
    // Normalization shift places based on leading zeros of divisor's most significant word
    final int places = Integer.numberOfLeadingZeros(b[bLen - 1]);
    final int[] activeDiv;
    final int remLen;

    if (0 < places) {
      activeDiv = divScratch;
      java.util.Arrays.fill(divScratch, 0, bLen + 1, 0);
      copyshiftLE(b, bLen, activeDiv, places);

      if (places <= Integer.numberOfLeadingZeros(a[aLen - 1])) {
        remLen = aLen + 1;
        java.util.Arrays.fill(remOut, 0, remLen + 1, 0);
        copyshiftLE(a, aLen, remOut, places);
      } else {
        remLen = aLen + 2;
        java.util.Arrays.fill(remOut, 0, remLen + 1, 0);
        copyshiftLE(a, aLen, remOut, places);
      }
    } else {
      activeDiv = b;
      remLen = aLen + 1;
      java.util.Arrays.fill(remOut, 0, remLen + 1, 0);
      System.arraycopy(a, 0, remOut, 0, aLen);
    }

    final int qints = remLen - bLen;
    java.util.Arrays.fill(quo, 0, qints + 1, 0);

    final int dh = activeDiv[bLen - 1];
    final int dl = activeDiv[bLen - 2];
    final long dhl = dh & LONG;

    // Main Knuth loop running from most significant quotient word down to least significant
    int qhat;
    for (int i = qints - 1; i >= 0; i--) {
      if ((qhat = D3_LE(i, remOut, bLen, dh, dhl, dl)) != 0) {
        quo[i] = D4_D5_LE(i, remOut, activeDiv, qhat, bLen);
      }
    }

    // Denormalize remainder
    if (0 < places) {
      rshiftLE(remOut, remLen, places);
    }
  }

  /**
   * Helper routine to copy an array while shifting left (for normalization) in Little-Endian layout.
   */
  private static void copyshiftLE(final int[] src, final int srcLen, final int[] dst, final int places) {
    final int invplaces = 32 - places;
    int carry = 0;
    for (int i = 0; i < srcLen; i++) {
      int val = src[i];
      dst[i] = (val << places) | carry;
      carry = val >>> invplaces;
    }
    dst[srcLen] = carry;
  }

  /**
   * Helper routine to shift an array right in-place (for denormalization of remainder) in Little-Endian layout.
   */
  private static void rshiftLE(final int[] a, final int len, final int places) {
    if (len == 0 || places == 0) return;
    final int bits = places & 0x1F;
    if (bits == 0) return;
    final int invShift = 32 - bits;
    for (int i = 0; i < len - 1; i++) {
      a[i] = (a[i] >>> bits) | (a[i + 1] << invShift);
    }
    a[len - 1] >>>= bits;
  }

  /**
   * Knuth step D3 for 64-bit divisor (long): Calculate quotient estimation qhat and remainder estimation qrem.
   */
  private static int D3_long_LE(final int i, final int[] rem, final int dh, final long dhl, final int dl) {
    int qhat, qrem;
    boolean correct = true;
    final int nh    = rem[i + 2];
    final int nm    = rem[i + 1];

    if (nh == dh) {
      qhat    = ~0;
      qrem    = nh + nm;
      correct = nh + 0x80000000 <= qrem + 0x80000000;
    } else {
      final long chunk = (((long)nh) << 32) | (nm & LONG);
      // Fast path: use simple JIT-optimized signed division for positive numbers
      if (chunk >= 0) {
        qhat = (int) (chunk / dhl);
        qrem = (int) (chunk - qhat * dhl);
      } else {
        qhat = (int) Long.divideUnsigned(chunk, dhl);
        qrem = (int) (chunk - qhat * dhl);
      }
    }

    if (qhat != 0 && correct) {
      final long nl = rem[i] & LONG;
      long rs       = ((qrem & LONG) << 32) | nl;
      long est      =  (dl   & LONG) * (qhat & LONG);

      if (0 < Long.compareUnsigned(est, rs)) {
        qhat--;
        qrem = (int)((qrem & LONG) + dhl);
        if (dhl <= (qrem & LONG)) {
          est -= (dl    & LONG);
          rs   = ((qrem & LONG) << 32) | nl;
          if (0 < Long.compareUnsigned(est, rs)) {
            qhat--;
          }
        }
      }
    }
    return qhat;
  }

  /**
   * Knuth step D4 & D5 for 64-bit divisor (long): Multiply and subtract, and add back divisor if a borrow occurred.
   */
  private static int D4_D5_long_LE(final int i, final int[] rem, final int dh, final int dl, final int qhat) {
    final int tmp    = rem[i + 2];
    rem[i + 2]       = 0;
    final int borrow = mulsub_long_LE(rem, dh, dl, qhat & LONG, i);

    if (tmp + 0x80000000L < borrow + 0x80000000L) {
      divadd_long_LE(dh, dl, rem, i);
      return qhat - 1;
    }
    return qhat;
  }

  /**
   * Multiplication and subtraction loop for a 64-bit divisor.
   */
  private static int mulsub_long_LE(final int[] q, final int dh, final int dl, final long x, final int off) {
    long prod   = (dl & LONG) * x;
    long diff   = (q[off] & LONG) - (prod & LONG);
    q[off]      = (int)diff;
    long carry  = (prod >>> 32) + (diff < 0 ? 1 : 0);
    prod        = (dh & LONG) * x + carry;
    diff        = (q[off + 1] & LONG) - (prod & LONG);
    q[off + 1]  = (int)diff;
    return (int)(prod >>> 32) + (diff < 0 ? 1 : 0);
  }

  /**
   * Correction addition loop for a 64-bit divisor.
   */
  private static int divadd_long_LE(final long dh, final long dl, final int[] result, final int off) {
    long sum = dl + (result[off] & LONG);
    result[off] = (int)sum;
    long carry = sum >>> 32;
    sum = dh + (result[off + 1] & LONG) + carry;
    result[off + 1] = (int)sum;
    return (int)(sum >>> 32);
  }

  /**
   * Knuth step D3 for multi-word array divisor: Calculate quotient estimation qhat and remainder estimation qrem.
   */
  private static int D3_LE(final int i, final int[] rem, final int bLen, final int dh, final long dhl, final int dl) {
    int qhat, qrem;
    boolean correct = true;
    final int nh    = rem[i + bLen];
    final int nm    = rem[i + bLen - 1];

    if (nh == dh) {
      qhat    = ~0;
      qrem    = nh + nm;
      correct = nh + 0x80000000 <= qrem + 0x80000000;
    } else {
      final long chunk = (((long)nh) << 32) | (nm & LONG);
      // Fast path: use simple JIT-optimized signed division for positive numbers
      if (chunk >= 0) {
        qhat = (int) (chunk / dhl);
        qrem = (int) (chunk - qhat * dhl);
      } else {
        qhat = (int) Long.divideUnsigned(chunk, dhl);
        qrem = (int) (chunk - qhat * dhl);
      }
    }

    if (qhat != 0 && correct) {
      final long nl = rem[i + bLen - 2] & LONG;
      long rs       = ((qrem & LONG) << 32) | nl;
      long est      =  (dl   & LONG) * (qhat & LONG);

      if (0 < Long.compareUnsigned(est, rs)) {
        qhat--;
        qrem = (int)((qrem & LONG) + dhl);
        if (dhl <= (qrem & LONG)) {
          est -= (dl    & LONG);
          rs   = ((qrem & LONG) << 32) | nl;
          if (0 < Long.compareUnsigned(est, rs)) {
            qhat--;
          }
        }
      }
    }
    return qhat;
  }

  /**
   * Knuth step D4 & D5 for multi-word array divisor: Multiply and subtract, and add back divisor if a borrow occurred.
   */
  private static int D4_D5_LE(final int i, final int[] rem, final int[] divisor, final int qhat, final int divLen) {
    final int tmp     = rem[i + divLen];
    rem[i + divLen]   = 0;
    final int borrow  = mulsub_LE(rem, divisor, qhat & LONG, divLen, i);

    if (tmp + 0x80000000L < borrow + 0x80000000L) {
      divadd_LE(divisor, rem, i, divLen);
      return qhat - 1;
    }
    return qhat;
  }

  /**
   * Multi-word multiplication and subtraction loop.
   */
  private static int mulsub_LE(final int[] q, final int[] a, final long x, final int len, final int off) {
    long carry = 0;
    for (int ai = 0; ai < len; ai++) {
      long prod = (a[ai] & LONG) * x + carry;
      long diff = (q[off + ai] & LONG) - (prod & LONG);
      q[off + ai] = (int)diff;
      carry = (prod >>> 32) + (diff < 0 ? 1 : 0);
    }
    return (int)carry;
  }

  /**
   * Multi-word correction addition loop.
   */
  private static int divadd_LE(final int[] a, final int[] result, final int offset, final int aLen) {
    long carry = 0;
    for (int ai = 0; ai < aLen; ai++) {
      long sum = (a[ai] & LONG) + (result[ai + offset] & LONG) + carry;
      result[ai + offset] = (int)sum;
      carry = sum >>> 32;
    }
    return (int)carry;
  }
}
