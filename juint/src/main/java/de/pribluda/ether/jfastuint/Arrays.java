package de.pribluda.ether.jfastuint;

import java.math.BigInteger;

import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.copyOf;

/**
 * These methods don't mutate their arguments or return arrays w/ leading zeroes.
 */
final class Arrays {

    // =========================================================================
    // Constants, Initialization & Scratchpad
    // =========================================================================

    static final long LONG = 0xffffffffL;
    static final int MAX_CACHE = 28;
    static final int[][] CACHE = new int[MAX_CACHE][1];

    static {
        CACHE[0] = new int[0];
        for (int i = 1; i < MAX_CACHE; i++) {
            CACHE[i] = new int[]{i};
        }
    }

    static final int[] ZERO = CACHE[0];
    static final int[] ONE = CACHE[1];
    static final int[] TWO = CACHE[2];

    private static BigInteger BIG_INT = BigInteger.valueOf(LONG);

    static final class Scratchpad {
        final int[] a = new int[16];
        final int[] b = new int[16];
        final int[] c = new int[16];
        final int[] d = new int[16];
        final int[] quo = new int[16];
        final int[] rem = new int[16];
        final int[] product = new int[16];
        final int[] tempAdd = product;
        final int[] tempMul = b;
        final int[] tempRes8 = new int[8];
        final int[] tempRes16 = new int[16];
        final int[] a4 = new int[4];
        final int[] a8 = new int[8];
        final int[] b4 = new int[4];
        final int[] b8 = new int[8];
        final int[] c4 = new int[4];
        final int[] c8 = new int[8];
        final int[] d4 = new int[4];
        final int[] d8 = new int[8];
        final int[] r4 = new int[4];
        final int[] r8 = new int[8];
        final int[] divA = new int[16];
        final int[] divB = new int[16];
        final int[] divQuo = new int[32];
        final int[] divRem = new int[32];
        final int[] divScr = new int[16];
        // Scratchpad array for 64‑bit accumulator used in multiplication to avoid allocations
        final long[] longAcc = new long[16];
    }

    static final ThreadLocal<Scratchpad> SCRATCH = ThreadLocal.withInitial(Scratchpad::new);

    // =========================================================================
    // Basic Helpers & Conversions
    // =========================================================================

    static int[] valueOf(final long v, final int maxWidth) {
        final int[] out = new int[maxWidth];
        out[0] = (int) v;
        if (maxWidth > 1) {
            out[1] = (int) (v >>> 32);
        }
        return out;
    }

    static int[] from(BigInteger b, final int maxWidth) {
        final int[] ints = new int[maxWidth];
        for (int i = 0; i < maxWidth; i++) {
            ints[i] = b.and(BIG_INT).intValue();
            b = b.shiftRight(32);
        }
        return ints;
    }

    static int[] from(final byte[] bytes, final int[] maxValue) {
        int len = bytes.length;
        final int maxWidth = maxValue.length;
        final int[] out = new int[maxWidth];

        if (len == 0) {
            return out;
        }

        int skip;
        for (skip = 0; skip < len && bytes[skip] == 0; skip++)
            ;

        final int ints = Math.min(maxWidth, ((len - skip) + 3) >>> 2);
        int b = len - 1;
        for (int i = 0; i < ints; i++) {
            out[i] = bytes[b--] & 0xff;
            int copy = Math.min(3, b - skip + 1);
            for (int j = 8; j <= (copy << 3); j += 8)
                out[i] |= ((bytes[b--] & 0xff) << j);
        }
        return out;
    }


    static BigInteger toBigInteger(final int[] ints) {
        BigInteger out = BigInteger.ZERO;
        for (int i = ints.length - 1; i >= 0; i--) {
            out = out.shiftLeft(32).or(BigInteger.valueOf(ints[i] & LONG));
        }
        return out;
    }

    static int[] maxValue(final int maxWidth) {
        final int[] max = new int[maxWidth];
        java.util.Arrays.fill(max, -1);
        return max;
    }

    static boolean isZero(final int[] ints) {
        return isZero(ints, 0, ints.length);
    }

    static boolean isZero(final int[] ints, final int offset, final int length) {
        for (int i = 0; i < length; i++) {
            if (ints[offset + i] != 0) return false;
        }
        return true;
    }

    static int bitLength(int a[]) {
        return bitLength(a, 0, a.length);
    }

    static int bitLength(int a[], final int offset, final int length) {
        int firstNonZero = length - 1;
        while (firstNonZero >= 0 && a[offset + firstNonZero] == 0) {
            firstNonZero--;
        }
        if (firstNonZero < 0) return 0;
        return (firstNonZero * 32) + (32 - Integer.numberOfLeadingZeros(a[offset + firstNonZero]));
    }

    static int[] stripLeadingZeroes(final int[] ints, int strip) {
        int end = ints.length;
        while (end > strip && ints[end - 1] == 0) {
            end--;
        }
        return end == ints.length ? ints : copyOfRange(ints, 0, end);
    }

    static int[] stripLeadingZeroes(final int[] ints) {
        return stripLeadingZeroes(ints, 0);
    }

    static byte[] stripLeadingZeroes(final byte[] bs) {
        int strip;
        final int len = bs.length;

        for (strip = 0; strip < len && bs[strip] == 0; strip++)
            ;
        return strip == 0 ? bs : copyOfRange(bs, strip, len);
    }

    // =========================================================================
    // Comparisons
    // =========================================================================

    static int compare(final int[] ints, final int[] other) {
        return compare(ints, 0, ints.length, other, 0, other.length);
    }

    static int compare(final int[] ints, final int offset, final int length, final int[] other, final int otherOffset, final int otherLength) {
        if (length < otherLength) {
            return -1;
        }
        if (length > otherLength) {
            return 1;
        }

        for (int i = length - 1; i >= 0; i--) {
            if (ints[offset + i] != other[otherOffset + i]) {
                return Integer.compareUnsigned(ints[offset + i], other[otherOffset + i]);
            }
        }

        return 0;
    }

    static int compareActive(final int[] a, final int[] b) {
        return compareActive(a, 0, a.length, b, 0, b.length);
    }

    static int compareActive(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength) {
        int aLen = aLength;
        while (aLen > 0 && a[aOffset + aLen - 1] == 0) {
            aLen--;
        }
        int bLen = bLength;
        while (bLen > 0 && b[bOffset + bLen - 1] == 0) {
            bLen--;
        }
        if (aLen < bLen) {
            return -1;
        }
        if (aLen > bLen) {
            return 1;
        }
        for (int i = aLen - 1; i >= 0; i--) {
            int aVal = a[aOffset + i];
            int bVal = b[bOffset + i];
            if (aVal != bVal) {
                return Integer.compareUnsigned(aVal, bVal);
            }
        }
        return 0;
    }

    static int compare(final int[] ints, final BigInteger other) {
        return compare(ints, 0, ints.length, other);
    }

    static int compare(final int[] ints, final int offset, final int length, final BigInteger other) {
        final int il = bitLength(ints, offset, length), bl = other.bitLength();
        if (il < bl) {
            return -1;
        }
        if (il > bl) {
            return 1;
        }

        return compareActive(ints, offset, length, from(other, length), 0, length);
    }

    // =========================================================================
    // Logical & Bitwise Operations
    // =========================================================================

    // immutable version of not
    static int[] not(final int[] ints) {
        return not(ints, 0, ints.length);
    }

    static int[] not(final int[] ints, final int offset, final int length) {
        int[] out = new int[length];
        System.arraycopy(ints, offset, out, 0, length);
        mNot(out, 0, length);
        return out;
    }

    // in place version of not
    static boolean mNot(final int[] ints) {
        return mNot(ints, 0, ints.length);
    }

    static boolean mNot(final int[] ints, final int offset, final int length) {
        for (int i = 0; i < length; i++) {
            ints[offset + i] = ~ints[offset + i];
        }
        return false;
    }

    // immutable bitwise and: copies the first operand and applies mutable mAnd
    static int[] and(final int[] a, final int[] b) {
        return and(a, 0, a.length, b, 0, b.length);
    }

    static int[] and(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength) {
        int[] out = new int[aLength];
        System.arraycopy(a, aOffset, out, 0, aLength);
        mAnd(out, 0, aLength, b, bOffset, bLength);
        return out;
    }

    // in-place bitwise and mutating the first operand 'ints'
    static boolean mAnd(final int[] ints, final int[] other) {
        return mAnd(ints, 0, ints.length, other, 0, other.length);
    }

    static boolean mAnd(final int[] ints, final int offset, final int length, final int[] other, final int otherOffset, final int otherLength) {
        for (int i = 0; i < length; i++) {
            ints[offset + i] &= (i < otherLength ? other[otherOffset + i] : 0);
        }
        return false;
    }

    // immutable bitwise or: copies the first operand and applies mutable mOr
    static int[] or(final int[] a, final int[] b) {
        return or(a, 0, a.length, b, 0, b.length);
    }

    static int[] or(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength) {
        int[] out = new int[aLength];
        System.arraycopy(a, aOffset, out, 0, aLength);
        mOr(out, 0, aLength, b, bOffset, bLength);
        return out;
    }

    // in-place bitwise or mutating the first operand 'ints'
    static boolean mOr(final int[] ints, final int[] other) {
        return mOr(ints, 0, ints.length, other, 0, other.length);
    }

    static boolean mOr(final int[] ints, final int offset, final int length, final int[] other, final int otherOffset, final int otherLength) {
        for (int i = 0; i < length; i++) {
            ints[offset + i] |= (i < otherLength ? other[otherOffset + i] : 0);
        }
        return false;
    }

    // immutable bitwise xor: copies the first operand and applies mutable mXor
    static int[] xor(final int[] a, final int[] b) {
        return xor(a, 0, a.length, b, 0, b.length);
    }

    static int[] xor(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength) {
        int[] out = new int[aLength];
        System.arraycopy(a, aOffset, out, 0, aLength);
        mXor(out, 0, aLength, b, bOffset, bLength);
        return out;
    }

    // in-place bitwise xor mutating the first operand 'ints'
    static boolean mXor(final int[] ints, final int[] other) {
        return mXor(ints, 0, ints.length, other, 0, other.length);
    }

    static boolean mXor(final int[] ints, final int offset, final int length, final int[] other, final int otherOffset, final int otherLength) {
        for (int i = 0; i < length; i++) {
            ints[offset + i] ^= (i < otherLength ? other[otherOffset + i] : 0);
        }
        return false;
    }

    // immutable setBit: copies the operand and applies mutable mSetBit
    static int[] setBit(final int[] a, final int bit) {
        return setBit(a, 0, a.length, bit);
    }

    static int[] setBit(final int[] a, final int offset, final int length, final int bit) {
        int[] out = new int[length];
        System.arraycopy(a, offset, out, 0, length);
        mSetBit(out, 0, length, bit);
        return out;
    }

    // in-place setBit mutating 'ints'
    static boolean mSetBit(final int[] ints, final int bit) {
        return mSetBit(ints, 0, ints.length, bit);
    }

    static boolean mSetBit(final int[] ints, final int offset, final int length, final int bit) {
        if (bit < 0 || bit >= length * 32) return true;
        ints[offset + (bit >>> 5)] |= (1 << (bit & 31));
        return false;
    }

    // immutable clearBit: copies the operand and applies mutable mClearBit
    static int[] clearBit(final int[] a, final int bit) {
        return clearBit(a, 0, a.length, bit);
    }

    static int[] clearBit(final int[] a, final int offset, final int length, final int bit) {
        int[] out = new int[length];
        System.arraycopy(a, offset, out, 0, length);
        mClearBit(out, 0, length, bit);
        return out;
    }

    // in-place clearBit mutating 'ints'
    static boolean mClearBit(final int[] ints, final int bit) {
        return mClearBit(ints, 0, ints.length, bit);
    }

    static boolean mClearBit(final int[] ints, final int offset, final int length, final int bit) {
        if (bit < 0 || bit >= length * 32) return true;
        ints[offset + (bit >>> 5)] &= ~(1 << (bit & 31));
        return false;
    }

    // immutable flipBit: copies the operand and applies mutable mFlipBit
    static int[] flipBit(final int[] a, final int bit) {
        return flipBit(a, 0, a.length, bit);
    }

    static int[] flipBit(final int[] a, final int offset, final int length, final int bit) {
        int[] out = new int[length];
        System.arraycopy(a, offset, out, 0, length);
        mFlipBit(out, 0, length, bit);
        return out;
    }

    // in-place flipBit mutating 'ints'
    static boolean mFlipBit(final int[] ints, final int bit) {
        return mFlipBit(ints, 0, ints.length, bit);
    }

    static boolean mFlipBit(final int[] ints, final int offset, final int length, final int bit) {
        if (bit < 0 || bit >= length * 32) return true;
        ints[offset + (bit >>> 5)] ^= (1 << (bit & 31));
        return false;
    }

    // =========================================================================
    // Bit Shifts
    // =========================================================================

    // immutable lshift: copies the operand to target width and applies mutable mShiftLeft
    static int[] lshift(final int[] a, final int n) {
        return lshift(a, 0, a.length, n);
    }

    static int[] lshift(final int[] a, final int offset, final int length, final int n) {
        int[] out = new int[length];
        System.arraycopy(a, offset, out, 0, length);
        mShiftLeft(out, 0, length, n);
        return out;
    }

    // in-place lshift mutating 'ints'
    static boolean mShiftLeft(final int[] ints, final int places) {
        return mShiftLeft(ints, 0, ints.length, places);
    }

    static boolean mShiftLeft(final int[] ints, final int offset, final int length, final int places) {
        if (places < 0) {
            return mShiftRight(ints, offset, length, -places);
        }
        if (places == 0) {
            return false;
        }
        if (places >= length * 32) {
            boolean overflow = !isZero(ints, offset, length);
            java.util.Arrays.fill(ints, offset, offset + length, 0);
            return overflow;
        }
        int words = places >>> 5;
        int bits = places & 31;
        boolean overflow = false;
        if (words > 0) {
            for (int i = length - words; i < length; i++) {
                if (ints[offset + i] != 0) {
                    overflow = true;
                }
            }
        }
        if (bits > 0 && words < length && (ints[offset + length - 1 - words] >>> (32 - bits)) != 0) {
            overflow = true;
        }
        if (bits == 0) {
            for (int i = length - 1; i >= words; i--) {
                ints[offset + i] = ints[offset + i - words];
            }
        } else {
            final int invShift = 32 - bits;
            for (int i = length - 1; i > words; i--) {
                ints[offset + i] = (ints[offset + i - words] << bits) | (ints[offset + i - words - 1] >>> invShift);
            }
            ints[offset + words] = ints[offset] << bits;
        }
        for (int i = 0; i < words; i++) {
            ints[offset + i] = 0;
        }
        return overflow;
    }

    // immutable rshift: copies the operand to target width and applies mutable mShiftRight
    static int[] rshift(final int[] a, final int n) {
        return rshift(a, 0, a.length, n);
    }

    static int[] rshift(final int[] a, final int offset, final int length, final int n) {
        int[] out = new int[length];
        System.arraycopy(a, offset, out, 0, length);
        mShiftRight(out, 0, length, n);
        return out;
    }

    // in-place rshift mutating 'ints'
    static boolean mShiftRight(final int[] ints, final int places) {
        return mShiftRight(ints, 0, ints.length, places);
    }

    static boolean mShiftRight(final int[] ints, final int offset, final int length, final int places) {
        if (places < 0) {
            return mShiftLeft(ints, offset, length, -places);
        }
        if (places == 0) {
            return false;
        }
        if (places >= length * 32) {
            boolean overflow = !isZero(ints, offset, length);
            java.util.Arrays.fill(ints, offset, offset + length, 0);
            return overflow;
        }
        int words = places >>> 5;
        int bits = places & 31;
        boolean overflow = false;
        if (words > 0) {
            for (int i = 0; i < words; i++) {
                if (ints[offset + i] != 0) {
                    overflow = true;
                }
            }
        }
        if (bits > 0 && words < length && (ints[offset + words] & ((1 << bits) - 1)) != 0) {
            overflow = true;
        }
        if (bits == 0) {
            for (int i = 0; i < length - words; i++) {
                ints[offset + i] = ints[offset + i + words];
            }
        } else {
            final int invShift = 32 - bits;
            for (int i = 0; i < length - words - 1; i++) {
                ints[offset + i] = (ints[offset + i + words] >>> bits) | (ints[offset + i + words + 1] << invShift);
            }
            ints[offset + length - 1 - words] = ints[offset + length - 1] >>> bits;
        }
        for (int i = length - words; i < length; i++) {
            ints[offset + i] = 0;
        }
        return overflow;
    }

    // =========================================================================
    // Basic Arithmetic Operations
    // =========================================================================

    // immutable inc: copies the operand to target width and applies mutable mInc
    static int[] inc(final int[] a) {
        return inc(a, 0, a.length);
    }

    static int[] inc(final int[] a, final int offset, final int length) {
        int[] out = new int[length];
        System.arraycopy(a, offset, out, 0, length);
        mInc(out, 0, length);
        return out;
    }

    // in-place inc mutating 'ints'
    static boolean mInc(final int[] ints) {
        return mInc(ints, 0, ints.length);
    }

    static boolean mInc(final int[] ints, final int offset, final int length) {
        long carry = 1;
        for (int i = 0; i < length && carry != 0; i++) {
            long sum = (ints[offset + i] & LONG) + carry;
            ints[offset + i] = (int) sum;
            carry = sum >>> 32;
        }
        return carry != 0;
    }

    // immutable dec: copies the operand and applies mutable mDec
    static int[] dec(final int[] a) {
        return dec(a, 0, a.length);
    }

    static int[] dec(final int[] a, final int offset, final int length) {
        int[] out = new int[length];
        System.arraycopy(a, offset, out, 0, length);
        mDec(out, 0, length);
        return out;
    }

    // in-place dec mutating 'ints'
    static boolean mDec(final int[] ints) {
        return mDec(ints, 0, ints.length);
    }

    static boolean mDec(final int[] ints, final int offset, final int length) {
        long borrow = 1;
        for (int i = 0; i < length && borrow != 0; i++) {
            long diff = (ints[offset + i] & LONG) - borrow;
            ints[offset + i] = (int) diff;
            borrow = (diff >> 32) != 0 ? 1 : 0;
        }
        return borrow != 0;
    }

    // immutable addition: copies the longer operand to target width and applies mutable mAdd
    static int[] add(final int[] a, final int[] b) {
        return add(a, 0, a.length, b, 0, b.length);
    }

    static int[] add(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength) {
        int[] out = new int[aLength];
        System.arraycopy(a, aOffset, out, 0, aLength);
        mAdd(out, 0, aLength, b, bOffset, bLength);
        return out;
    }

    // in-place addition mutating 'ints'
    static boolean mAdd(final int[] ints, final int[] other) {
        return mAdd(ints, 0, ints.length, other, 0, other.length);
    }

    static boolean mAdd(final int[] ints, final int offset, final int length, final int[] other, final int otherOffset, final int otherLength) {
        long carry = 0;
        for (int i = 0; i < length; i++) {
            long aVal = ints[offset + i] & LONG;
            long bVal = i < otherLength ? (other[otherOffset + i] & LONG) : 0L;
            long sum = aVal + bVal + carry;
            ints[offset + i] = (int) sum;
            carry = sum >>> 32;
        }
        boolean overflow = carry != 0;
        if (!overflow && otherLength > length) {
            for (int i = length; i < otherLength; i++) {
                if (other[otherOffset + i] != 0) {
                    overflow = true;
                    break;
                }
            }
        }
        return overflow;
    }

    // immutable subtraction: copies the first operand and applies mutable mSubtract
    static int[] sub(final int[] a, final int[] b) {
        return sub(a, 0, a.length, b, 0, b.length);
    }

    static int[] sub(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength) {
        int[] out = new int[aLength];
        System.arraycopy(a, aOffset, out, 0, aLength);
        mSubtract(out, 0, aLength, b, bOffset, bLength);
        return out;
    }

    // in-place subtraction mutating 'ints'
    static boolean mSubtract(final int[] ints, final int[] other) {
        return mSubtract(ints, 0, ints.length, other, 0, other.length);
    }

    static boolean mSubtract(final int[] ints, final int offset, final int length, final int[] other, final int otherOffset, final int otherLength) {
        long borrow = 0;
        for (int i = 0; i < length; i++) {
            long aVal = ints[offset + i] & LONG;
            long bVal = i < otherLength ? (other[otherOffset + i] & LONG) : 0L;
            long diff = aVal - bVal - borrow;
            ints[offset + i] = (int) diff;
            borrow = (diff >> 32) != 0 ? 1 : 0;
        }
        boolean overflow = borrow != 0;
        if (!overflow && otherLength > length) {
            for (int i = length; i < otherLength; i++) {
                if (other[otherOffset + i] != 0) {
                    overflow = true;
                    break;
                }
            }
        }
        return overflow;
    }

    // immutable subgt: copies the first operand and applies mutable mSubgt
    static int[] subgt(final int[] a, final int[] b) {
        return subgt(a, 0, a.length, b, 0, b.length);
    }

    static int[] subgt(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength) {
        int[] out = new int[aLength];
        System.arraycopy(a, aOffset, out, 0, aLength);
        mSubgt(out, 0, aLength, b, bOffset, bLength);
        return out;
    }

    // in-place subgt mutating 'ints'
    static boolean mSubgt(final int[] ints, final int[] other) {
        return mSubgt(ints, 0, ints.length, other, 0, other.length);
    }

    static boolean mSubgt(final int[] ints, final int offset, final int length, final int[] other, final int otherOffset, final int otherLength) {
        Scratchpad pad = SCRATCH.get();
        int[] temp = pad.a;
        java.util.Arrays.fill(temp, 0);

        if (length == 0) {
            System.arraycopy(other, otherOffset, temp, 0, Math.min(otherLength, temp.length));
            mNot(temp, 0, temp.length);
            mInc(temp, 0, temp.length);
        } else {
            System.arraycopy(other, otherOffset, temp, 0, Math.min(otherLength, temp.length));
            mSubtract(temp, 0, temp.length, ints, offset, length);
            mNot(temp, 0, temp.length);
            mInc(temp, 0, temp.length);
        }

        System.arraycopy(temp, 0, ints, offset, length);
        return true;
    }

    // =========================================================================
    // Multiplication
    // =========================================================================

    static int[] multiply(int[] a, int[] b) {
        return multiply(a, b, new boolean[1]);
    }

    static int[] multiply(int[] a, int[] b, boolean[] overflowHolder) {
        return multiply(a, 0, a.length, b, 0, b.length, overflowHolder);
    }

    static int[] multiply(int[] a, int aOffset, int aLength, int[] b, int bOffset, int bLength, boolean[] overflowHolder) {
        int[] out = new int[aLength];
        System.arraycopy(a, aOffset, out, 0, aLength);
        overflowHolder[0] = mMultiply(out, 0, aLength, b, bOffset, bLength);
        return out;
    }

    //  uses grammar school multiplication - for our data width (up to 256)  other methods
    //  are slower
    static boolean mMultiply(final int[] ints, final int[] other) {
        return mMultiply(ints, 0, ints.length, other, 0, other.length);
    }

    static boolean mMultiply(final int[] ints, final int offset, final int length, final int[] other, final int otherOffset, final int otherLength) {
        int maxWidth = length;
        Scratchpad pad = SCRATCH.get();
        int[] product = pad.product;
        java.util.Arrays.fill(product, 0, maxWidth, 0);

        boolean overflow = false;
        for (int i = 0; i < length; i++) {
            long aVal = ints[offset + i] & LONG;
            if (aVal == 0) continue;
            long carry = 0;
            for (int j = 0; j < otherLength; j++) {
                int targetIdx = i + j;
                if (targetIdx < maxWidth) {
                    long prod = aVal * (other[otherOffset + j] & LONG) + (product[targetIdx] & LONG) + carry;
                    product[targetIdx] = (int) prod;
                    carry = prod >>> 32;
                } else {
                    if (carry != 0) {
                        overflow = true;
                        carry = 0;
                    }
                    if (other[otherOffset + j] != 0) {
                        overflow = true;
                    }
                }
            }
            int carryIdx = i + otherLength;
            while (carry != 0) {
                if (carryIdx < maxWidth) {
                    long sum = (product[carryIdx] & LONG) + carry;
                    product[carryIdx] = (int) sum;
                    carry = sum >>> 32;
                    carryIdx++;
                } else {
                    overflow = true;
                    break;
                }
            }
        }

        System.arraycopy(product, 0, ints, offset, maxWidth);
        return overflow;
    }

    // =========================================================================
    // Division, Modulo, and Quotient-Remainder
    // =========================================================================

    static int[] divide(final int[] a, final int[] b) {
        return divide(a, 0, a.length, b, 0, b.length);
    }

    static int[] divide(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength) {
        int[] out = new int[aLength];
        System.arraycopy(a, aOffset, out, 0, aLength);
        mDivide(out, 0, aLength, b, bOffset, bLength);
        return out;
    }

    static boolean mDivide(final int[] ints, final int[] other) {
        return mDivide(ints, 0, ints.length, other, 0, other.length);
    }

    static boolean mDivide(final int[] ints, final int offset, final int length, final int[] other, final int otherOffset, final int otherLength) {
        if (isZero(other, otherOffset, otherLength)) throw new ArithmeticException("divide by zero");
        if (isZero(ints, offset, length)) {
            return false;
        }
        int cmp = compareActive(ints, offset, length, other, otherOffset, otherLength);
        if (cmp < 0) {
            java.util.Arrays.fill(ints, offset, offset + length, 0);
            return false;
        }
        if (cmp == 0) {
            java.util.Arrays.fill(ints, offset, offset + length, 0);
            ints[offset] = 1;
            return false;
        }

        Scratchpad pad = SCRATCH.get();
        java.util.Arrays.fill(pad.quo, 0);
        java.util.Arrays.fill(pad.rem, 0);
        java.util.Arrays.fill(pad.d, 0);

        int otherEnd = otherLength - 1;
        while (otherEnd >= 0 && other[otherOffset + otherEnd] == 0) {
            otherEnd--;
        }
        int otherActiveLen = otherEnd + 1;
        System.arraycopy(other, otherOffset, pad.d, 0, otherActiveLen);

        int qints;
        int[] tempInts = pad.divA;
        java.util.Arrays.fill(tempInts, 0);
        System.arraycopy(ints, offset, tempInts, 0, length);

        if (otherActiveLen == 1) {
            Division.div(tempInts, length, pad.d[0], pad.quo, pad.rem);
            qints = length;
        } else if (otherActiveLen == 2) {
            final long divisor = ((pad.d[1] & LONG) << 32) | (pad.d[0] & LONG);
            Division.div(tempInts, length, divisor, pad.quo, pad.divRem);
            qints = length - 1;
        } else {
            Division.div(tempInts, length, pad.d, otherActiveLen, pad.quo, pad.divRem, pad.divScr);
            int places = Integer.numberOfLeadingZeros(pad.d[otherActiveLen - 1]);
            int remLen = length + 1;
            if (0 < places && places > Integer.numberOfLeadingZeros(tempInts[length - 1])) {
                remLen = length + 2;
            }
            qints = remLen - otherActiveLen;
        }

        java.util.Arrays.fill(ints, offset, offset + length, 0);
        int copyLen = Math.min(qints, length);
        System.arraycopy(pad.quo, 0, ints, offset, copyLen);
        return false;
    }

    static int[] mod(final int[] a, final int[] b) {
        return mod(a, 0, a.length, b, 0, b.length);
    }

    static int[] mod(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength) {
        int[] out = new int[aLength];
        System.arraycopy(a, aOffset, out, 0, aLength);
        mMod(out, 0, aLength, b, bOffset, bLength);
        return out;
    }

    static boolean mMod(final int[] ints, final int[] other) {
        return mMod(ints, 0, ints.length, other, 0, other.length);
    }

    static boolean mMod(final int[] ints, final int offset, final int length, final int[] other, final int otherOffset, final int otherLength) {
        if (isZero(other, otherOffset, otherLength)) throw new ArithmeticException("modulo by zero");
        mModInPlace(ints, offset, length, other, otherOffset, otherLength);
        return false;
    }

    /**
     * Calculates both division quotient and remainder simultaneously (immutable version).
     * Copies the dividend array and delegates to the mutable {@code mDivmod}.
     *
     * @param a the dividend array
     * @param b the divisor value as a long
     * @return a two-dimensional array where index 0 is the quotient and index 1 is the remainder
     */
    static int[][] divmod(final int[] a, final long b) {
        return divmod(a, 0, a.length, b);
    }

    static int[][] divmod(final int[] a, final int offset, final int length, final long b) {
        return divmod(a, offset, length, valueOf(b, length), 0, length);
    }

    /**
     * Calculates both division quotient and remainder simultaneously (immutable version).
     * Copies the dividend array and delegates to the mutable {@code mDivmod}.
     *
     * @param a the dividend array
     * @param b the divisor array
     * @return a two-dimensional array where index 0 is the quotient and index 1 is the remainder
     */
    static int[][] divmod(final int[] a, final int[] b) {
        return divmod(a, 0, a.length, b, 0, b.length);
    }

    static int[][] divmod(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength) {
        int[] q = new int[aLength];
        int[] r = new int[aLength];
        System.arraycopy(a, aOffset, q, 0, aLength);
        System.arraycopy(a, aOffset, r, 0, aLength);
        mDivmod(q, 0, aLength, r, 0, aLength, b, bOffset, bLength);
        return new int[][]{q, r};
    }

    /**
     * Performs simultaneous division and modulo in-place (mutable version).
     * Delegates to the array-based version of {@code mDivmod} using valueOf(b).
     *
     * @param q target quotient array to be modified in-place
     * @param r target remainder array to be modified in-place
     * @param b the divisor value as a long
     * @return false (overflow flag not used)
     */
    static boolean mDivmod(final int[] q, final int[] r, final long b) {
        return mDivmod(q, 0, q.length, r, 0, r.length, b);
    }

    static boolean mDivmod(final int[] q, final int qOffset, final int qLength, final int[] r, final int rOffset, final int rLength, final long b) {
        return mDivmod(q, qOffset, qLength, r, rOffset, rLength, valueOf(b, qLength), 0, qLength);
    }

    /**
     * Performs simultaneous division and modulo in-place (mutable version).
     * Modifies the supplied {@code q} and {@code r} arrays to store the quotient and remainder.
     *
     * @param q target quotient array to be modified in-place
     * @param r target remainder array to be modified in-place
     * @param b the divisor array
     * @return false (overflow flag not used)
     */
    static boolean mDivmod(final int[] q, final int[] r, final int[] b) {
        return mDivmod(q, 0, q.length, r, 0, r.length, b, 0, b.length);
    }

    static boolean mDivmod(final int[] q, final int qOffset, final int qLength, final int[] r, final int rOffset, final int rLength, final int[] b, final int bOffset, final int bLength) {
        mDivide(q, qOffset, qLength, b, bOffset, bLength);
        mMod(r, rOffset, rLength, b, bOffset, bLength);
        return false;
    }

    static void mModInPlace(final int[] val, final int[] mod) {
        mModInPlace(val, 0, val.length, mod, 0, mod.length);
    }

    static void mModInPlace(final int[] val, final int valOffset, final int valLength, final int[] mod, final int modOffset, final int modLength) {
        if (isZero(val, valOffset, valLength)) {
            return;
        }
        int cmp = compareActive(val, valOffset, valLength, mod, modOffset, modLength);
        if (cmp < 0) {
            return;
        }
        if (cmp == 0) {
            java.util.Arrays.fill(val, valOffset, valOffset + valLength, 0);
            return;
        }

        int modEnd = modLength - 1;
        while (modEnd >= 0 && mod[modOffset + modEnd] == 0) {
            modEnd--;
        }
        int modActiveLen = modEnd + 1;
        if (modActiveLen == 0) {
            throw new ArithmeticException("modulo by zero");
        }

        int valEnd = valLength - 1;
        while (valEnd >= 0 && val[valOffset + valEnd] == 0) {
            valEnd--;
        }
        int activeLen = valEnd + 1;
        if (activeLen == 0) {
            java.util.Arrays.fill(val, valOffset, valOffset + valLength, 0);
            return;
        }

        Scratchpad pad = SCRATCH.get();
        java.util.Arrays.fill(pad.a, 0);
        System.arraycopy(val, valOffset, pad.a, 0, activeLen);

        java.util.Arrays.fill(pad.d, 0);
        System.arraycopy(mod, modOffset, pad.d, 0, modActiveLen);

        java.util.Arrays.fill(pad.quo, 0);
        java.util.Arrays.fill(pad.rem, 0);

        if (modActiveLen == 1) {
            Division.div(pad.a, activeLen, pad.d[0], pad.quo, pad.rem);
            java.util.Arrays.fill(val, valOffset, valOffset + valLength, 0);
            val[valOffset] = pad.rem[0];
        } else if (modActiveLen == 2) {
            final long divisor = ((pad.d[1] & LONG) << 32) | (pad.d[0] & LONG);
            Division.div(pad.a, activeLen, divisor, pad.quo, pad.divRem);
            java.util.Arrays.fill(val, valOffset, valOffset + valLength, 0);
            int copyLimit = Math.min(2, valLength);
            System.arraycopy(pad.divRem, 0, val, valOffset, copyLimit);
        } else {
            Division.div(pad.a, activeLen, pad.d, modActiveLen, pad.quo, pad.divRem, pad.divScr);
            java.util.Arrays.fill(val, valOffset, valOffset + valLength, 0);
            int copyLimit = Math.min(modActiveLen, valLength);
            System.arraycopy(pad.divRem, 0, val, valOffset, copyLimit);
        }
    }

    // =========================================================================
    // Modular Arithmetic
    // =========================================================================

    // immutable addition modulo: copies the first operand to target width and applies mutable mAddMod
    static int[] addmod(final int[] a, final int[] b, final int[] c) {
        return addmod(a, 0, a.length, b, 0, b.length, c, 0, c.length);
    }

    static int[] addmod(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength, final int[] c, final int cOffset, final int cLength) {
        int[] out = new int[cLength];
        System.arraycopy(a, aOffset, out, 0, Math.min(aLength, cLength));
        mAddMod(out, 0, out.length, b, bOffset, bLength, c, cOffset, cLength);
        return out;
    }

    // in-place addition modulo mutating 'ints'
    static boolean mAddMod(final int[] ints, final int[] add, final int[] mod) {
        return mAddMod(ints, 0, ints.length, add, 0, add.length, mod, 0, mod.length);
    }

    static boolean mAddMod(final int[] ints, final int offset, final int length, final int[] add, final int addOffset, final int addLength, final int[] mod, final int modOffset, final int modLength) {
        if (isZero(mod, modOffset, modLength)) throw new ArithmeticException("modulo by zero");

        mModInPlace(ints, offset, length, mod, modOffset, modLength);

        Scratchpad pad = SCRATCH.get();
        int[] tempAdd = pad.tempAdd;
        java.util.Arrays.fill(tempAdd, 0);
        System.arraycopy(add, addOffset, tempAdd, 0, addLength);
        mModInPlace(tempAdd, 0, tempAdd.length, mod, modOffset, modLength);

        boolean carry = mAdd(ints, offset, length, tempAdd, 0, tempAdd.length);
        if (carry || compareActive(ints, offset, length, mod, modOffset, modLength) >= 0) {
            mSubtract(ints, offset, length, mod, modOffset, modLength);
        }
        return false;
    }

    static int[] mulmod(final int[] a, final int[] b, final int[] c) {
        return mulmod(a, 0, a.length, b, 0, b.length, c, 0, c.length);
    }

    static int[] mulmod(final int[] a, final int aOffset, final int aLength, final int[] b, final int bOffset, final int bLength, final int[] c, final int cOffset, final int cLength) {
        int[] out = new int[cLength];
        System.arraycopy(a, aOffset, out, 0, Math.min(aLength, cLength));
        mMulMod(out, 0, out.length, b, bOffset, bLength, c, cOffset, cLength);
        return out;
    }

    static boolean mMulMod(final int[] ints, final int[] mul, final int[] mod) {
        return mMulMod(ints, 0, ints.length, mul, 0, mul.length, mod, 0, mod.length);
    }

    static boolean mMulMod(final int[] ints, final int offset, final int length, final int[] mul, final int mulOffset, final int mulLength, final int[] mod, final int modOffset, final int modLength) {
        if (isZero(mod, modOffset, modLength)) throw new ArithmeticException("modulo by zero");

        mModInPlace(ints, offset, length, mod, modOffset, modLength);

        Scratchpad pad = SCRATCH.get();
        int[] tempMul = pad.tempMul;
        java.util.Arrays.fill(tempMul, 0);
        System.arraycopy(mul, mulOffset, tempMul, 0, mulLength);
        mModInPlace(tempMul, 0, tempMul.length, mod, modOffset, modLength);

        int[] tempRes;
        if (length == 4) {
            tempRes = pad.tempRes8;
        } else  {
            tempRes = pad.tempRes16;
        }
        java.util.Arrays.fill(tempRes, 0);
        System.arraycopy(ints, offset, tempRes, 0, length);

        mMultiply(tempRes, 0, tempRes.length, tempMul, 0, tempMul.length);

        mModInPlace(tempRes, 0, tempRes.length, mod, modOffset, modLength);

        java.util.Arrays.fill(ints, offset, offset + length, 0);
        System.arraycopy(tempRes, 0, ints, offset, length);
        return false;
    }

    // =========================================================================
    // Powers, Squaring, and Roots
    // =========================================================================

    static int[] square(final int[] a, boolean[] overflowHolder) {
        return square(a, 0, a.length, overflowHolder);
    }

    static int[] square(final int[] a, final int offset, final int length, boolean[] overflowHolder) {
        int[] padded = new int[length];
        System.arraycopy(a, offset, padded, 0, length);
        return multiply(padded, 0, length, padded, 0, length, overflowHolder);
    }

    static int[] square(final int[] a) {
        return square(a, 0, a.length);
    }

    static int[] square(final int[] a, final int offset, final int length) {
        return square(a, offset, length, new boolean[1]);
    }

    static int getLowestSetBit(final int[] ints) {
        return getLowestSetBit(ints, 0, ints.length);
    }

    static int getLowestSetBit(final int[] ints, final int offset, final int length) {
        for (int i = 0; i < length; i++) {
            if (ints[offset + i] != 0) {
                return i * 32 + Integer.numberOfTrailingZeros(ints[offset + i]);
            }
        }
        return -1;
    }

    static int[] pow(int[] a, int exp) {
        return pow(a, exp, new boolean[1]);
    }

    static int[] pow(int[] a, int exp, boolean[] overflowHolder) {
        return pow(a, 0, a.length, exp, overflowHolder);
    }

    static int[] pow(int[] a, int offset, int length, int exp, boolean[] overflowHolder) {
        int[] out = new int[length];
        System.arraycopy(a, offset, out, 0, length);
        overflowHolder[0] = mPow(out, 0, length, exp);
        return out;
    }

    static boolean mPow(final int[] ints, final int exp) {
        return mPow(ints, 0, ints.length, exp);
    }

    static boolean mPow(final int[] ints, final int offset, final int length, final int exp) {
        if (exp < 0) throw new ArithmeticException("Negative exponent");
        if (exp == 0) {
            java.util.Arrays.fill(ints, offset, offset + length, 0);
            ints[offset] = 1;
            return false;
        }
        if (isZero(ints, offset, length)) {
            return false;
        }
        if (length == 1 && ints[offset] == 1) {
            return false;
        }

        final int maxWidth = length;
        final int lo = getLowestSetBit(ints, offset, length);

        Scratchpad pad = SCRATCH.get();
        int[] a;
        int[] out;
        int[] tempC;
        int[] tempD;
        int[] res;

        if (maxWidth == 4) {
            a = pad.a4;
            out = pad.b4;
            tempC = pad.c4;
            tempD = pad.d4;
            res = pad.r4;
        } else if (maxWidth == 8) {
            a = pad.a8;
            out = pad.b8;
            tempC = pad.c8;
            tempD = pad.d8;
            res = pad.r8;
        } else {
            a = new int[maxWidth];
            out = new int[maxWidth];
            tempC = new int[maxWidth];
            tempD = new int[maxWidth];
            res = new int[maxWidth];
        }

        java.util.Arrays.fill(a, 0);
        System.arraycopy(ints, offset, a, 0, maxWidth);

        boolean overflowVal = false;

        int[] resultArr;
        if (exp == 2) {
            java.util.Arrays.fill(res, 0);
            System.arraycopy(a, 0, res, 0, maxWidth);
            overflowVal = mMultiply(res, 0, res.length, res, 0, res.length);
            resultArr = res;
        } else {
            long shift = (long) lo * exp;
            if (Integer.MAX_VALUE < shift) {
                overflowVal = true;
                java.util.Arrays.fill(res, 0);
                resultArr = res;
            } else {
                if (0 < lo) {
                    mShiftRight(a, 0, a.length, lo);
                }

                final int bits = bitLength(a, 0, a.length);
                if (bits == 1) {
                    boolean overflow = (lo * exp >= maxWidth * 32);
                    overflowVal = overflow;
                    java.util.Arrays.fill(res, 0);
                    res[0] = 1;
                    if (0 < lo) {
                        mShiftLeft(res, 0, res.length, lo * exp);
                    }
                    resultArr = res;
                } else {
                    long scale = (long) bits * exp;

                    if (maxWidth == 1 && scale < 63) {
                        long outVal = 1, base = a[0] & LONG;

                        int e = exp;
                        while (e != 0) {
                            if ((e & 1) == 1)
                                outVal *= base;
                            if ((e >>>= 1) != 0)
                                base *= base;
                        }

                        java.util.Arrays.fill(res, 0);
                        if (0 < lo) {
                            int outBits = 64 - Long.numberOfLeadingZeros(outVal);
                            boolean overflow = (shift >= maxWidth * 32) || (outBits + shift > maxWidth * 32);
                            overflowVal = overflow;
                            if ((shift + scale) < 63) {
                                long val = outVal << shift;
                                res[0] = (int) val;
                                if (maxWidth > 1) {
                                    res[1] = (int) (val >>> 32);
                                }
                            } else {
                                res[0] = (int) outVal;
                                mShiftLeft(res, 0, res.length, (int) shift);
                            }
                        } else {
                            int outBits = 64 - Long.numberOfLeadingZeros(outVal);
                            boolean overflow = (outBits > maxWidth * 32);
                            overflowVal = overflow;
                            res[0] = (int) outVal;
                        }
                        resultArr = res;
                    } else {
                        final int lplaces = lo * exp;
                        java.util.Arrays.fill(out, 0);
                        out[0] = 1;

                        boolean overflow = false;
                        int e = exp;
                        while (e != 0) {
                            if ((e & 1) == 1) {
                                java.util.Arrays.fill(tempD, 0);
                                System.arraycopy(out, 0, tempD, 0, maxWidth);
                                boolean stepOverflow = mMultiply(tempD, 0, tempD.length, a, 0, a.length);
                                if (stepOverflow) {
                                    overflow = true;
                                }
                                System.arraycopy(tempD, 0, out, 0, maxWidth);
                            }
                            if ((e >>>= 1) != 0) {
                                java.util.Arrays.fill(tempC, 0);
                                System.arraycopy(a, 0, tempC, 0, maxWidth);
                                boolean stepOverflow = mMultiply(tempC, 0, tempC.length, tempC, 0, tempC.length);
                                if (stepOverflow) {
                                    overflow = true;
                                }
                                System.arraycopy(tempC, 0, a, 0, maxWidth);
                            }
                        }

                        if (0 < lplaces) {
                            if (!isZero(out, 0, out.length) && (lplaces >= maxWidth * 32 || bitLength(out, 0, out.length) + lplaces > maxWidth * 32)) {
                                overflow = true;
                            }
                            mShiftLeft(out, 0, out.length, lplaces);
                        }
                        overflowVal = overflow;
                        resultArr = out;
                    }
                }
            }
        }

        java.util.Arrays.fill(ints, offset, offset + maxWidth, 0);
        System.arraycopy(resultArr, 0, ints, offset, maxWidth);
        return overflowVal;
    }

    static int[] sqrt(final int[] a) {
        return sqrt(a, 0, a.length);
    }

    static int[] sqrt(final int[] a, final int offset, final int length) {
        int[] out = new int[length];
        System.arraycopy(a, offset, out, 0, length);
        mSqrt(out, 0, length);
        return out;
    }

    static boolean mSqrt(final int[] ints) {
        return mSqrt(ints, 0, ints.length);
    }

    static boolean mSqrt(final int[] ints, final int offset, final int length) {
        if (isZero(ints, offset, length)) {
            return false;
        }

        Scratchpad pad = SCRATCH.get();
        int[] op = pad.a;
        int[] nextY = pad.b;

        java.util.Arrays.fill(op, 0);
        java.util.Arrays.fill(nextY, 0);

        int targetWidth = length;
        System.arraycopy(ints, offset, op, 0, targetWidth);

        int bit = bitLength(op, 0, targetWidth);
        if (bit == 0) {
            java.util.Arrays.fill(ints, offset, offset + length, 0);
            return false;
        }

        java.util.Arrays.fill(ints, offset, offset + length, 0);
        mSetBit(ints, offset, length, (bit + 1) / 2);

        while (true) {
            System.arraycopy(op, 0, nextY, 0, targetWidth);
            mDivide(nextY, 0, targetWidth, ints, offset, length);
            mAdd(nextY, 0, targetWidth, ints, offset, length);
            mShiftRight(nextY, 0, targetWidth, 1);

            if (compareActive(nextY, 0, targetWidth, ints, offset, length) >= 0) {
                break;
            }
            System.arraycopy(nextY, 0, ints, offset, targetWidth);
        }

        return false;
    }
}
