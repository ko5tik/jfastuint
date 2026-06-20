package io.nervous.juint;

import java.math.BigInteger;

import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.copyOf;

/**
 * These methods don't mutate their arguments or return arrays w/ leading zeroes.
 */
final class Arrays {
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

    static final class Scratchpad {
        final int[] a = new int[128];
        final int[] b = new int[128];
        final int[] c = new int[128];
        final int[] d = new int[128];
        final int[] quo = new int[128];
        final int[] rem = new int[128];
        final int[] div = new int[128];
        final int[] tempAdd = new int[128];
        final int[] tempMul = new int[128];
        final int[] product = new int[128];
        final int[] productClean = new int[128];
    }

    static final ThreadLocal<Scratchpad> SCRATCH = ThreadLocal.withInitial(Scratchpad::new);

    static int[] valueOf(final long v) {
        if (0 <= v && v < MAX_CACHE) {
            return CACHE[(int) v];
        }

        final int hi = (int) (v >>> 32);
        return hi == 0 ? new int[]{(int) v} : new int[]{(int) v, hi};
    }

    static int compare(final int[] ints, final int[] other) {
        final int len = ints.length;
        if (len < other.length) {
            return -1;
        }
        if (len > other.length) {
            return 1;
        }

        for (int i = len - 1; i >= 0; i--) {
            if (ints[i] != other[i]) {
                return Integer.compareUnsigned(ints[i], other[i]);
            }
        }

        return 0;
    }

    static int compareActive(final int[] a, final int[] b) {
        int aLen = a.length;
        while (aLen > 0 && a[aLen - 1] == 0) {
            aLen--;
        }
        int bLen = b.length;
        while (bLen > 0 && b[bLen - 1] == 0) {
            bLen--;
        }
        if (aLen < bLen) {
            return -1;
        }
        if (aLen > bLen) {
            return 1;
        }
        for (int i = aLen - 1; i >= 0; i--) {
            int aVal = a[i];
            int bVal = b[i];
            if (aVal != bVal) {
                return Integer.compareUnsigned(aVal, bVal);
            }
        }
        return 0;
    }

    static int compare(final int[] ints, final BigInteger other, final int maxWidth) {
        final int il = bitLength(ints), bl = other.bitLength();
        if (il < bl) {
            return -1;
        }
        if (il > bl) {
            return 1;
        }

        return compareActive(ints, from(other, maxWidth));
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
    // immutable version of not
    static int[] not(final int[] ints) {
        int[] out = copyOf(ints, ints.length);
        mNot(out);
        return out;
    }

    // in place version of not
    static boolean mNot(final int[] ints) {
        for (int i = 0; i < ints.length; i++) {
            ints[i] = ~ints[i];
        }
        return false;
    }
    // immutable bitwise and: copies the first operand and applies mutable mAnd
    static int[] and(final int[] a, final int[] b) {
        int[] out = copyOf(a, a.length);
        mAnd(out, b);
        return out;
    }

    // in-place bitwise and mutating the first operand 'ints'
    static boolean mAnd(final int[] ints, final int[] other) {
        int len = ints.length;
        int otherLen = other.length;
        for (int i = 0; i < len; i++) {
            ints[i] &= (i < otherLen ? other[i] : 0);
        }
        return false;
    }

    // immutable bitwise or: copies the first operand and applies mutable mOr
    static int[] or(final int[] a, final int[] b) {
        int[] out = copyOf(a, a.length);
        mOr(out, b);
        return out;
    }

    // in-place bitwise or mutating the first operand 'ints'
    static boolean mOr(final int[] ints, final int[] other) {
        int len = ints.length;
        int otherLen = other.length;
        for (int i = 0; i < len; i++) {
            ints[i] |= (i < otherLen ? other[i] : 0);
        }
        return false;
    }

    // immutable bitwise xor: copies the first operand and applies mutable mXor
    static int[] xor(final int[] a, final int[] b) {
        int[] out = copyOf(a, a.length);
        mXor(out, b);
        return out;
    }

    // in-place bitwise xor mutating the first operand 'ints'
    static boolean mXor(final int[] ints, final int[] other) {
        int len = ints.length;
        int otherLen = other.length;
        for (int i = 0; i < len; i++) {
            ints[i] ^= (i < otherLen ? other[i] : 0);
        }
        return false;
    }

    // immutable setBit: copies the operand and applies mutable mSetBit
    static int[] setBit(final int[] a, final int bit) {
        int[] out = copyOf(a, a.length);
        mSetBit(out, bit);
        return out;
    }

    // in-place setBit mutating 'ints'
    static boolean mSetBit(final int[] ints, final int bit) {
        int len = ints.length;
        if (bit < 0 || bit >= len * 32) return true;
        ints[bit >>> 5] |= (1 << (bit & 31));
        return false;
    }

    // immutable clearBit: copies the operand and applies mutable mClearBit
    static int[] clearBit(final int[] a, final int bit) {
        int[] out = copyOf(a, a.length);
        mClearBit(out, bit);
        return out;
    }

    // in-place clearBit mutating 'ints'
    static boolean mClearBit(final int[] ints, final int bit) {
        int len = ints.length;
        if (bit < 0 || bit >= len * 32) return true;
        ints[bit >>> 5] &= ~(1 << (bit & 31));
        return false;
    }

    // immutable flipBit: copies the operand and applies mutable mFlipBit
    static int[] flipBit(final int[] a, final int bit) {
        int[] out = copyOf(a, a.length);
        mFlipBit(out, bit);
        return out;
    }

    // in-place flipBit mutating 'ints'
    static boolean mFlipBit(final int[] ints, final int bit) {
        int len = ints.length;
        if (bit < 0 || bit >= len * 32) return true;
        ints[bit >>> 5] ^= (1 << (bit & 31));
        return false;
    }

    // immutable lshift: copies the operand to target width and applies mutable mShiftLeft
    static int[] lshift(final int[] a, final int n, final int maxWidth) {
        int targetWidth = maxWidth == -1 ? a.length + (n >>> 5) + ((n & 0x1f) != 0 ? 1 : 0) : maxWidth;
        int[] out = copyOf(a, targetWidth);
        mShiftLeft(out, n);
        return out;
    }

    // in-place lshift mutating 'ints'
    static boolean mShiftLeft(final int[] ints, final int places) {
        if (places < 0) {
            return mShiftRight(ints, -places);
        }
        if (places == 0) {
            return false;
        }
        int len = ints.length;
        if (places >= len * 32) {
            boolean overflow = !isZero(ints);
            java.util.Arrays.fill(ints, 0);
            return overflow;
        }
        int words = places >>> 5;
        int bits = places & 31;
        boolean overflow = false;
        if (words > 0) {
            for (int i = len - words; i < len; i++) {
                if (ints[i] != 0) {
                    overflow = true;
                }
            }
        }
        if (bits > 0 && words < len && (ints[len - 1 - words] >>> (32 - bits)) != 0) {
            overflow = true;
        }
        if (bits == 0) {
            for (int i = len - 1; i >= words; i--) {
                ints[i] = ints[i - words];
            }
        } else {
            final int invShift = 32 - bits;
            for (int i = len - 1; i > words; i--) {
                ints[i] = (ints[i - words] << bits) | (ints[i - words - 1] >>> invShift);
            }
            ints[words] = ints[0] << bits;
        }
        for (int i = 0; i < words; i++) {
            ints[i] = 0;
        }
        return overflow;
    }

    // immutable rshift: copies the operand to target width and applies mutable mShiftRight
    static int[] rshift(final int[] a, final int n, final int maxWidth) {
        int targetWidth = maxWidth == -1 ? a.length : maxWidth;
        int[] out = copyOf(a, targetWidth);
        mShiftRight(out, n);
        return out;
    }

    // in-place rshift mutating 'ints'
    static boolean mShiftRight(final int[] ints, final int places) {
        if (places < 0) {
            return mShiftLeft(ints, -places);
        }
        if (places == 0) {
            return false;
        }
        int len = ints.length;
        if (places >= len * 32) {
            boolean overflow = !isZero(ints);
            java.util.Arrays.fill(ints, 0);
            return overflow;
        }
        int words = places >>> 5;
        int bits = places & 31;
        boolean overflow = false;
        if (words > 0) {
            for (int i = 0; i < words; i++) {
                if (ints[i] != 0) {
                    overflow = true;
                }
            }
        }
        if (bits > 0 && words < len && (ints[words] & ((1 << bits) - 1)) != 0) {
            overflow = true;
        }
        if (bits == 0) {
            for (int i = 0; i < len - words; i++) {
                ints[i] = ints[i + words];
            }
        } else {
            final int invShift = 32 - bits;
            for (int i = 0; i < len - words - 1; i++) {
                ints[i] = (ints[i + words] >>> bits) | (ints[i + words + 1] << invShift);
            }
            ints[len - 1 - words] = ints[len - 1] >>> bits;
        }
        for (int i = len - words; i < len; i++) {
            ints[i] = 0;
        }
        return overflow;
    }

    // immutable inc: copies the operand to target width and applies mutable mInc
    static int[] inc(final int[] a, final int maxWidth) {
        int targetWidth = maxWidth == -1 ? a.length : maxWidth;
        int[] out = copyOf(a, targetWidth);
        mInc(out);
        return out;
    }

    // in-place inc mutating 'ints'
    static boolean mInc(final int[] ints) {
        long carry = 1;
        for (int i = 0; i < ints.length && carry != 0; i++) {
            long sum = (ints[i] & LONG) + carry;
            ints[i] = (int) sum;
            carry = sum >>> 32;
        }
        return carry != 0;
    }

    // immutable dec: copies the operand and applies mutable mDec
    static int[] dec(final int[] a) {
        int[] out = copyOf(a, a.length);
        mDec(out);
        return out;
    }

    // in-place dec mutating 'ints'
    static boolean mDec(final int[] ints) {
        long borrow = 1;
        for (int i = 0; i < ints.length && borrow != 0; i++) {
            long diff = (ints[i] & LONG) - borrow;
            ints[i] = (int) diff;
            borrow = (diff >> 32) != 0 ? 1 : 0;
        }
        return borrow != 0;
    }

    // immutable addition: copies the longer operand to target width and applies mutable mAdd
    static int[] add(int[] a, int[] b, final int maxWidth) {
        if (a.length < b.length) {
            int[] tmp = a;
            a = b;
            b = tmp;
        }
        int targetWidth = maxWidth == -1 ? a.length : maxWidth;
        int[] out = copyOf(a, targetWidth);
        boolean carry = mAdd(out, b);
        
        if (maxWidth == -1 && carry) {
            int[] grown = new int[targetWidth + 1];
            System.arraycopy(out, 0, grown, 0, targetWidth);
            grown[targetWidth] = 1;
            return grown;
        }
        return out;
    }

    // in-place addition mutating 'ints'
    static boolean mAdd(final int[] ints, final int[] other) {
        long carry = 0;
        int len = ints.length;
        int otherLen = other.length;
        for (int i = 0; i < len; i++) {
            long aVal = ints[i] & LONG;
            long bVal = i < otherLen ? (other[i] & LONG) : 0L;
            long sum = aVal + bVal + carry;
            ints[i] = (int) sum;
            carry = sum >>> 32;
        }
        boolean overflow = carry != 0;
        if (!overflow && otherLen > len) {
            for (int i = len; i < otherLen; i++) {
                if (other[i] != 0) {
                    overflow = true;
                    break;
                }
            }
        }
        return overflow;
    }

    // immutable subgt: copies the first operand and applies mutable mSubgt
    static int[] subgt(final int[] a, final int[] b) {
        int[] out = copyOf(a, a.length);
        mSubgt(out, b);
        return out;
    }

    // in-place subgt mutating 'ints'
    static boolean mSubgt(final int[] ints, final int[] other) {
        Scratchpad pad = SCRATCH.get();
        int[] temp = pad.a;
        java.util.Arrays.fill(temp, 0);

        if (ints.length == 0) {
            System.arraycopy(other, 0, temp, 0, Math.min(other.length, temp.length));
            mNot(temp);
            mInc(temp);
        } else {
            System.arraycopy(other, 0, temp, 0, Math.min(other.length, temp.length));
            mSubtract(temp, ints);
            mNot(temp);
            mInc(temp);
        }

        System.arraycopy(temp, 0, ints, 0, ints.length);
        return true;
    }

    // immutable subtraction: copies the first operand and applies mutable mSubtract
    static int[] sub(final int[] a, final int[] b) {
        int[] out = copyOf(a, a.length);
        mSubtract(out, b);
        return out;
    }

    // in-place subtraction mutating 'ints'
    static boolean mSubtract(final int[] ints, final int[] other) {
        long borrow = 0;
        int len = ints.length;
        int otherLen = other.length;
        for (int i = 0; i < len; i++) {
            long aVal = ints[i] & LONG;
            long bVal = i < otherLen ? (other[i] & LONG) : 0L;
            long diff = aVal - bVal - borrow;
            ints[i] = (int) diff;
            borrow = (diff >> 32) != 0 ? 1 : 0;
        }
        boolean overflow = borrow != 0;
        if (!overflow && otherLen > len) {
            for (int i = len; i < otherLen; i++) {
                if (other[i] != 0) {
                    overflow = true;
                    break;
                }
            }
        }
        return overflow;
    }

    static int[] multiply(int[] a, int[] b) {
        return multiply(a, b, new boolean[1]);
    }

    static int[] multiply(int[] a, int[] b, boolean[] overflowHolder) {
        int[] out = copyOf(a, a.length);
        overflowHolder[0] = mMultiply(out, b);
        return out;
    }

    static boolean mMultiply(final int[] ints, final int[] other) {
        int maxWidth = ints.length;
        int alen = maxWidth;
        int blen = Math.min(other.length, maxWidth);

        Scratchpad pad = SCRATCH.get();
        int[] product = pad.product;

        java.util.Arrays.fill(product, 0, maxWidth, 0);

        boolean overflow = false;
        for (int i = 0; i < alen; i++) {
            long aVal = ints[i] & LONG;
            if (aVal == 0) continue;
            long carry = 0;
            for (int j = 0; j < blen; j++) {
                int targetIdx = i + j;
                if (targetIdx < maxWidth) {
                    long prod = aVal * (other[j] & LONG) + (product[targetIdx] & LONG) + carry;
                    product[targetIdx] = (int) prod;
                    carry = prod >>> 32;
                } else {
                    if (carry != 0) {
                        overflow = true;
                        carry = 0;
                    }
                    if (other[j] != 0) {
                        overflow = true;
                    }
                }
            }
            int carryIdx = i + blen;
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

        if (other.length > maxWidth) {
            for (int i = maxWidth; i < other.length; i++) {
                if (other[i] != 0) {
                    overflow = true;
                    break;
                }
            }
        }

        System.arraycopy(product, 0, ints, 0, maxWidth);
        return overflow;
    }

    static int[] mulmod(final int[] a, final int[] b, final int[] c) {
        int[] out = copyOf(a, c.length);
        mMulMod(out, b, c);
        return out;
    }

    static boolean mMulMod(final int[] ints, final int[] mul, final int[] mod) {
        if (isZero(mod)) throw new ArithmeticException("modulo by zero");

        mModInPlace(ints, mod);

        Scratchpad pad = SCRATCH.get();
        int[] tempMul = pad.tempMul;
        java.util.Arrays.fill(tempMul, 0);
        System.arraycopy(mul, 0, tempMul, 0, mul.length);
        mModInPlace(tempMul, mod);

        int[] tempRes = new int[2 * ints.length];
        System.arraycopy(ints, 0, tempRes, 0, ints.length);

        mMultiply(tempRes, tempMul);

        mModInPlace(tempRes, mod);

        java.util.Arrays.fill(ints, 0);
        System.arraycopy(tempRes, 0, ints, 0, ints.length);
        return false;
    }



    // immutable addition modulo: copies the first operand to target width and applies mutable mAddMod
    static int[] addmod(final int[] a, final int[] b, final int[] c) {
        int[] out = copyOf(a, c.length);
        mAddMod(out, b, c);
        return out;
    }

    // in-place addition modulo mutating 'ints'
    static boolean mAddMod(final int[] ints, final int[] add, final int[] mod) {
        if (isZero(mod)) throw new ArithmeticException("modulo by zero");

        mModInPlace(ints, mod);

        Scratchpad pad = SCRATCH.get();
        int[] tempAdd = pad.tempAdd;
        java.util.Arrays.fill(tempAdd, 0);
        System.arraycopy(add, 0, tempAdd, 0, add.length);
        mModInPlace(tempAdd, mod);

        boolean carry = mAdd(ints, tempAdd);
        if (carry || compareActive(ints, mod) >= 0) {
            mSubtract(ints, mod);
        }
        return false;
    }



    static void mModInPlace(final int[] val, final int[] mod) {
        if (isZero(val)) {
            return;
        }
        int cmp = compareActive(val, mod);
        if (cmp < 0) {
            return;
        }
        if (cmp == 0) {
            java.util.Arrays.fill(val, 0);
            return;
        }

        int modEnd = mod.length - 1;
        while (modEnd >= 0 && mod[modEnd] == 0) {
            modEnd--;
        }
        int modActiveLen = modEnd + 1;
        if (modActiveLen == 0) {
            throw new ArithmeticException("modulo by zero");
        }

        int valEnd = val.length - 1;
        while (valEnd >= 0 && val[valEnd] == 0) {
            valEnd--;
        }
        int activeLen = valEnd + 1;
        if (activeLen == 0) {
            java.util.Arrays.fill(val, 0);
            return;
        }

        Scratchpad pad = SCRATCH.get();
        java.util.Arrays.fill(pad.a, 0);
        System.arraycopy(val, 0, pad.a, 0, activeLen);

        java.util.Arrays.fill(pad.d, 0);
        System.arraycopy(mod, 0, pad.d, 0, modActiveLen);

        java.util.Arrays.fill(pad.quo, 0);
        java.util.Arrays.fill(pad.rem, 0);
        java.util.Arrays.fill(pad.div, 0);

        if (modActiveLen == 1) {
            Division.div(pad.a, activeLen, pad.d[0], pad.quo, pad.rem);
            java.util.Arrays.fill(val, 0);
            val[0] = pad.rem[0];
        } else if (modActiveLen == 2) {
            final long divisor = ((pad.d[1] & LONG) << 32) | (pad.d[0] & LONG);
            Division.div(pad.a, activeLen, divisor, pad.quo, pad.rem);
            java.util.Arrays.fill(val, 0);
            int copyLimit = Math.min(2, val.length);
            System.arraycopy(pad.rem, 0, val, 0, copyLimit);
        } else {
            Division.div(pad.a, activeLen, pad.d, modActiveLen, pad.quo, pad.rem, pad.div);
            java.util.Arrays.fill(val, 0);
            int copyLimit = Math.min(modActiveLen, val.length);
            System.arraycopy(pad.rem, 0, val, 0, copyLimit);
        }
    }

    static int bitLength(int a[]) {
        int firstNonZero = a.length - 1;
        while (firstNonZero >= 0 && a[firstNonZero] == 0) {
            firstNonZero--;
        }
        if (firstNonZero < 0) return 0;
        return (firstNonZero * 32) + (32 - Integer.numberOfLeadingZeros(a[firstNonZero]));
    }

    static int[] square(final int[] a, final int maxWidth, boolean[] overflowHolder) {
        int[] padded = copyOf(a, maxWidth);
        return multiply(padded, padded, overflowHolder);
    }

    static int[] square(final int[] a, final int maxWidth) {
        int[] padded = copyOf(a, maxWidth);
        return multiply(padded, padded);
    }

    static int[] pow(int[] a, final int lo, int exp, final int maxWidth) {
        return pow(a, lo, exp, maxWidth, new boolean[1]);
    }

    static int[] pow(int[] a, final int lo, int exp, final int maxWidth, boolean[] overflowHolder) {
        int[] res = powInternal(a, lo, exp, maxWidth, overflowHolder);
        return UInt.padToWidth(res, maxWidth);
    }

    private static int[] powInternal(int[] a, final int lo, int exp, final int maxWidth, boolean[] overflowHolder) {
        if (exp == 2) {
            return square(a, maxWidth, overflowHolder);
        }

        long shift = (long) lo * exp;
        if (Integer.MAX_VALUE < shift) {
            overflowHolder[0] = true;
            return new int[maxWidth];
        }

        if (0 < lo) {
            a = rshift(a, lo, maxWidth);
        }

        final int bits = bitLength(a);
        if (bits == 1) {
            boolean overflow = (lo * exp >= maxWidth * 32);
            overflowHolder[0] = overflow;
            return 0 < lo ? lshift(ONE, lo * exp, maxWidth) : ONE;
        }

        long scale = (long) bits * exp;

        if (a.length == 1 && scale < 63) {
            long out = 1, base = a[0] & LONG;

            while (exp != 0) {
                if ((exp & 1) == 1)
                    out *= base;
                if ((exp >>>= 1) != 0)
                    base *= base;
            }

            if (0 < lo) {
                int outBits = 64 - Long.numberOfLeadingZeros(out);
                boolean overflow = (shift >= maxWidth * 32) || (outBits + shift > maxWidth * 32);
                overflowHolder[0] = overflow;
                return ((shift + scale) < 63 ?
                        valueOf(out << shift) :
                        lshift(valueOf(out), (int) shift, maxWidth));
            }
            int outBits = 64 - Long.numberOfLeadingZeros(out);
            boolean overflow = (outBits > maxWidth * 32);
            overflowHolder[0] = overflow;
            return valueOf(out);
        }

        final int lplaces = lo * exp;
        int[] out = ONE;
        boolean overflow = false;
        boolean[] stepOverflow = new boolean[1];

        while (exp != 0) {
            if ((exp & 1) == 1) {
                out = multiply(copyOf(out, maxWidth), a, stepOverflow);
                if (stepOverflow[0]) {
                    overflow = true;
                }
            }
            if ((exp >>>= 1) != 0) {
                a = square(a, maxWidth, stepOverflow);
                if (stepOverflow[0]) {
                    overflow = true;
                }
            }
        }

        if (0 < lplaces) {
            if (!isZero(out) && (lplaces >= maxWidth * 32 || bitLength(out) + lplaces > maxWidth * 32)) {
                overflow = true;
            }
            out = lshift(out, lplaces, maxWidth);
        }
        overflowHolder[0] = overflow;
        return out;
    }

    static int[] divide(final int[] a, final int[] b) {
        final int[] cleanA = stripLeadingZeroes(a);
        final int[] cleanB = stripLeadingZeroes(b);
        final int[] q;

        switch (cleanB.length) {
            case 1:
                q = Division.div(cleanA, cleanB[0])[0];
                break;
            case 2:
                final long divisor = ((cleanB[1] & LONG) << 32) | (cleanB[0] & LONG);
                q = Division.div(cleanA, divisor)[0];
                break;
            default:
                q = Division.div(cleanA, cleanB)[0];
        }

        if (q.length == a.length) {
            return q;
        }
        int[] padded = new int[a.length];
        int len = Math.min(q.length, a.length);
        System.arraycopy(q, 0, padded, 0, len);
        return padded;
    }

    static int[] mod(final int[] a, final int[] b) {
        final int[] cleanA = stripLeadingZeroes(a);
        final int[] cleanB = stripLeadingZeroes(b);
        final int[] r;

        switch (cleanB.length) {
            case 1:
                r = Division.div(cleanA, cleanB[0])[1];
                break;
            case 2:
                final long divisor = ((cleanB[1] & LONG) << 32) | (cleanB[0] & LONG);
                r = Division.div(cleanA, divisor)[1];
                break;
            default:
                r = Division.div(cleanA, cleanB)[1];
        }

        if (r.length == a.length) {
            return r;
        }
        int[] padded = new int[a.length];
        int len = Math.min(r.length, a.length);
        System.arraycopy(r, 0, padded, 0, len);
        return padded;
    }

    static int[][] divmod(final int[] a, final long b) {
        final int[] cleanA = stripLeadingZeroes(a);
        final int[][] qr = Division.div(cleanA, b);

        int[] paddedQ = new int[a.length];
        int lenQ = Math.min(qr[0].length, a.length);
        System.arraycopy(qr[0], 0, paddedQ, 0, lenQ);

        int[] paddedR = new int[a.length];
        int lenR = Math.min(qr[1].length, a.length);
        System.arraycopy(qr[1], 0, paddedR, 0, lenR);

        return new int[][]{paddedQ, paddedR};
    }

    static int[][] divmod(final int[] a, final int[] b) {
        final int[] cleanA = stripLeadingZeroes(a);
        final int[] cleanB = stripLeadingZeroes(b);
        final int[][] qr;

        switch (cleanB.length) {
            case 1:
                qr = Division.div(cleanA, cleanB[0]);
                break;
            case 2:
                final long divisor = ((cleanB[1] & LONG) << 32) | (cleanB[0] & LONG);
                qr = Division.div(cleanA, divisor);
                break;
            default:
                qr = Division.div(cleanA, cleanB);
        }

        int[] paddedQ = new int[a.length];
        int lenQ = Math.min(qr[0].length, a.length);
        System.arraycopy(qr[0], 0, paddedQ, 0, lenQ);

        int[] paddedR = new int[a.length];
        int lenR = Math.min(qr[1].length, a.length);
        System.arraycopy(qr[1], 0, paddedR, 0, lenR);

        return new int[][]{paddedQ, paddedR};
    }

    private static BigInteger BIG_INT = BigInteger.valueOf(LONG);

    static int[] from(BigInteger b, final int maxWidth) {
        int n = Math.min((b.bitLength() >>> 5) + 1, maxWidth);
        final int[] ints = new int[n];
        for (int i = 0; i < n; i++) {
            ints[i] = b.and(BIG_INT).intValue();
            b = b.shiftRight(32);
        }
        return (0 < ints.length && ints[ints.length - 1] == 0) ? stripLeadingZeroes(ints) : ints;
    }

    static int[] from(final byte[] bytes, final int[] maxValue) {
        int len = bytes.length;

        if (len == 0) {
            return ZERO;
        }

        int skip;
        for (skip = 0; skip < len && bytes[skip] == 0; skip++)
            ;

        final int ints = Math.min(maxValue.length, ((len - skip) + 3) >>> 2);
        final int[] out = new int[ints];
        int b = len - 1;
        for (int i = 0; i < ints; i++) {
            out[i] = bytes[b--] & 0xff;
            int copy = Math.min(3, b - skip + 1);
            for (int j = 8; j <= (copy << 3); j += 8)
                out[i] |= ((bytes[b--] & 0xff) << j);
        }
        return out;
    }

    static int[] maxValue(final int maxWidth) {
        final int[] max = new int[maxWidth];
        java.util.Arrays.fill(max, -1);
        return max;
    }

    static BigInteger toBigInteger(final int[] ints) {
        BigInteger out = BigInteger.ZERO;
        for (int i = ints.length - 1; i >= 0; i--) {
            out = out.shiftLeft(32).or(BigInteger.valueOf(ints[i] & LONG));
        }
        return out;
    }

    static boolean isZero(final int[] ints) {
        for (int val : ints) {
            if (val != 0) return false;
        }
        return true;
    }
















    static boolean mPow(final int[] ints, final int exp) {
        if (exp < 0) throw new ArithmeticException("Negative exponent");
        if (exp == 0) {
            java.util.Arrays.fill(ints, 0);
            ints[0] = 1;
            return false;
        }
        if (isZero(ints)) {
            return false;
        }
        if (ints.length == 1 && ints[0] == 1) {
            return false;
        }

        Scratchpad pad = SCRATCH.get();
        int[] base = pad.a;
        int[] result = pad.b;

        java.util.Arrays.fill(base, 0);
        java.util.Arrays.fill(result, 0);

        int len = ints.length;
        System.arraycopy(ints, 0, base, 0, len);
        result[0] = 1;

        boolean overflow = false;
        int e = exp;
        while (e > 0) {
            if ((e & 1) == 1) {
                java.util.Arrays.fill(pad.c, 0);
                System.arraycopy(result, 0, pad.c, 0, len);

                java.util.Arrays.fill(pad.d, 0);
                System.arraycopy(base, 0, pad.d, 0, len);

                mMultiply(pad.c, pad.d);

                for (int i = len; i < pad.c.length; i++) {
                    if (pad.c[i] != 0) {
                        overflow = true;
                        break;
                    }
                }
                System.arraycopy(pad.c, 0, result, 0, len);
            }
            e >>>= 1;
            if (e > 0) {
                java.util.Arrays.fill(pad.c, 0);
                System.arraycopy(base, 0, pad.c, 0, len);

                mMultiply(pad.c, pad.c);

                for (int i = len; i < pad.c.length; i++) {
                    if (pad.c[i] != 0) {
                        overflow = true;
                        break;
                    }
                }
                System.arraycopy(pad.c, 0, base, 0, len);
            }
        }

        System.arraycopy(result, 0, ints, 0, len);
        return overflow;
    }

    static boolean mDivide(final int[] ints, final int[] other) {
        if (isZero(other)) throw new ArithmeticException("divide by zero");
        if (isZero(ints)) {
            return false;
        }
        int cmp = compareActive(ints, other);
        if (cmp < 0) {
            java.util.Arrays.fill(ints, 0);
            return false;
        }
        if (cmp == 0) {
            java.util.Arrays.fill(ints, 0);
            ints[0] = 1;
            return false;
        }

        Scratchpad pad = SCRATCH.get();
        java.util.Arrays.fill(pad.quo, 0);
        java.util.Arrays.fill(pad.rem, 0);
        java.util.Arrays.fill(pad.div, 0);
        java.util.Arrays.fill(pad.d, 0);

        int otherEnd = other.length - 1;
        while (otherEnd >= 0 && other[otherEnd] == 0) {
            otherEnd--;
        }
        int otherActiveLen = otherEnd + 1;
        System.arraycopy(other, 0, pad.d, 0, otherActiveLen);

        int qints;
        if (otherActiveLen == 1) {
            Division.div(ints, ints.length, pad.d[0], pad.quo, pad.rem);
            qints = ints.length;
        } else if (otherActiveLen == 2) {
            final long divisor = ((pad.d[1] & LONG) << 32) | (pad.d[0] & LONG);
            Division.div(ints, ints.length, divisor, pad.quo, pad.rem);
            qints = ints.length - 1;
        } else {
            Division.div(ints, ints.length, pad.d, otherActiveLen, pad.quo, pad.rem, pad.div);
            int places = Integer.numberOfLeadingZeros(pad.d[otherActiveLen - 1]);
            int remLen = ints.length + 1;
            if (0 < places && places > Integer.numberOfLeadingZeros(ints[ints.length - 1])) {
                remLen = ints.length + 2;
            }
            qints = remLen - otherActiveLen;
        }

        int len = ints.length;
        java.util.Arrays.fill(ints, 0);
        int copyLen = Math.min(qints, len);
        System.arraycopy(pad.quo, 0, ints, 0, copyLen);
        return false;
    }

    static boolean mMod(final int[] ints, final int[] other) {
        if (isZero(other)) throw new ArithmeticException("modulo by zero");
        mModInPlace(ints, other);
        return false;
    }

    static int[] sqrt(final int[] a, final int maxWidth) {
        int targetWidth = maxWidth == -1 ? a.length : maxWidth;
        int[] op = copyOf(a, targetWidth);
        int[] res = new int[targetWidth];
        int[] one = new int[targetWidth];
        int[] sum = new int[targetWidth];

        int bit = bitLength(op);
        if (bit == 0) {
            return res;
        }
        if (bit % 2 == 0) {
            bit -= 2;
        } else {
            bit -= 1;
        }
        mSetBit(one, bit);

        while (!isZero(one)) {
            System.arraycopy(res, 0, sum, 0, targetWidth);
            mAdd(sum, one);

            if (compareActive(op, sum) >= 0) {
                mSubtract(op, sum);
                mShiftRight(res, 1);
                mAdd(res, one);
            } else {
                mShiftRight(res, 1);
            }
            mShiftRight(one, 2);
        }

        return res;
    }

    static boolean mSqrt(final int[] ints) {
        if (isZero(ints)) {
            return false;
        }
        int[] res = sqrt(ints, ints.length);
        java.util.Arrays.fill(ints, 0);
        int copyLen = Math.min(res.length, ints.length);
        System.arraycopy(res, 0, ints, 0, copyLen);
        return false;
    }
}
