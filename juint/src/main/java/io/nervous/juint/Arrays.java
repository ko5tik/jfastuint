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
        return hi == 0 ? new int[]{(int) v} : new int[]{hi, (int) v};
    }

    static int compare(final int[] ints, final int[] other) {
        final int len = ints.length;
        if (len < other.length) {
            return -1;
        }
        if (len > other.length) {
            return 1;
        }

        int cmp;
        for (int i = 0; i < len; i++) {
            if (ints[i] != other[i]) {
                return Integer.compareUnsigned(ints[i], other[i]);
            }
        }

        return 0;
    }

    static int compareActive(final int[] a, final int[] b) {
        int aStart = 0;
        while (aStart < a.length && a[aStart] == 0) {
            aStart++;
        }
        int aLen = a.length - aStart;

        int bStart = 0;
        while (bStart < b.length && b[bStart] == 0) {
            bStart++;
        }
        int bLen = b.length - bStart;

        if (aLen < bLen) {
            return -1;
        }
        if (aLen > bLen) {
            return 1;
        }

        for (int i = 0; i < aLen; i++) {
            int aVal = a[aStart + i];
            int bVal = b[bStart + i];
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
        final int len = ints.length;

        for (; strip < len && ints[strip] == 0; strip++)
            ;
        return strip == 0 ? ints : copyOfRange(ints, strip, len);
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

    static int[] not(final int[] ints, final int[] maxValue) {
        int maxWidth = maxValue.length;
        final int[] out = new int[maxWidth];
        int len = ints.length;
        int diff = maxWidth - len;
        for (int i = 0; i < diff; i++) {
            out[i] = -1;
        }
        for (int i = diff; i < maxWidth; i++) {
            out[i] = ~ints[i - diff];
        }
        return out;
    }

    // in place version of not
    static void not(final int[] ints) {
        for (int i = 0; i < ints.length; i++) {
            ints[i] = ~ints[i];
        }
    }

    static int[] and(int[] longer, int[] shorter) {
        if (longer.length < shorter.length) {
            int[] tmp = longer;
            longer = shorter;
            shorter = tmp;
        }
        int shortlen = shorter.length;
        if (shortlen == 0) {
            return new int[longer.length];
        }

        final int[] out = copyOf(shorter, shortlen);
        int longlen = longer.length;

        while (0 < shortlen) {
            out[--shortlen] &= longer[--longlen];
        }

        return out;
    }

    static int[] or(int[] longer, int[] shorter) {
        if (longer.length < shorter.length) {
            int[] tmp = longer;
            longer = shorter;
            shorter = tmp;
        }
        int longlen = longer.length, shortlen = shorter.length;
        final int[] out = copyOf(longer, longlen);

        while (0 < shortlen) {
            out[--longlen] |= shorter[--shortlen];
        }

        return out;
    }

    static int[] xor(int[] longer, int[] shorter) {
        if (longer.length < shorter.length) {
            int[] tmp = longer;
            longer = shorter;
            shorter = tmp;
        }
        int longlen = longer.length, shortlen = shorter.length;
        if (longlen == 0) {
            return new int[0];
        }

        final int[] out = copyOf(longer, longlen);

        while (0 < shortlen) {
            out[--longlen] ^= shorter[--shortlen];
        }

        return out;
    }

    static int[] setBit(final int[] a, final int bit) {
        final int i = bit >>> 5, alen = a.length;

        if (i <= alen - 1) {
            final int j = alen - i - 1, v = a[j] | 1 << (bit & 31);
            if (v == a[j]) {
                return a;
            }
            final int[] out = copyOf(a, alen);
            out[j] = v;
            return out;
        }

        final int[] out = new int[i + 1];
        System.arraycopy(a, 0, out, out.length - alen, alen);

        out[0] = 1 << (bit & 31);
        return out;
    }

    static int[] clearBit(final int[] a, final int bit) {
        final int alen = a.length, i = alen - (bit >>> 5) - 1;
        final int v = a[i] & ~(1 << (bit & 31));

        if (v == a[i]) {
            return a;
        }

        final int[] out = copyOf(a, alen);
        out[i] = v;
        return out;
    }

    static int[] flipBit(final int[] a, final int bit) {
        final int i = bit >>> 5, alen = a.length;

        if (i < alen) {
            final int[] out = copyOf(a, alen);
            out[alen - i - 1] ^= (1 << (bit & 31));
            return out;
        }

        final int[] out = new int[i + 1];
        System.arraycopy(a, 0, out, out.length - alen, alen);
        out[0] ^= (1 << (bit & 31));
        return out;
    }

    static int[] lshift(final int[] a, final int n, final int maxWidth) {
        int targetWidth = maxWidth == -1 ? a.length + (n >>> 5) + ((n & 0x1f) != 0 ? 1 : 0) : maxWidth;
        final int[] out = new int[targetWidth];
        final int wordShift = n >>> 5;
        if (wordShift >= targetWidth) {
            return out;
        }
        final int alen = a.length;
        final int bitShift = n & 0x1f;
        if (bitShift == 0) {
            for (int i = 0; i < targetWidth - wordShift; i++) {
                int aIdx = i + wordShift - (targetWidth - alen);
                out[i] = (aIdx >= 0 && aIdx < alen) ? a[aIdx] : 0;
            }
        } else {
            final int invShift = 32 - bitShift;
            for (int i = 0; i < targetWidth - wordShift; i++) {
                int aIdx1 = i + wordShift - (targetWidth - alen);
                int aIdx2 = i + wordShift + 1 - (targetWidth - alen);
                int val1 = (aIdx1 >= 0 && aIdx1 < alen) ? a[aIdx1] : 0;
                int val2 = (aIdx2 >= 0 && aIdx2 < alen) ? a[aIdx2] : 0;
                out[i] = (val1 << bitShift) | (val2 >>> invShift);
            }
        }
        return out;
    }

    static int[] rshift(final int[] a, final int n, final int maxWidth) {
        int targetWidth = maxWidth == -1 ? a.length : maxWidth;
        final int[] out = new int[targetWidth];
        final int wordShift = n >>> 5;
        if (wordShift >= targetWidth) {
            return out;
        }
        final int alen = a.length;
        final int bitShift = n & 0x1f;
        if (bitShift == 0) {
            for (int i = wordShift; i < targetWidth; i++) {
                int aIdx = i - wordShift - (targetWidth - alen);
                out[i] = (aIdx >= 0 && aIdx < alen) ? a[aIdx] : 0;
            }
        } else {
            final int invShift = 32 - bitShift;
            for (int i = wordShift; i < targetWidth; i++) {
                int aIdx1 = i - wordShift - 1 - (targetWidth - alen);
                int aIdx2 = i - wordShift - (targetWidth - alen);
                int val1 = (aIdx1 >= 0 && aIdx1 < alen) ? a[aIdx1] : 0;
                int val2 = (aIdx2 >= 0 && aIdx2 < alen) ? a[aIdx2] : 0;
                out[i] = (val1 << invShift) | (val2 >>> bitShift);
            }
        }
        return out;
    }

    static int[] inc(final int[] a, final int maxWidth) {
        return inc(a, false, maxWidth);
    }

    static int[] inc(final int[] a, final boolean mutate, final int maxWidth) {
        final int len = a.length;
        final int targetWidth = maxWidth == -1 ? len : maxWidth;
        final int[] b;
        if (len == targetWidth) {
            b = mutate ? a : copyOf(a, len);
        } else {
            b = new int[targetWidth];
            System.arraycopy(a, 0, b, targetWidth - len, len);
        }

        int last = targetWidth - 1;
        while (0 <= last) {
            if (++(b[last--]) != 0) {
                return b;
            }
        }
        return b;
    }

    static int[] dec(final int[] a) {
        final int len = a.length;
        final int[] b = copyOf(a, len);
        int last = len - 1;
        while (last >= 0) {
            if (--(b[last--]) != -1) {
                break;
            }
        }
        return b;
    }

    static int[] add(int[] longer, int[] shorter, final int maxWidth) {
        if (longer.length < shorter.length) {
            int[] tmp = longer;
            longer = shorter;
            shorter = tmp;
        }
        int targetWidth = maxWidth == -1 ? longer.length : maxWidth;
        final int[] out = new int[targetWidth];
        
        int longi = longer.length;
        int shorti = shorter.length;
        int outi = targetWidth;
        long sum = 0;

        while (0 < outi) {
            long aVal = 0;
            if (0 < longi) {
                aVal = longer[--longi] & LONG;
            }
            long bVal = 0;
            if (0 < shorti) {
                bVal = shorter[--shorti] & LONG;
            }
            sum = aVal + bVal + (sum >>> 32);
            out[--outi] = (int) sum;
        }

        if (maxWidth == -1 && (sum >>> 32) != 0) {
            int[] grown = new int[targetWidth + 1];
            grown[0] = 1;
            System.arraycopy(out, 0, grown, 1, targetWidth);
            return grown;
        }

        return out;
    }

    static int[] subgt(final int[] a, final int[] b, final int[] maxValue) {
        if (a.length == 0) {
            return inc(not(b, maxValue), true, maxValue.length);
        }
        return inc(not(sub(b, a), maxValue), true, maxValue.length);
    }

    static int[] sub(final int[] a, final int[] b) {
        int longi = a.length, shorti = b.length;
        final int[] out = copyOf(a, longi);
        long diff = 0;

        while (0 < shorti) {
            diff = (out[--longi] & LONG) - (b[--shorti] & LONG) + (diff >> 32);
            out[longi] = (int) diff;
        }

        if (diff >> 32 != 0) {
            while (0 < longi && --(out[--longi]) == -1) {
                ;
            }
        }

        return out;
    }

    static int[] mulmod(int[] a, int[] b, final int[] c) {
        if (a.length < b.length) {
            int[] tmp = a;
            a = b;
            b = tmp;
        }
        if (b.length == 0) {
            return new int[c.length];
        }
        final int[] mul = mul(a, a.length, b, b.length);
        final int cmp = compareActive(mul, c);
        int[] res = (cmp < 0 ? mul : (cmp == 0 ? ZERO : mod(mul, c)));
        if (res.length == c.length) {
            return res;
        }
        int[] padded = new int[c.length];
        int len = Math.min(res.length, c.length);
        System.arraycopy(res, res.length - len, padded, c.length - len, len);
        return padded;
    }

    static int[] addmod(int[] a, int[] b, final int[] c) {
        if (a.length < b.length) {
            int[] tmp = a;
            a = b;
            b = tmp;
        }
        final int[] add = b.length == 0 ? a : add(a, b, -1);
        final int cmp = compareActive(add, c);
        int[] res = (cmp < 0 ? add : (cmp == 0 ? ZERO : mod(add, c)));
        if (res.length == c.length) {
            return res;
        }
        int[] padded = new int[c.length];
        int len = Math.min(res.length, c.length);
        System.arraycopy(res, res.length - len, padded, c.length - len, len);
        return padded;
    }

    static int[] multiply(int[] a, int[] b, final int maxWidth) {
        if (a.length < b.length) {
            int[] tmp = a;
            a = b;
            b = tmp;
        }
        final int alen = a.length, blen = b.length;

        if (blen == 1) {
            return mul(a, alen, b[0], maxWidth);
        }
        if (blen == 2) {
            return mul(a, alen, b[0], b[1], maxWidth);
        }

        final int outlen = alen + blen;
        return mul(a, alen, b, blen, maxWidth, Math.max(0, outlen - maxWidth));
    }

    static int[] mul(int[] a, final int alen, int b, int maxWidth) {
        if (Integer.bitCount(b) == 1) {
            return lshift(a, Integer.numberOfTrailingZeros(b), maxWidth);
        }

        final int[] out = new int[maxWidth];
        long carry = 0;
        final long bl = b & LONG;

        int ai = alen - 1;
        int outi = maxWidth - 1;
        while (0 <= ai && 0 <= outi) {
            final long prod = (a[ai--] & LONG) * bl + carry;
            out[outi--] = (int) prod;
            carry = prod >>> 32;
        }
        if (0 <= outi) {
            out[outi] = (int) carry;
        }

        return out;
    }

    static int[] mul(
            final int[] a, final int alen, final int hi, final int lo, final int maxWidth) {

        final int[] out = new int[maxWidth];
        final long lhi = hi & LONG;
        final long llo = lo & LONG;

        long carry = 0;
        int ai = alen - 1;
        int outi = maxWidth - 1;
        while (0 <= ai && 0 <= outi) {
            long prod = (a[ai--] & LONG) * llo + carry;
            out[outi--] = (int) prod;
            carry = prod >>> 32;
        }
        if (0 <= outi) {
            out[outi] = (int) carry;
        }

        carry = 0;
        ai = alen - 1;
        outi = maxWidth - 2;
        while (0 <= ai && 0 <= outi) {
            long prod = (a[ai--] & LONG) * lhi + (out[outi] & LONG) + carry;
            out[outi--] = (int) prod;
            carry = prod >>> 32;
        }
        if (0 <= outi) {
            out[outi] = (int) ((out[outi] & LONG) + carry);
        }

        return out;
    }

    static int[] mul(
            final int[] a, final int alen, final int[] b, final int blen) {

        final int outlen = alen + blen;
        final int astart = alen - 1, bstart = blen - 1;
        final int[] out = new int[outlen];
        long carry = 0;

        for (int bi = bstart, outi = outlen - 1; 0 <= bi; bi--, outi--) {
            final long prod = (b[bi] & LONG) * (a[astart] & LONG) + carry;
            out[outi] = (int) prod;
            carry = prod >>> 32;
        }
        out[astart] = (int) carry;

        for (int ai = astart - 1; 0 <= ai; ai--) {
            carry = 0;
            for (int bi = bstart, outi = bstart + ai + 1; 0 <= bi; bi--, outi--) {
                final long prod = (b[bi] & LONG) * (a[ai] & LONG) + (out[outi] & LONG) + carry;
                out[outi] = (int) prod;
                carry = prod >>> 32;
            }
            out[ai] = (int) carry;
        }

        return carry == 0L ? stripLeadingZeroes(out, 1) : out;
    }

    static void mul(
            final int[] a, final int alen, final int[] b, final int blen, final int[] out) {

        final int outlen = alen + blen;
        java.util.Arrays.fill(out, 0, outlen, 0);
        final int astart = alen - 1, bstart = blen - 1;
        long carry = 0;

        for (int bi = bstart, outi = outlen - 1; 0 <= bi; bi--, outi--) {
            final long prod = (b[bi] & LONG) * (a[astart] & LONG) + carry;
            out[outi] = (int) prod;
            carry = prod >>> 32;
        }
        out[astart] = (int) carry;

        for (int ai = astart - 1; 0 <= ai; ai--) {
            carry = 0;
            for (int bi = bstart, outi = bstart + ai + 1; 0 <= bi; bi--, outi--) {
                final long prod = (b[bi] & LONG) * (a[ai] & LONG) + (out[outi] & LONG) + carry;
                out[outi] = (int) prod;
                carry = prod >>> 32;
            }
            out[ai] = (int) carry;
        }
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

        int modStart = 0;
        while (modStart < mod.length && mod[modStart] == 0) {
            modStart++;
        }
        int modActiveLen = mod.length - modStart;
        if (modActiveLen == 0) {
            throw new ArithmeticException("modulo by zero");
        }

        int start = 0;
        while (start < val.length && val[start] == 0) {
            start++;
        }
        int activeLen = val.length - start;
        if (activeLen == 0) {
            java.util.Arrays.fill(val, 0);
            return;
        }

        Scratchpad pad = SCRATCH.get();
        java.util.Arrays.fill(pad.a, 0);
        System.arraycopy(val, start, pad.a, 0, activeLen);

        java.util.Arrays.fill(pad.d, 0);
        System.arraycopy(mod, modStart, pad.d, 0, modActiveLen);

        java.util.Arrays.fill(pad.quo, 0);
        java.util.Arrays.fill(pad.rem, 0);
        java.util.Arrays.fill(pad.div, 0);

        if (modActiveLen == 1) {
            Division.div(pad.a, activeLen, pad.d[0], pad.quo, pad.rem);
            java.util.Arrays.fill(val, 0);
            val[val.length - 1] = pad.rem[0];
        } else if (modActiveLen == 2) {
            final long divisor = ((pad.d[0] & LONG) << 32) | (pad.d[1] & LONG);
            Division.div(pad.a, activeLen, divisor, pad.quo, pad.rem);
            java.util.Arrays.fill(val, 0);
            int srcLen = activeLen + 1;
            int copyLimit = Math.min(srcLen, val.length);
            System.arraycopy(pad.rem, srcLen - copyLimit, val, val.length - copyLimit, copyLimit);
        } else {
            Division.div(pad.a, activeLen, pad.d, modActiveLen, pad.quo, pad.rem, pad.div);
            int places = Integer.numberOfLeadingZeros(pad.d[0]);
            int remLen = (0 < places && places > Integer.numberOfLeadingZeros(pad.a[0])) ? activeLen + 2 : activeLen + 1;
            int copyLimit = Math.min(remLen, val.length);
            java.util.Arrays.fill(val, 0);
            System.arraycopy(pad.rem, remLen - copyLimit, val, val.length - copyLimit, copyLimit);
        }
    }

    static int[] mul(
            final int[] a, final int alen, final int[] b, final int blen,
            final int outlen, final int trunc) {

        final int astart = alen - 1, bstart = blen - 1;
        final int[] out = new int[outlen];
        long carry = 0;

        for (int bi = bstart, outi = outlen - 1; 0 <= bi; bi--, outi--) {
            final long prod = (b[bi] & LONG) * (a[astart] & LONG) + carry;
            out[outi] = (int) prod;
            carry = prod >>> 32;
        }
        if (trunc <= astart) {
            out[astart - trunc] = (int) carry;
        }

        int outend = outlen - 2;
        for (int ai = astart - 1; 0 <= ai; ai--) {
            carry = 0;
            for (int bi = bstart, outi = outend--; 0 <= bi && 0 <= outi; bi--, outi--) {
                final long prod = (b[bi] & LONG) * (a[ai] & LONG) + (out[outi] & LONG) + carry;
                out[outi] = (int) prod;
                carry = prod >>> 32;
            }
            if (trunc <= ai) {
                out[ai - trunc] = (int) carry;
            }
        }
        return out;
    }

    static int bitLength(int a[]) {
        int firstNonZero = 0;
        while (firstNonZero < a.length && a[firstNonZero] == 0) {
            firstNonZero++;
        }
        if (firstNonZero == a.length) return 0;
        int activeLen = a.length - firstNonZero;
        return ((activeLen - 1) * 32) + (32 - Integer.numberOfLeadingZeros(a[firstNonZero]));
    }

    static int[] square(final int[] a, final int maxWidth) {
        final int alen = a.length, start;

        int outlen = alen << 1;
        if (maxWidth < outlen) {
            start = (outlen - maxWidth) >>> 1;
            outlen = maxWidth;
        } else {
            start = 0;
        }

        final int[] out = new int[outlen];

        int last = 0;
        for (int ai = start, i = 0; ai < alen; ai++) {
            final long wl = (a[ai] & LONG);
            final long prod = wl * wl;
            out[i++] = (last << 31) | (int) (prod >>> 33);
            out[i++] = (int) (prod >>> 1);
            last = (int) prod;
        }

        for (int ai = alen, pos = 1; start < ai && pos < outlen; ai--, pos += 2) {
            long carry = 0;
            int outi = outlen - pos - 1;
            final long v = a[ai - 1] & LONG;

            for (int aj = ai - 2; 0 <= aj && 0 <= outi; aj--) {
                long prod = (a[aj] & LONG) * v + (out[outi] & LONG) + carry;
                out[outi--] = (int) prod;
                carry = prod >>> 32;
            }

            if (0 <= outi) {
                carry += (out[outi] & LONG);
                out[outi] = (int) carry;
                if ((carry >> 32) != 0) {
                    for (int tmp = ai - 1; 0 <= tmp && 0 <= --outi && ++(out[outi]) == 0; tmp--) {
                        ;
                    }
                }
            }
        }

        Division.lshunt(out, 1);
        out[outlen - 1] |= (a[alen - 1] & 1);
        return out;
    }

    static int[] pow(int[] a, final int lo, int exp, final int maxWidth) {
        int[] res = powInternal(a, lo, exp, maxWidth);
        if (res.length == maxWidth) {
            return res;
        }
        int[] padded = new int[maxWidth];
        int len = Math.min(res.length, maxWidth);
        System.arraycopy(res, res.length - len, padded, maxWidth - len, len);
        return padded;
    }

    private static int[] powInternal(int[] a, final int lo, int exp, final int maxWidth) {
        if (exp == 2) {
            return square(a, maxWidth);
        }

        long shift = (long) lo * exp;
        if (Integer.MAX_VALUE < shift) {
            throw new ArithmeticException("Overflow");
        }

        if (0 < lo) {
            a = rshift(a, lo, maxWidth);
        }

        final int bits = bitLength(a);
        if (bits == 1) {
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
                return ((shift + scale) < 63 ?
                        valueOf(out << shift) :
                        lshift(valueOf(out), (int) shift, maxWidth));
            }
            return valueOf(out);
        }

        final int lplaces = lo * exp;
        int[] out = ONE;

        while (exp != 0) {
            if ((exp & 1) == 1)
                out = multiply(out, a, maxWidth);
            if ((exp >>>= 1) != 0)
                a = square(a, maxWidth);
        }

        return 0 < lplaces ? lshift(out, lplaces, maxWidth) : out;
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
                final long divisor = ((cleanB[0] & LONG) << 32) | (cleanB[1] & LONG);
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
        System.arraycopy(q, q.length - len, padded, a.length - len, len);
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
                final long divisor = ((cleanB[0] & LONG) << 32) | (cleanB[1] & LONG);
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
        System.arraycopy(r, r.length - len, padded, a.length - len, len);
        return padded;
    }

    static int[][] divmod(final int[] a, final long b) {
        final int[] cleanA = stripLeadingZeroes(a);
        final int[][] qr = Division.div(cleanA, b);

        int[] paddedQ = new int[a.length];
        int lenQ = Math.min(qr[0].length, a.length);
        System.arraycopy(qr[0], qr[0].length - lenQ, paddedQ, a.length - lenQ, lenQ);

        int[] paddedR = new int[a.length];
        int lenR = Math.min(qr[1].length, a.length);
        System.arraycopy(qr[1], qr[1].length - lenR, paddedR, a.length - lenR, lenR);

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
                final long divisor = ((cleanB[0] & LONG) << 32) | (cleanB[1] & LONG);
                qr = Division.div(cleanA, divisor);
                break;
            default:
                qr = Division.div(cleanA, cleanB);
        }

        int[] paddedQ = new int[a.length];
        int lenQ = Math.min(qr[0].length, a.length);
        System.arraycopy(qr[0], qr[0].length - lenQ, paddedQ, a.length - lenQ, lenQ);

        int[] paddedR = new int[a.length];
        int lenR = Math.min(qr[1].length, a.length);
        System.arraycopy(qr[1], qr[1].length - lenR, paddedR, a.length - lenR, lenR);

        return new int[][]{paddedQ, paddedR};
    }

    private static BigInteger BIG_INT = BigInteger.valueOf(LONG);

    static int[] from(BigInteger b, final int maxWidth) {
        int n = Math.min((b.bitLength() >>> 5) + 1, maxWidth);
        final int[] ints = new int[n];
        while (0 < n) {
            ints[--n] = b.and(BIG_INT).intValue();
            b = b.shiftRight(32);
        }
        return (0 < ints.length && ints[0] == 0) ? stripLeadingZeroes(ints) : ints;
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
        for (int i = ints - 1; 0 <= i; i--) {
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
        for (int i = 0; i < ints.length; i++) {
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

    static boolean mNot(final int[] ints) {
        for (int i = 0; i < ints.length; i++) {
            ints[i] = ~ints[i];
        }
        return false;
    }

    static boolean mAnd(final int[] ints, final int[] other) {
        int len = ints.length;
        int otherLen = other.length;
        for (int i = 0; i < len; i++) {
            int otherIdx = otherLen - (len - i);
            int otherVal = otherIdx >= 0 ? other[otherIdx] : 0;
            ints[i] &= otherVal;
        }
        return false;
    }

    static boolean mOr(final int[] ints, final int[] other) {
        int len = ints.length;
        int otherLen = other.length;
        for (int i = 0; i < len; i++) {
            int otherIdx = otherLen - (len - i);
            int otherVal = otherIdx >= 0 ? other[otherIdx] : 0;
            ints[i] |= otherVal;
        }
        return false;
    }

    static boolean mXor(final int[] ints, final int[] other) {
        int len = ints.length;
        int otherLen = other.length;
        for (int i = 0; i < len; i++) {
            int otherIdx = otherLen - (len - i);
            int otherVal = otherIdx >= 0 ? other[otherIdx] : 0;
            ints[i] ^= otherVal;
        }
        return false;
    }

    static boolean mSetBit(final int[] ints, final int bit) {
        int len = ints.length;
        if (bit < 0 || bit >= len * 32) return true;
        int word = len - 1 - (bit >>> 5);
        ints[word] |= (1 << (bit & 31));
        return false;
    }

    static boolean mClearBit(final int[] ints, final int bit) {
        int len = ints.length;
        if (bit < 0 || bit >= len * 32) return true;
        int word = len - 1 - (bit >>> 5);
        ints[word] &= ~(1 << (bit & 31));
        return false;
    }

    static boolean mFlipBit(final int[] ints, final int bit) {
        int len = ints.length;
        if (bit < 0 || bit >= len * 32) return true;
        int word = len - 1 - (bit >>> 5);
        ints[word] ^= (1 << (bit & 31));
        return false;
    }

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
            for (int i = 0; i < words; i++) {
                if (ints[i] != 0) {
                    overflow = true;
                }
            }
        }
        if (bits > 0 && words < len && (ints[words] >>> (32 - bits)) != 0) {
            overflow = true;
        }
        if (bits == 0) {
            for (int i = 0; i < len - words; i++) {
                ints[i] = ints[i + words];
            }
        } else {
            for (int i = 0; i < len - words - 1; i++) {
                ints[i] = (ints[i + words] << bits) | (ints[i + words + 1] >>> (32 - bits));
            }
            ints[len - words - 1] = ints[len - 1] << bits;
        }
        for (int i = len - words; i < len; i++) {
            ints[i] = 0;
        }
        return overflow;
    }

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
            for (int i = len - words; i < len; i++) {
                if (ints[i] != 0) {
                    overflow = true;
                }
            }
        }
        if (bits > 0 && words < len && (ints[len - 1 - words] & ((1 << bits) - 1)) != 0) {
            overflow = true;
        }
        if (bits == 0) {
            for (int i = len - 1; i >= words; i--) {
                ints[i] = ints[i - words];
            }
        } else {
            for (int i = len - 1; i > words; i--) {
                ints[i] = (ints[i - words] >>> bits) | (ints[i - words - 1] << (32 - bits));
            }
            ints[words] = ints[0] >>> bits;
        }
        for (int i = 0; i < words; i++) {
            ints[i] = 0;
        }
        return overflow;
    }

    static boolean mInc(final int[] ints) {
        long carry = 1;
        for (int i = ints.length - 1; i >= 0 && carry != 0; i--) {
            long sum = (ints[i] & LONG) + carry;
            ints[i] = (int) sum;
            carry = sum >>> 32;
        }
        return carry != 0;
    }

    static boolean mDec(final int[] ints) {
        long borrow = 1;
        for (int i = ints.length - 1; i >= 0 && borrow != 0; i--) {
            long diff = (ints[i] & LONG) - borrow;
            ints[i] = (int) diff;
            borrow = (diff >> 32) != 0 ? 1 : 0;
        }
        return borrow != 0;
    }

    static boolean mAdd(final int[] ints, final int[] other) {
        long carry = 0;
        int len = ints.length;
        int otherLen = other.length;
        for (int i = len - 1; i >= 0; i--) {
            long aVal = ints[i] & LONG;
            int otherIdx = otherLen - (len - i);
            long bVal = otherIdx >= 0 ? (other[otherIdx] & LONG) : 0L;
            long sum = aVal + bVal + carry;
            ints[i] = (int) sum;
            carry = sum >>> 32;
        }
        boolean overflow = carry != 0;
        if (!overflow && otherLen > len) {
            for (int i = 0; i < otherLen - len; i++) {
                if (other[i] != 0) {
                    overflow = true;
                    break;
                }
            }
        }
        return overflow;
    }

    static boolean mSubtract(final int[] ints, final int[] other) {
        long borrow = 0;
        int len = ints.length;
        int otherLen = other.length;
        for (int i = len - 1; i >= 0; i--) {
            long aVal = ints[i] & LONG;
            int otherIdx = otherLen - (len - i);
            long bVal = otherIdx >= 0 ? (other[otherIdx] & LONG) : 0L;
            long diff = aVal - bVal - borrow;
            ints[i] = (int) diff;
            borrow = (diff >> 32) != 0 ? 1 : 0;
        }
        boolean overflow = borrow != 0;
        if (!overflow && otherLen > len) {
            for (int i = 0; i < otherLen - len; i++) {
                if (other[i] != 0) {
                    overflow = true;
                    break;
                }
            }
        }
        return overflow;
    }

    static boolean mMultiply(final int[] ints, final int[] other) {
        int len = ints.length;
        int otherLen = other.length;
        Scratchpad pad = SCRATCH.get();
        int[] a = pad.a;
        System.arraycopy(ints, 0, a, 0, len);
        java.util.Arrays.fill(ints, 0);
        boolean overflow = false;
        for (int i = len - 1; i >= 0; i--) {
            long aVal = a[i] & LONG;
            if (aVal == 0) {
                continue;
            }
            long carry = 0;
            for (int j = otherLen - 1; j >= 0; j--) {
                int targetIdx = i - (otherLen - 1 - j);
                if (targetIdx >= 0) {
                    long prod = aVal * (other[j] & LONG) + (ints[targetIdx] & LONG) + carry;
                    ints[targetIdx] = (int) prod;
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
            int carryIdx = i - otherLen;
            while (carry != 0) {
                if (carryIdx >= 0) {
                    long sum = (ints[carryIdx] & LONG) + carry;
                    ints[carryIdx] = (int) sum;
                    carry = sum >>> 32;
                    carryIdx--;
                } else {
                    overflow = true;
                    break;
                }
            }
        }
        return overflow;
    }

    static boolean mAddMod(final int[] ints, final int[] add, final int[] mod) {
        if (isZero(mod)) throw new ArithmeticException("modulo by zero");

        mModInPlace(ints, mod);

        Scratchpad pad = SCRATCH.get();
        int[] tempAdd = pad.tempAdd;
        java.util.Arrays.fill(tempAdd, 0);
        System.arraycopy(add, 0, tempAdd, tempAdd.length - add.length, add.length);
        mModInPlace(tempAdd, mod);

        boolean carry = mAdd(ints, tempAdd);
        if (carry || compareActive(ints, mod) >= 0) {
            mSubtract(ints, mod);
        }
        return false;
    }

    static boolean mMulMod(final int[] ints, final int[] mul, final int[] mod) {
        if (isZero(mod)) throw new ArithmeticException("modulo by zero");

        mModInPlace(ints, mod);

        Scratchpad pad = SCRATCH.get();
        int[] tempMul = pad.tempMul;
        java.util.Arrays.fill(tempMul, 0);
        System.arraycopy(mul, 0, tempMul, tempMul.length - mul.length, mul.length);
        mModInPlace(tempMul, mod);

        System.arraycopy(tempMul, tempMul.length - ints.length, pad.b, 0, ints.length);
        int[] product = pad.product;
        mul(ints, ints.length, pad.b, ints.length, product);

        int productLen = 2 * ints.length;
        int start = 0;
        while (start < productLen && product[start] == 0) {
            start++;
        }
        int cleanLen = productLen - start;
        if (cleanLen == 0) {
            java.util.Arrays.fill(ints, 0);
            return false;
        }

        int[] productClean = pad.productClean;
        java.util.Arrays.fill(productClean, 0);
        System.arraycopy(product, start, productClean, 0, cleanLen);

        int[] tempRes = pad.c;
        java.util.Arrays.fill(tempRes, 0);
        System.arraycopy(productClean, 0, tempRes, tempRes.length - cleanLen, cleanLen);

        mModInPlace(tempRes, mod);

        java.util.Arrays.fill(ints, 0);
        int copyLen = Math.min(tempRes.length, ints.length);
        System.arraycopy(tempRes, tempRes.length - copyLen, ints, ints.length - copyLen, copyLen);
        return false;
    }

    static boolean mPow(final int[] ints, final int exp) {
        if (exp < 0) throw new ArithmeticException("Negative exponent");
        if (exp == 0) {
            java.util.Arrays.fill(ints, 0);
            ints[ints.length - 1] = 1;
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
        int[] prod = pad.product;

        java.util.Arrays.fill(base, 0);
        java.util.Arrays.fill(result, 0);

        int len = ints.length;
        System.arraycopy(ints, 0, base, base.length - len, len);
        result[result.length - 1] = 1;

        boolean overflow = false;
        int e = exp;
        while (e > 0) {
            if ((e & 1) == 1) {
                System.arraycopy(result, result.length - len, pad.c, 0, len);
                System.arraycopy(base, base.length - len, pad.d, 0, len);

                mul(pad.c, len, pad.d, len, prod);

                for (int i = 0; i < len; i++) {
                    if (prod[i] != 0) {
                        overflow = true;
                        break;
                    }
                }
                System.arraycopy(prod, len, result, result.length - len, len);
            }
            e >>>= 1;
            if (e > 0) {
                System.arraycopy(base, base.length - len, pad.c, 0, len);
                System.arraycopy(base, base.length - len, pad.d, 0, len);

                mul(pad.c, len, pad.d, len, prod);

                for (int i = 0; i < len; i++) {
                    if (prod[i] != 0) {
                        overflow = true;
                        break;
                    }
                }
                System.arraycopy(prod, len, base, base.length - len, len);
            }
        }

        System.arraycopy(result, result.length - len, ints, 0, len);
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
            ints[ints.length - 1] = 1;
            return false;
        }

        Scratchpad pad = SCRATCH.get();
        java.util.Arrays.fill(pad.quo, 0);
        java.util.Arrays.fill(pad.rem, 0);
        java.util.Arrays.fill(pad.div, 0);
        java.util.Arrays.fill(pad.d, 0);

        int otherStart = 0;
        while (otherStart < other.length && other[otherStart] == 0) {
            otherStart++;
        }
        int otherActiveLen = other.length - otherStart;
        System.arraycopy(other, otherStart, pad.d, 0, otherActiveLen);

        int qints;
        if (otherActiveLen == 1) {
            Division.div(ints, ints.length, pad.d[0], pad.quo, pad.rem);
            qints = ints.length;
        } else if (otherActiveLen == 2) {
            final long divisor = ((pad.d[0] & LONG) << 32) | (pad.d[1] & LONG);
            Division.div(ints, ints.length, divisor, pad.quo, pad.rem);
            qints = ints.length - 1;
        } else {
            Division.div(ints, ints.length, pad.d, otherActiveLen, pad.quo, pad.rem, pad.div);
            int places = Integer.numberOfLeadingZeros(pad.d[0]);
            int remLen = (0 < places && places > Integer.numberOfLeadingZeros(ints[0])) ? ints.length + 2 : ints.length + 1;
            qints = remLen - otherActiveLen;
        }

        int len = ints.length;
        java.util.Arrays.fill(ints, 0);
        int copyLen = Math.min(qints, len);
        System.arraycopy(pad.quo, qints - copyLen, ints, len - copyLen, copyLen);
        return false;
    }

    static boolean mMod(final int[] ints, final int[] other) {
        if (isZero(other)) throw new ArithmeticException("modulo by zero");
        mModInPlace(ints, other);
        return false;
    }

    static int[] sqrt(final int[] a, final int maxWidth) {
        if (isZero(a)) {
            return new int[maxWidth];
        }
        int bitLen = bitLength(a);
        int guessShift = (bitLen + 1) / 2;
        int[] x = setBit(new int[maxWidth], guessShift);

        while (true) {
            int[] divRes;
            if (compareActive(a, x) < 0) {
                divRes = new int[maxWidth];
            } else {
                divRes = divide(a, x);
            }
            int[] sum = add(x, divRes, -1);
            int[] next = rshift(sum, 1, -1);

            if (compareActive(next, x) >= 0) {
                break;
            }
            x = next;
        }

        if (x.length == maxWidth) {
            return x;
        }
        int[] padded = new int[maxWidth];
        int len = Math.min(x.length, maxWidth);
        System.arraycopy(x, x.length - len, padded, maxWidth - len, len);
        return padded;
    }

    static boolean mSqrt(final int[] ints) {
        if (isZero(ints)) {
            return false;
        }
        int[] res = sqrt(ints, ints.length);
        java.util.Arrays.fill(ints, 0);
        int copyLen = Math.min(res.length, ints.length);
        System.arraycopy(res, res.length - copyLen, ints, ints.length - copyLen, copyLen);
        return false;
    }
}
