package de.pribluda.ether.jfastuint;

import java.math.BigInteger;
import static de.pribluda.ether.jfastuint.Arrays.LONG;

public abstract class UInt<T extends UInt>
  extends    java.lang.Number
  implements Comparable<T> {

  final int[] ints;
  final int offset;
  final int length;
  boolean overflow;

  public final boolean overflow() { return overflow; }

  @SuppressWarnings("unchecked")
  public final T reset() { this.overflow = false; return (T) this; }

  /**
   * Sets the value of this unsigned integer to the value of the other unsigned integer,
   * copying the value and the overflow state.
   * Modifies the backing array in-place. If the other integer is shorter, the remaining
   * most-significant words are zeroed. If the other integer is longer, it is truncated,
   * setting the overflow flag if any truncated words are non-zero.
   *
   * @param other the unsigned integer to copy from.
   * @return this object for chaining.
   */
  @SuppressWarnings("unchecked")
  public final T set(final UInt<?> other) {
    final int minLen = Math.min(this.length, other.length);
    System.arraycopy(other.ints, other.offset, this.ints, this.offset, minLen);
    if (this.length > other.length) {
      java.util.Arrays.fill(this.ints, this.offset + other.length, this.offset + this.length, 0);
      this.overflow = other.overflow;
    } else {
      boolean extraOverflow = false;
      for (int i = this.length; i < other.length; i++) {
        if (other.ints[other.offset + i] != 0) {
          extraOverflow = true;
          break;
        }
      }
      this.overflow = other.overflow || extraOverflow;
    }
    return (T) this;
  }

  /* toString */
  static final int DEFAULT_RADIX = 10;

  static int[] padToWidth(final int[] ints, final int maxWidth) {
    if (ints.length == maxWidth) {
      return ints;
    }
    int[] padded = new int[maxWidth];
    int len = Math.min(ints.length, maxWidth);
    System.arraycopy(ints, 0, padded, 0, len);
    return padded;
  }

  static int[] padToWidth(final int[] ints, final int offset, final int length, final int maxWidth) {
    int[] padded = new int[maxWidth];
    int len = Math.min(length, maxWidth);
    System.arraycopy(ints, offset, padded, 0, len);
    return padded;
  }

  static int[] padToWidthBE(final int[] ints, final int maxWidth) {
    int[] padded = new int[maxWidth];
    int len = Math.min(ints.length, maxWidth);
    for (int i = 0; i < len; i++) {
      padded[i] = ints[ints.length - 1 - i];
    }
    return padded;
  }

  UInt(final long l, final int maxWidth) {
    this.ints = Arrays.valueOf(l, maxWidth);
    this.offset = 0;
    this.length = maxWidth;
  }

  UInt(final int[] ints) {
    this.ints = ints;
    this.offset = 0;
    this.length = ints.length;
  }

  UInt(final int[] ints, final int offset, final int length) {
    this.ints = ints;
    this.offset = offset;
    this.length = length;
  }

  UInt(final int[] ints, final int maxWidth) {
    this(padToWidthBE(ints, maxWidth));
  }

  UInt(final UInt other, final int maxWidth) {
    this(padToWidth(other.ints, other.offset, other.length, maxWidth));
  }

  UInt(final String s, final int radix, final int maxWidth) {
    this.ints = padToWidth(StringUtil.fromString(s, radix, maxWidth), maxWidth);
    this.offset = 0;
    this.length = maxWidth;
  }

  UInt(final BigInteger b, final int maxWidth) {
    this(Arrays.from(b, maxWidth));
  }

  UInt(final byte[] bytes, final UInt maxValue) {
    this(Arrays.from(bytes, maxValue.length));
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
  public abstract T multiply(int other);
  public abstract T multiply(long other);
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
   * {@code sqrt(this)}
   */
  public abstract T sqrt();
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
  public abstract T add(int other);
  public abstract T add(long other);
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
   * @return this object for chaining.
   */
  public abstract T mNot();

  /**
   * Performs a bitwise AND operation in-place with the specified value, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mAnd(T other);

  /**
   * Performs a bitwise OR operation in-place with the specified value, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mOr(T other);

  /**
   * Performs a bitwise XOR operation in-place with the specified value, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mXor(T other);

  /**
   * Sets the specified bit in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mSetBit(int bit);

  /**
   * Clears the specified bit in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mClearBit(int bit);

  /**
   * Flips the specified bit in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mFlipBit(int bit);

  /**
   * Shifts the bits left by the specified number of places in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mShiftLeft(int places);

  /**
   * Shifts the bits right by the specified number of places in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mShiftRight(int places);

  /**
   * Increments the value in-place by 1, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mInc();

  /**
   * Decrements the value in-place by 1, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mDec();

  /**
   * Adds the specified value to this value in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mAdd(T other);
  public abstract T mAdd(int other);
  public abstract T mAdd(long other);

  /**
   * Adds the specified value to this value, modulo mod, in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mAddMod(T add, T mod);

  /**
   * Subtracts the specified value from this value in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mSubtract(T other);

  /**
   * Multiplies this value by the specified value in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mMultiply(T other);
  public abstract T mMultiply(int other);
  public abstract T mMultiply(long other);

  /**
   * Multiplies this value by the specified value, modulo mod, in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mMulMod(T mul, T mod);

  /**
   * Raises this value to the specified power in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mPow(int exp);

  /**
   * Takes the square root of this value in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mSqrt();

  /**
   * Divides this value by the specified value in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mDivide(T other);

  /**
   * Takes the remainder when this value is divided by the specified value in-place, modifying the backing array.
   * @return this object for chaining.
   */
  public abstract T mMod(T other);

  /**
   * {@code (this & (1 << bit)) != 0}
   */
  public final boolean testBit(final int bit) {
    if(bit < 0) {
      throw new ArithmeticException("Negative bit address");
    }
    final int i = bit >>> 5;
    return i < length && 0 != (ints[offset + i] & (1 << (bit & 31)));
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
      return Arrays.bitLength(ints, offset, length);
  }

  /**
   * {@code this == 0}
   */
  public final boolean isZero() {
    return Arrays.isZero(ints, offset, length);
  }

  /**
   * Return the index of the right-most set bit, or {@code -1}.
   */
  public final int getLowestSetBit() {
      return Arrays.getLowestSetBit(ints, offset, length);
  }

  /**
   * Return a hash code identical to the equivalent OpenJDK {@code BigInteger}.
   */
  public int hashCode() {
    int firstNonZero = length - 1;
    while (firstNonZero >= 0 && ints[offset + firstNonZero] == 0) {
      firstNonZero--;
    }
    if (firstNonZero < 0) return 0;
    int out = 0;
    for (int i = firstNonZero; i >= 0; i--) {
      out = (int)(31*out + (ints[offset + i] & LONG));
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
    int thisActive = this.length - 1;
    while (thisActive >= 0 && this.ints[this.offset + thisActive] == 0) {
      thisActive--;
    }
    int otherActive = other.length - 1;
    while (otherActive >= 0 && other.ints[other.offset + otherActive] == 0) {
      otherActive--;
    }
    if (thisActive < otherActive) return -1;
    if (thisActive > otherActive) return 1;
    for (int i = thisActive; i >= 0; i--) {
      int aVal = this.ints[this.offset + i];
      int bVal = other.ints[other.offset + i];
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
    return length == 0 ? 0 : ints[offset];
  }

  public final long longValue() {
    final int len = length;
    if(len == 0) {
      return 0;
    }
    final long out = ints[offset] & LONG;
    return len == 1 ? out : ((ints[offset + 1] & LONG) << 32 | out);
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
    return Arrays.toBigInteger(ints, offset, length);
  }

  /**
   * Return a big-endian byte array.
   */
  public final byte[] toByteArray() {
    final int bytes  = (int)Math.ceil(bitLength() / 8.0);
    final byte[] out = new byte[bytes];

    int intsi = offset, v = 0;
    for(int outi = bytes - 1, copied = 0; 0 <= outi; outi--, copied++)
      out[outi] = (byte)(v = (copied % 4 == 0) ? ints[intsi++] : v >>> 8);
    return out;
  }

  /**
   * Return a big-endian int array.
   */
  public final int[] toIntArray() {
    int[] out = new int[length];
    for (int i = 0; i < length; i++) {
      out[i] = ints[offset + length - 1 - i];
    }
    return out;
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

    final int[] stripped = Arrays.stripLeadingZeroes(ints, offset, length);
    if(stripped.length == 1) {
      return Integer.toUnsignedString(stripped[0], radix);
    }
    if(stripped.length == 2) {
      return Long.toUnsignedString(((stripped[1] & LONG) << 32) | (stripped[0] & LONG), radix);
    }

    return StringUtil.toString(stripped, radix);
  }
}
