package de.pribluda.ether.jfastuint;

import org.junit.Test;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

public class TestFactories {

    @Test
    public void testUInt128FactoryCorrectness() {
        UInt128Factory factory = new UInt128Factory();

        // Test create empty
        UInt128 u1 = factory.create();
        assertTrue(u1.isZero());
        assertEquals(0, u1.intValue());

        // Test create from long
        UInt128 u2 = factory.create(1234567890123L);
        assertEquals(1234567890123L, u2.longValue());

        // Test create from BigInteger
        BigInteger bigVal = new BigInteger("987654321098765432109876543210");
        UInt128 u3 = factory.create(bigVal);
        assertEquals(bigVal, u3.toBigInteger());

        // Test create from int array
        int[] arr = {0, 0, 0, 5}; // big endian format
        UInt128 u4 = factory.create(arr);
        assertEquals(BigInteger.valueOf(5), u4.toBigInteger());

        // Test create from String
        UInt128 u5 = factory.create("abcdef", 16);
        assertEquals(0xabcdefL, u5.longValue());
    }

    @Test
    public void testUInt256FactoryCorrectness() {
        UInt256Factory factory = new UInt256Factory();

        // Test create empty
        UInt256 u1 = factory.create();
        assertTrue(u1.isZero());
        assertEquals(0, u1.intValue());

        // Test create from long
        UInt256 u2 = factory.create(9876543210L);
        assertEquals(9876543210L, u2.longValue());

        // Test create from BigInteger
        BigInteger bigVal = new BigInteger("12345678901234567890123456789012345678901234567890");
        UInt256 u3 = factory.create(bigVal);
        assertEquals(bigVal, u3.toBigInteger());

        // Test create from String
        UInt256 u4 = factory.create("1234567890abcdef", 16);
        assertEquals(new BigInteger("1234567890abcdef", 16), u4.toBigInteger());
    }

    @Test
    public void testRecyclingAndBatchAllocation128() {
        UInt128Factory factory = new UInt128Factory();

        // Create 2600 instances of UInt128.
        // Each batch is 10000 ints, which fits 2500 UInt128 instances.
        // The 2501st instance triggers a new batch allocation.
        UInt128[] array = new UInt128[2600];
        for (int i = 0; i < 2600; i++) {
            array[i] = factory.create(i);
        }

        // Verify values
        for (int i = 0; i < 2600; i++) {
            assertEquals(i, array[i].longValue());
        }

        // Recycle all
        for (int i = 0; i < 2600; i++) {
            factory.recycle(array[i]);
        }

        // Re-acquire and make sure they are recycled (no new batch allocated)
        for (int i = 0; i < 2600; i++) {
            UInt128 recycled = factory.create();
            assertTrue(recycled.isZero());
        }
    }

    @Test
    public void testRecyclingAndBatchAllocation256() {
        UInt256Factory factory = new UInt256Factory();

        // Create 1300 instances of UInt256.
        // Each batch is 10000 ints, which fits 1250 UInt256 instances.
        // The 1251st instance triggers a new batch allocation.
        UInt256[] array = new UInt256[1300];
        for (int i = 0; i < 1300; i++) {
            array[i] = factory.create(i);
        }

        // Verify values
        for (int i = 0; i < 1300; i++) {
            assertEquals(i, array[i].longValue());
        }

        // Recycle all
        for (int i = 0; i < 1300; i++) {
            factory.recycle(array[i]);
        }

        // Re-acquire and make sure they are recycled (no new batch allocated)
        for (int i = 0; i < 1300; i++) {
            UInt256 recycled = factory.create();
            assertTrue(recycled.isZero());
        }
    }

    @Test
    public void testFactoryConcurrency() throws InterruptedException {
        final UInt128Factory factory = new UInt128Factory();
        Thread[] threads = new Thread[10];
        final AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < 1000; j++) {
                            UInt128 val = factory.create(j);
                            assertEquals(j, val.longValue());
                            factory.recycle(val);
                        }
                    } catch (Throwable t) {
                        error.set(t);
                    }
                }
            });
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        assertNull(error.get());
    }
}
