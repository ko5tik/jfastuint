package io.nervous.juint;

import java.math.BigInteger;
import static io.nervous.juint.Arrays.LONG;

public abstract class UInt<T extends UInt>
  extends    java.lang.Number
  implements Comparable<T> {

  final int[] ints;

  /* toString */
  static final int DEFAULT_RADIX = 10;

  static int[] padToWidth(final int[] ints, final int maxWidth) {
    if (ints.length == maxWidth) {
      return ints;
    }
    int[] padded = new int[maxWidth];
    int len = Math.min(ints.length, maxWidth);
    System.arraycopy(ints, ints.length - len, padded, maxWidth - len, len);
    return padded;
  }

  UInt(final long l, final int maxWidth) {
    this.ints = padToWidth(Arrays.valueOf(l), maxWidth);
  }

  UInt(final int[] ints) {
    this.ints = ints;
  }

  UInt(final int[] ints, final int maxWidth) {
    this(padToWidth(ints, maxWidth));
  }

  UInt(final UInt other, final int maxWidth) {
    this(other.ints, maxWidth);
  }

  UInt(final String s, final int radix, final int maxWidth) {
    this.ints = padToWidth(StringUtil.fromString(s, radix, maxWidth), maxWidth);
  }

  UInt(final BigInteger b, final int maxWidth) {
    this(Arrays.from(b, maxWidth), maxWidth);
  }

  UInt(final byte[] bytes, final UInt maxValue) {
    this(Arrays.from(bytes, maxValue.ints), maxValue.ints.length);
  }

  /**
   * {@code this / other, this % other}
   */
  public abstract T[] divmod(T other);
  /**
   * {@code this / other}
   */
  public abstract T divide(T other);
  /**
   * {@code this % other}
   */
  public abstract T mod(T other);
  /**
   * {@code this * other}
   */
  public abstract T multiply(T other);
  /**
   * {@code (this * mul) % mod}
   *
   * The multiply operation is unbounded by the type's width.
   */
  public abstract T mulmod(T mul, T mod);
  /**
   * {@code this ** exp}
   */
  public abstract T pow(int exp);
  /**
   * {@code ~this}
   */
  public abstract T not();
  /**
   * {@code this & other}
   */
  public abstract T and(T other);
  /**
   * {@code this | other}
   */
  public abstract T or(T other);
  /**
   * {@code this ^ other}
   */
  public abstract T xor(T other);
  /**
   * {@code this + 1}
   */
  public abstract T inc();
  /**
   * {@code this - 1}
   */
  public abstract T dec();
  /**
   * {@code this + other}
   */
  public abstract T add(T other);
  /**
   * {@code (this + add) % mod}
   *
   * The add operation is unbounded by the type's width.
   */
  public abstract T addmod(T add, T mod);
  /**
   * {@code this - other}
   */
  public abstract T subtract(T other);
  /**
   * {@code this << places}.
   *
   * Shifts right if {@code places} is negative.
   */
  public abstract T shiftLeft(int places);
  /**
   * {@code this >> places}.
   *
   * Shifts left if {@code places} is negative.
   */
  public abstract T shiftRight(int places);
  /**
   * {@code this | (1 << bit)}
   */
  public abstract T setBit(int bit);
  /**
   * {@code this & ~(1 << bit)}
   */
  public abstract T clearBit(int bit);
  /**
   * {@code this ^ (1 << bit)}
   */
  public abstract T flipBit(int bit);

  /**
   * Performs a bitwise NOT operation in-place, modifying the backing array.
   * @return true if an overflow occurred, false otherwise.
   */
  public abstract boolean mNot();

  /**
   * Performs a bitwise AND operation in-place with the specified value, modifying the backing array.
   * @return true if an overflow/out-of-bounds occurred, false otherwise.
   */
  public abstract boolean mAnd(T other);

  /**
   * Performs a bitwise OR operation in-place with the specified value, modifying the backing array.
   * @return true if an overflow/out-of-bounds occurred, false otherwise.
   */
  public abstract boolean mOr(T other);

  /**
   * Performs a bitwise XOR operation in-place with the specified value, modifying the backing array.
   * @return true if an overflow/out-of-bounds occurred, false otherwise.
   */
  public abstract boolean mXor(T other);

  /**
   * Sets the specified bit in-place, modifying the backing array.
   * @return true if the bit index is out of bounds for the backing array, false otherwise.
   */
  public abstract boolean mSetBit(int bit);

  /**
   * Clears the specified bit in-place, modifying the backing array.
   * @return true if the bit index is out of bounds for the backing array, false otherwise.
   */
  public abstract boolean mClearBit(int bit);

  /**
   * Flips the specified bit in-place, modifying the backing array.
   * @return true if the bit index is out of bounds for the backing array, false otherwise.
   */
  public abstract boolean mFlipBit(int bit);

  /**
   * Shifts the bits left by the specified number of places in-place, modifying the backing array.
   * @return true if any non-zero bits were shifted out (overflow), false otherwise.
   */
  public abstract boolean mShiftLeft(int places);

  /**
   * Shifts the bits right by the specified number of places in-place, modifying the backing array.
   * @return true if any non-zero bits were shifted out (underflow), false otherwise.
   */
  public abstract boolean mShiftRight(int places);

  /**
   * Increments the value in-place by 1, modifying the backing array.
   * @return true if a carry/overflow occurred, false otherwise.
   */
  public abstract boolean mInc();

  /**
   * Decrements the value in-place by 1, modifying the backing array.
   * @return true if an underflow occurred, false otherwise.
   */
  public abstract boolean mDec();

  /**
   * Adds the specified value to this value in-place, modifying the backing array.
   * @return true if a carry/overflow occurred, false otherwise.
   */
  public abstract boolean mAdd(T other);

  /**
   * Adds the specified value to this value, modulo mod, in-place, modifying the backing array.
   * @return true if a carry/overflow/underflow occurred, false otherwise.
   */
  public abstract boolean mAddMod(T add, T mod);

  /**
   * Subtracts the specified value from this value in-place, modifying the backing array.
   * @return true if a borrow/underflow occurred, false otherwise.
   */
  public abstract boolean mSubtract(T other);

  /**
   * Multiplies this value by the specified value in-place, modifying the backing array.
   * @return true if a carry/overflow occurred, false otherwise.
   */
  public abstract boolean mMultiply(T other);

  /**
   * Multiplies this value by the specified value, modulo mod, in-place, modifying the backing array.
   * @return true if a carry/overflow/underflow occurred, false otherwise.
   */
  public abstract boolean mMulMod(T mul, T mod);

  /**
   * Raises this value to the specified power in-place, modifying the backing array.
   * @return true if a carry/overflow occurred, false otherwise.
   */
  public abstract boolean mPow(int exp);

  /**
   * Divides this value by the specified value in-place, modifying the backing array.
   * @return true if an overflow occurred, false otherwise.
   */
  public abstract boolean mDivide(T other);

  /**
   * Takes the remainder when this value is divided by the specified value in-place, modifying the backing array.
   * @return true if an overflow occurred, false otherwise.
   */
  public abstract boolean mMod(T other);

  /**
   * {@code (this & (1 << bit)) != 0}
   */
  public final boolean testBit(final int bit) {
    if(bit < 0) {
      throw new ArithmeticException("Negative bit address");
    }
    final int i = bit >>> 5;
    return i < ints.length && 0 != (ints[ints.length - i - 1] & (1 << (bit & 31)));
  }

  /**
   * Alias for {@link #divmod}.
   */
  public final T[] divideAndRemainder(T other) { return divmod(other); }

  /**
   * Alias for {@link #mod}.
   */
  public final T remainder(T other) { return mod(other); };

  /**
   * Count the number of bits required to represent this number in binary.
   */
  public final int bitLength() {
    int firstNonZero = 0;
    while (firstNonZero < ints.length && ints[firstNonZero] == 0) {
      firstNonZero++;
    }
    if (firstNonZero == ints.length) return 0;
    int activeLen = ints.length - firstNonZero;
    return ((activeLen - 1) * 32) + (32 - Integer.numberOfLeadingZeros(ints[firstNonZero]));
  }

  /**
   * {@code this == 0}
   */
  public final boolean isZero() {
    return Arrays.isZero(ints);
  }

  /**
   * Return the index of the right-most set bit, or {@code -1}.
   */
  public final int getLowestSetBit() {
    final int start = ints.length - 1;
    for(int i = start; 0 <= i; i--) {
      if(ints[i] != 0) {
        return (start - i) * 32 + Integer.numberOfTrailingZeros(ints[i]);
      }
    }
    return -1;
  }

  /**
   * Return a hash code identical to the equivalent OpenJDK {@code BigInteger}.
   */
  public int hashCode() {
    int firstNonZero = 0;
    while (firstNonZero < ints.length && ints[firstNonZero] == 0) {
      firstNonZero++;
    }
    if (firstNonZero == ints.length) return 0;
    int out = 0;
    for (int i = firstNonZero; i < ints.length; i++) {
      out = (int)(31*out + (ints[i] & LONG));
    }
    return out;
  }

  public boolean equals(final Object other) {
    if(other instanceof UInt) {
      return compareTo((T)other) == 0;
    }
    return false;
  }

  public final int compareTo(final T other) {
    int thisStart = 0;
    while (thisStart < this.ints.length && this.ints[thisStart] == 0) {
      thisStart++;
    }
    int otherStart = 0;
    while (otherStart < other.ints.length && other.ints[otherStart] == 0) {
      otherStart++;
    }
    int thisLen = this.ints.length - thisStart;
    int otherLen = other.ints.length - otherStart;
    if (thisLen < otherLen) return -1;
    if (thisLen > otherLen) return 1;
    for (int i = 0; i < thisLen; i++) {
      int aVal = this.ints[thisStart + i];
      int bVal = other.ints[otherStart + i];
      if (aVal != bVal) {
        return Integer.compareUnsigned(aVal, bVal);
      }
    }
    return 0;
  }

  /**
   * {@code other < this ? this : other}
   */
  @SuppressWarnings("unchecked")
  public final T max(final T other) {
    return 0 < compareTo(other) ? ((T)this) : other;
  }

  /**
   * {@code this < other ? this : other }
   */
  @SuppressWarnings("unchecked")
  public final T min(final T other) {
    return compareTo(other) < 0 ? ((T)this) : other;
  }

  public final int intValue() {
    return ints.length == 0 ? 0 : ints[ints.length - 1];
  }

  public final long longValue() {
    final int len = ints.length;
    if(len == 0) {
      return 0;
    }
    final long out = ints[len - 1] & LONG;
    return ints.length == 1 ? out : ((ints[len - 2] & LONG) << 32 | out);
  }

  public final float floatValue() {
    /* Unless somebody desperately wants this to be as efficient as possible,
       I don't think it's worth spending time on - as with doubleValue. */
    return Float.parseFloat(toString());
  }

  public final double doubleValue() {
    return Double.parseDouble(toString());
  }

  public final int intValueExact() {
    if(bitLength() < 32) {
      return intValue();
    }
    throw new ArithmeticException("Out of int range");
  }

  public final long longValueExact() {
    if(bitLength() < 64) {
      return longValue();
    }
    throw new ArithmeticException("Out of long range");
  }

  public final short shortValueExact() {
    if(bitLength() < 32) {
      final int v = intValue();
      if(Short.MIN_VALUE <= v && v <= Short.MAX_VALUE) {
        return shortValue();
      }
    }
    throw new ArithmeticException("Out of short range");
  }

  public final byte byteValueExact() {
    if(bitLength() < 32) {
      final int v = intValue();
      if(Byte.MIN_VALUE <= v && v <= Byte.MAX_VALUE) {
        return byteValue();
      }
    }
    throw new ArithmeticException("Out of byte range");
  }

  public final BigInteger toBigInteger() {
    return Arrays.toBigInteger(ints);
  }

  /**
   * Return a big-endian byte array.
   */
  public final byte[] toByteArray() {
    final int bytes  = (int)Math.ceil(bitLength() / 8.0);
    final byte[] out = new byte[bytes];

    int intsi = ints.length - 1, v = 0;
    for(int outi = bytes - 1, copied = 0; 0 <= outi; outi--, copied++)
      out[outi] = (byte)(v = (copied % 4 == 0) ? ints[intsi--] : v >>> 8);
    return out;
  }

  /**
   * Return a big-endian int array.
   */
  public final int[] toIntArray() {
    return java.util.Arrays.copyOf(ints, ints.length);
  }

 /**
  * Decimal string representation.
  */
  public final String toString() {
    return toString(DEFAULT_RADIX);
  }

  /**
   * String representation in the given radix.
   *
   * {@code radix} values outside {@code Character.MIN_RADIX} and
   * {@code Character.MAX_RADIX} are substituted with {@code 10}.
   */
  public final String toString(int radix) {
    if(isZero()) {
      return "0";
    }

    if(radix < Character.MIN_RADIX || Character.MAX_RADIX < radix) {
      radix = DEFAULT_RADIX;
    }

    final int[] stripped = Arrays.stripLeadingZeroes(ints);
    if(stripped.length == 1) {
      return Integer.toUnsignedString(stripped[0], radix);
    }
    if(stripped.length == 2) {
      return Long.toUnsignedString(((stripped[0] & LONG) << 32) | (stripped[1] & LONG), radix);
    }

    return StringUtil.toString(stripped, radix);
  }
}
