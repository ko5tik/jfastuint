package de.pribluda.ether.jfastuint;

import java.math.BigInteger;

public class UInt256Factory {
    private static final int BATCH_INTS = 10000;
    private static final int WIDTH = 8;

    private int[] currentBatch;
    private int currentOffset;
    private final java.util.List<Slice> freeSlices = new java.util.ArrayList<>();

    private static class Slice {
        final int[] array;
        final int offset;
        Slice(int[] array, int offset) {
            this.array = array;
            this.offset = offset;
        }
    }

    public UInt256Factory() {
        allocateBatch();
    }

    private void allocateBatch() {
        this.currentBatch = new int[BATCH_INTS];
        this.currentOffset = 0;
    }

    private Slice acquireSlice() {
        if (!freeSlices.isEmpty()) {
            return freeSlices.remove(freeSlices.size() - 1);
        }
        if (currentOffset + WIDTH > BATCH_INTS) {
            allocateBatch();
        }
        int offset = currentOffset;
        currentOffset += WIDTH;
        return new Slice(currentBatch, offset);
    }

    public synchronized UInt256 create() {
        Slice slice = acquireSlice();
        for (int i = 0; i < WIDTH; i++) {
            slice.array[slice.offset + i] = 0;
        }
        return new UInt256(slice.array, slice.offset, false);
    }

    public synchronized UInt256 create(long val) {
        Slice slice = acquireSlice();
        slice.array[slice.offset] = (int) val;
        slice.array[slice.offset + 1] = (int) (val >>> 32);
        for (int i = 2; i < WIDTH; i++) {
            slice.array[slice.offset + i] = 0;
        }
        return new UInt256(slice.array, slice.offset, false);
    }

    public synchronized UInt256 create(int[] initialValues) {
        Slice slice = acquireSlice();
        for (int i = 0; i < WIDTH; i++) {
            slice.array[slice.offset + i] = 0;
        }
        if (initialValues != null) {
            int len = Math.min(initialValues.length, WIDTH);
            for (int i = 0; i < len; i++) {
                slice.array[slice.offset + i] = initialValues[initialValues.length - 1 - i];
            }
        }
        return new UInt256(slice.array, slice.offset, false);
    }

    public synchronized UInt256 create(BigInteger val) {
        Slice slice = acquireSlice();
        for (int i = 0; i < WIDTH; i++) {
            slice.array[slice.offset + i] = 0;
        }
        if (val != null) {
            BigInteger temp = val;
            for (int i = 0; i < WIDTH; i++) {
                slice.array[slice.offset + i] = temp.and(BigInteger.valueOf(0xffffffffL)).intValue();
                temp = temp.shiftRight(32);
            }
        }
        return new UInt256(slice.array, slice.offset, false);
    }

    public synchronized UInt256 create(String s) {
        return create(s, 10);
    }

    public synchronized UInt256 create(String s, int radix) {
        Slice slice = acquireSlice();
        int[] parsed = StringUtil.fromString(s, radix, WIDTH);
        int len = Math.min(parsed.length, WIDTH);
        for (int i = 0; i < len; i++) {
            slice.array[slice.offset + i] = parsed[i];
        }
        for (int i = len; i < WIDTH; i++) {
            slice.array[slice.offset + i] = 0;
        }
        return new UInt256(slice.array, slice.offset, false);
    }

    public synchronized void recycle(UInt256 val) {
        if (val != null && val.ints != null && val.length == WIDTH) {
            freeSlices.add(new Slice(val.ints, val.offset));
        }
    }
}
