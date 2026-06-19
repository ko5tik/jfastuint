package io.nervous.juint;

import java.math.BigInteger;

/**
 * Represents unsigned values less than {@code 2**128}.
 *
 * As indicated by the type signatures, arithmetic operations are not applicable
 * to types of other widths in this package.  Copy constructors can be used to
 * explicitly promote or truncate values for the purposes of interoperability.
 */
public final class UInt128 extends UInt<UInt128> {
  static final int MAX_WIDTH = 4;

  /**
   * Maximum representable value.
   */
  public static UInt128 MAX_VALUE = new UInt128(Arrays.maxValue(MAX_WIDTH));

  public static UInt128 ZERO = new UInt128(Arrays.ZERO);
  public static UInt128 ONE  = new UInt128(Arrays.ONE);
  public static UInt128 TWO  = new UInt128(Arrays.TWO);

  /**
   * Construct from a big-endian {@code int} array.
   *
   * If {@code ints} exceeds {@link MAX_VALUE}, only the maximum prefix
   * will be considered.  Leaves {@code ints} untouched.
   */
  public UInt128(final int[] ints) {
    super(ints, MAX_WIDTH);
  }

  /**
   * Construct from a big-endian {@code byte} array.
   *
   * If {@code bytes} exceeds {@link MAX_VALUE}, only the maximum prefix
   * will be considered.  Leaves {@code bytes} untouched.
   */
  public UInt128(final byte[] bytes) {
    super(bytes, MAX_VALUE);
  }

  /**
   * Construct from a {@link UInt256}.
   *
   * Excessively wide numbers will be truncated.
   */
  public UInt128(final UInt256 other) {
    super(other, MAX_WIDTH);
  }

  /**
   * Construct from a base ten string.
   *
   * Excessively wide numbers will be truncated.
   *
   * @throws NumberFormatException Negative, invalid or zero-length number.
   */
  public UInt128(final String s) {
    this(s, 10);
  }

  /**
   * Construct from a string in the given radix.
   *
   * Excessively wide numbers will be truncated.
   *
   * @throws NumberFormatException Negative, invalid or zero-length number.
   */
  public UInt128(final String s, final int radix) {
    super(s, radix, MAX_WIDTH);
  }

  /**
   * Construct from a {@link BigInteger}.
   *
   * If {@code b} exceeds {@link MAX_VALUE}, it's truncated.
   */
  public UInt128(final BigInteger b) { super(b, MAX_WIDTH); }

  /**
   * Construct from a {@code long}, when considered unsigned.
   *
   * For low values of {@code v}, an array cache may be used.
   */
    public UInt128(final long v) { super(v, MAX_WIDTH); }

  public UInt128 not() {
    return new UInt128(Arrays.not(ints, MAX_VALUE.ints), false);
  }

  public UInt128 and(final UInt128 other) {
    return new UInt128(Arrays.and(ints, other.ints), false);
  }

  public UInt128 or(final UInt128 other) {
    return new UInt128(Arrays.or(ints, other.ints), false);
  }

  public UInt128 xor(final UInt128 other) {
    return new UInt128(Arrays.xor(ints, other.ints), false);
  }

  public UInt128 setBit(final int bit) {
    if(bit < 0) {
      throw new ArithmeticException("Negative bit address");
    }
    return ((MAX_WIDTH <= bit >>> 5) ? this :
            new UInt128(Arrays.setBit(ints, bit), false));
  }

  public UInt128 clearBit(final int bit) {
    if(bit < 0) {
      throw new ArithmeticException("Negative bit address");
    }
    return ((ints.length <= bit >>> 5) ? this :
            new UInt128(Arrays.clearBit(ints, bit), false));
  }

  public UInt128 flipBit(final int bit) {
     if(bit < 0) {
       throw new ArithmeticException("Negative bit address");
     }
     return ((MAX_WIDTH <= bit >>> 5) ? this :
             new UInt128(Arrays.flipBit(ints, bit), false));
  }

  public UInt128 shiftLeft(final int places) {
    return new UInt128(
      0 < places ?
      Arrays.lshift(ints,  places, MAX_WIDTH) :
      Arrays.rshift(ints, -places, MAX_WIDTH), false);
  }

  public UInt128 shiftRight(final int places) {
    return new UInt128(
      0 < places ?
      Arrays.rshift(ints,  places, MAX_WIDTH) :
      Arrays.lshift(ints, -places, MAX_WIDTH), false);
  }

  public UInt128 inc() {
    return new UInt128(Arrays.inc(ints, MAX_WIDTH), false);
  }

  public UInt128 dec() {
    return isZero() ? MAX_VALUE : new UInt128(Arrays.dec(ints), false);
  }

  public UInt128 add(final UInt128 other) {
    return (isZero() ? other :
            (other.isZero() ? this :
             new UInt128(Arrays.add(ints, other.ints, MAX_WIDTH), false)));
  }

  public UInt128 addmod(final UInt128 add, final UInt128 mod) {
    if(mod.isZero()) {
      throw new ArithmeticException("div/mod by zero");
    }
    return new UInt128(Arrays.addmod(ints, add.ints, mod.ints), false);
  }

  public UInt128 subtract(final UInt128 other) {
    if(other.isZero()) {
      return this;
    }
    final int cmp = compareTo(other);
    return (cmp == 0 ? ZERO :
            new UInt128(
              cmp < 0 ?
              Arrays.subgt(ints, other.ints, MAX_VALUE.ints) :
              Arrays.sub  (ints, other.ints), false));
  }

  public UInt128 multiply(final UInt128 other) {
    if(ints.length == 0 || other.ints.length == 0)
      return ZERO;

    boolean[] overflow = new boolean[1];
    int[] res = Arrays.multiply(ints, other.ints, MAX_WIDTH, overflow);
    return new UInt128(res, overflow[0]);
  }

  public UInt128 mulmod(final UInt128 mul, final UInt128 mod) {
    if(mod.isZero()) {
      throw new ArithmeticException("div/mod by zero");
    }
    return new UInt128(Arrays.mulmod(ints, mul.ints, mod.ints), false);
  }

  public UInt128 pow(final int exp) {
    if(exp < 0) {
      throw new ArithmeticException("Negative exponent");
    }
    if(exp == 0) {
      return ONE;
    }
    if(isZero()) {
      return this;
    }
    if(exp == 1) {
      return this;
    }
    boolean[] overflow = new boolean[1];
    int[] res = Arrays.pow(ints, getLowestSetBit(), exp, MAX_WIDTH, overflow);
    return new UInt128(res, overflow[0]);
  }

  public UInt128 sqrt() {
    if (isZero()) {
      return ZERO;
    }
    return new UInt128(Arrays.sqrt(ints, MAX_WIDTH), false);
  }

  public UInt128 divide(final UInt128 other) {
    if(other.isZero()) {
      throw new ArithmeticException("div/mod by zero");
    }
    if(isZero()) {
      return ZERO;
    }
    final int cmp = compareTo(other);
    return (cmp  <  0 ? ZERO :
            (cmp == 0 ? ONE  :
             new UInt128(Arrays.divide(ints, other.ints), false)));
  }

  public UInt128 mod(final UInt128 other) {
    if(other.isZero()) {
      throw new ArithmeticException("div/mod by zero");
    }
    if(isZero()) {
      return ZERO;
    }
    final int cmp = compareTo(other);
    return (cmp  <  0 ? this :
            (cmp == 0 ? ZERO :
             new UInt128(Arrays.mod(ints, other.ints), false)));
  }

  public UInt128[] divmod(final UInt128 other) {
    if(other.isZero()) {
      throw new ArithmeticException("div/mod by zero");
    }
    if(isZero()) {
      return new UInt128[]{ZERO, ZERO};
    }
    final int cmp = compareTo(other);
    if(cmp < 0) {
      return new UInt128[]{ZERO, this};
    }
    if(cmp == 0) {
      return new UInt128[]{ONE, ZERO};
    }

    final int[][] qr = Arrays.divmod(ints, other.ints);
    return new UInt128[]{new UInt128(qr[0], false), new UInt128(qr[1], false)};
  }

  public boolean equals(final Object other) {
    if(other instanceof BigInteger) {
      return this.toBigInteger().equals(other);
    }
    return super.equals(other);
  }

  public static UInt128 mutable(final int[] ints) {
    return new UInt128(padToWidthBE(ints, MAX_WIDTH), false);
  }

  public static UInt128 mutable(final long v) {
    return mutable(Arrays.valueOf(v));
  }

  UInt128(final int[] ints, final boolean overflow) {
    super(ints);
    this.overflow = overflow;
  }

  @Override
  public UInt128 mNot() {
    this.overflow |= Arrays.mNot(this.ints);
    return this;
  }

  @Override
  public UInt128 mAnd(final UInt128 other) {
    this.overflow |= Arrays.mAnd(this.ints, other.ints);
    return this;
  }

  @Override
  public UInt128 mOr(final UInt128 other) {
    this.overflow |= Arrays.mOr(this.ints, other.ints);
    return this;
  }

  @Override
  public UInt128 mXor(final UInt128 other) {
    this.overflow |= Arrays.mXor(this.ints, other.ints);
    return this;
  }

  @Override
  public UInt128 mSetBit(final int bit) {
    this.overflow |= Arrays.mSetBit(this.ints, bit);
    return this;
  }

  @Override
  public UInt128 mClearBit(final int bit) {
    this.overflow |= Arrays.mClearBit(this.ints, bit);
    return this;
  }

  @Override
  public UInt128 mFlipBit(final int bit) {
    this.overflow |= Arrays.mFlipBit(this.ints, bit);
    return this;
  }

  @Override
  public UInt128 mShiftLeft(final int places) {
    this.overflow |= Arrays.mShiftLeft(this.ints, places);
    return this;
  }

  @Override
  public UInt128 mShiftRight(final int places) {
    this.overflow |= Arrays.mShiftRight(this.ints, places);
    return this;
  }

  @Override
  public UInt128 mInc() {
    this.overflow |= Arrays.mInc(this.ints);
    return this;
  }

  @Override
  public UInt128 mDec() {
    this.overflow |= Arrays.mDec(this.ints);
    return this;
  }

  @Override
  public UInt128 mAdd(final UInt128 other) {
    this.overflow |= Arrays.mAdd(this.ints, other.ints);
    return this;
  }

  @Override
  public UInt128 mSubtract(final UInt128 other) {
    this.overflow |= Arrays.mSubtract(this.ints, other.ints);
    return this;
  }

  @Override
  public UInt128 mMultiply(final UInt128 other) {
    this.overflow |= Arrays.mMultiply(this.ints, other.ints);
    return this;
  }

  @Override
  public UInt128 mAddMod(final UInt128 add, final UInt128 mod) {
    this.overflow |= Arrays.mAddMod(this.ints, add.ints, mod.ints);
    return this;
  }

  @Override
  public UInt128 mMulMod(final UInt128 mul, final UInt128 mod) {
    this.overflow |= Arrays.mMulMod(this.ints, mul.ints, mod.ints);
    return this;
  }

  @Override
  public UInt128 mPow(final int exp) {
    this.overflow |= Arrays.mPow(this.ints, exp);
    return this;
  }

  @Override
  public UInt128 mSqrt() {
    this.overflow |= Arrays.mSqrt(this.ints);
    return this;
  }

  @Override
  public UInt128 mDivide(final UInt128 other) {
    this.overflow |= Arrays.mDivide(this.ints, other.ints);
    return this;
  }

  @Override
  public UInt128 mMod(final UInt128 other) {
    this.overflow |= Arrays.mMod(this.ints, other.ints);
    return this;
  }
}
